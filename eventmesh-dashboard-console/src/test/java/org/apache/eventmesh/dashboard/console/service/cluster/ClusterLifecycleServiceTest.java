/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.eventmesh.dashboard.console.service.cluster;

import org.apache.eventmesh.dashboard.common.enums.DeployStatusType;
import org.apache.eventmesh.dashboard.console.controller.deploy.handler.ClusterLifecycleHandler;
import org.apache.eventmesh.dashboard.console.mapper.cluster.ClusterMapper;
import org.apache.eventmesh.dashboard.console.model.deploy.ClusterLifecycleDTO;
import org.apache.eventmesh.dashboard.console.service.cluster.impl.ClusterLifecycleServiceImpl;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises real mapper SQL and transactions against an isolated in-memory database. */
@SpringJUnitConfig(ClusterLifecycleServiceTest.Config.class)
class ClusterLifecycleServiceTest {
    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        DataSource dataSource() {
            JdbcDataSource source = new JdbcDataSource();
            source.setURL("jdbc:h2:mem:lifecycle;MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }
        @Bean
        DataSourceTransactionManager transactionManager(DataSource source) {
            return new DataSourceTransactionManager(source);
        }
        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource source) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(source);
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.addMapper(ClusterMapper.class);
            factory.setConfiguration(configuration);
            return factory.getObject();
        }
        @Bean
        ClusterMapper mapper(SqlSessionFactory factory) {
            return org.mockito.Mockito.spy(new SqlSessionTemplate(factory).getMapper(ClusterMapper.class));
        }
        @Bean
        ClusterLifecycleHandler lifecycleHandler() {
            return new ClusterLifecycleHandler();
        }
        @Bean
        ClusterLifecycleService service() {
            return new ClusterLifecycleServiceImpl();
        }
    }
    @Autowired
    private ClusterLifecycleService service;
    @Autowired
    private ClusterLifecycleHandler lifecycleHandler;
    @Autowired
    private DataSource source;
    @Autowired
    private ClusterMapper mapper;
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepare() {
        org.mockito.Mockito.reset(mapper);
        jdbc = new JdbcTemplate(source);
        jdbc.execute("DROP ALL OBJECTS");
        for (String table : List.of("cluster", "runtime")) {
            jdbc.execute("CREATE TABLE " + table + " (id BIGINT PRIMARY KEY, organization_id BIGINT, cluster_id BIGINT, "
                + "deploy_status_type VARCHAR(40), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "is_delete INT DEFAULT 0, status INT DEFAULT 1)");
        }
        jdbc.update("INSERT INTO cluster(id, organization_id, deploy_status_type) VALUES(1, 1, 'CREATE_SUCCESS')");
        jdbc.update("INSERT INTO runtime(id, organization_id, cluster_id, deploy_status_type) VALUES(10, 1, 1, 'CREATE_SUCCESS')");
        jdbc.update("INSERT INTO runtime(id, organization_id, cluster_id, deploy_status_type) VALUES(20, 1, 2, 'CREATE_SUCCESS')");
    }
    private ClusterLifecycleDTO request() {
        ClusterLifecycleDTO request = new ClusterLifecycleDTO();
        request.setOrganizationId(1L);
        request.setClusterId(1L);
        return request;
    }
    private String status(String table, long id) {
        return jdbc.queryForObject("SELECT deploy_status_type FROM " + table + " WHERE id = ?", String.class, id);
    }
    @Test
    void pausePersistsPendingAndLeavesOtherClusterUntouched() {
        assertEquals(1, service.submit(request(), DeployStatusType.PAUSE));
        assertEquals("PAUSE", status("cluster", 1));
        assertEquals("PAUSE", status("runtime", 10));
        assertEquals("CREATE_SUCCESS", status("runtime", 20));
        assertThrows(ResponseStatusException.class, () -> service.submit(request(), DeployStatusType.PAUSE));
    }
    @Test
    void resumePersistsReset() {
        jdbc.update("UPDATE cluster SET deploy_status_type = 'PAUSE_SUCCESS'");
        jdbc.update("UPDATE runtime SET deploy_status_type = 'PAUSE_SUCCESS' WHERE id = 10");
        service.submit(request(), DeployStatusType.RESET);
        assertEquals("RESET", status("cluster", 1));
        assertEquals("RESET", status("runtime", 10));
    }
    @Test
    void uninstallKeepsRowsAndWritesPending() {
        service.submit(request(), DeployStatusType.UNINSTALL);
        assertEquals("UNINSTALL", status("cluster", 1));
        assertEquals("UNINSTALL", status("runtime", 10));
        assertEquals(0, jdbc.queryForObject("SELECT is_delete FROM cluster WHERE id = 1", Integer.class));
    }
    @Test
    void wrongOrganizationCannotWrite() {
        ClusterLifecycleDTO request = request();
        request.setOrganizationId(2L);
        assertThrows(ResponseStatusException.class, () -> service.submit(request, DeployStatusType.PAUSE));
        assertEquals("CREATE_SUCCESS", status("cluster", 1));
    }
    @Test
    void busyRuntimeRejectsBeforeWriting() {
        jdbc.update("UPDATE runtime SET deploy_status_type = 'CREATE_ING' WHERE id = 10");
        assertThrows(ResponseStatusException.class, () -> service.submit(request(), DeployStatusType.PAUSE));
        assertEquals("CREATE_SUCCESS", status("cluster", 1));
    }
    @Test
    void runtimeWriteFailureRollsBackCluster() {
        jdbc.execute("ALTER TABLE runtime ADD CONSTRAINT reject_pause CHECK (deploy_status_type <> 'PAUSE')");
        assertThrows(RuntimeException.class, () -> service.submit(request(), DeployStatusType.PAUSE));
        assertEquals("CREATE_SUCCESS", status("cluster", 1));
        assertEquals("CREATE_SUCCESS", status("runtime", 10));
    }
    @Test
    void concurrentSubmissionsOnlyAcceptOne() throws Exception {
        // Both transactions must read the same old state before either performs its conditional update.
        var readers = new java.util.concurrent.CountDownLatch(2);
        org.mockito.Mockito.doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            readers.countDown();
            assertTrue(readers.await(5, java.util.concurrent.TimeUnit.SECONDS));
            return result;
        }).when(mapper).selectRuntimes(org.mockito.ArgumentMatchers.any());
        var executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> submit = () -> {
                try {
                    service.submit(request(), DeployStatusType.PAUSE);
                    return true;
                } catch (ResponseStatusException exception) {
                    return false;
                }
            };
            var results = executor.invokeAll(List.of(submit, submit));
            assertNotEquals(results.get(0).get(), results.get(1).get());
        } finally {
            executor.shutdownNow();
        }
    }
    @Test
    void staleStateCannotOverwriteAnotherOperation() {
        jdbc.update("UPDATE cluster SET deploy_status_type = 'UNINSTALL' WHERE id = 1");
        assertEquals(0, mapper.updateCluster(request(), DeployStatusType.CREATE_SUCCESS, DeployStatusType.PAUSE));
        assertEquals("UNINSTALL", status("cluster", 1));
        jdbc.update("UPDATE runtime SET deploy_status_type = 'UNINSTALL' WHERE id = 10");
        assertEquals(0, mapper.updateRuntime(request(), 10L, DeployStatusType.CREATE_SUCCESS, DeployStatusType.PAUSE));
        assertEquals("UNINSTALL", status("runtime", 10));
    }

    @Test
    void runtimeConditionalUpdateConflictRollsBackCluster() {
        org.mockito.Mockito.doReturn(0).when(mapper).updateRuntime(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(10L),
            org.mockito.ArgumentMatchers.eq(DeployStatusType.CREATE_SUCCESS), org.mockito.ArgumentMatchers.eq(DeployStatusType.PAUSE));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.submit(request(), DeployStatusType.PAUSE));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("CREATE_SUCCESS", status("cluster", 1));
        assertEquals("CREATE_SUCCESS", status("runtime", 10));
    }

    @Test
    void emptyClusterAndInvalidAction() {
        jdbc.update("DELETE FROM runtime WHERE cluster_id = 1");
        assertThrows(ResponseStatusException.class, () -> service.submit(request(), DeployStatusType.CREATE));
        assertEquals(0, service.submit(request(), DeployStatusType.PAUSE));
    }
    @Test
    void httpEndpointsValidateAndAcknowledgePersistence() throws Exception {
        var controller = new org.apache.eventmesh.dashboard.console.controller.deploy.ClusterCycleController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "clusterLifecycleHandler", lifecycleHandler);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        String body = "{\"organizationId\":1,\"clusterId\":1}";
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/pauseCluster").contentType("application/json").content(body))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.deployStatusType").value("PAUSE"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.clusterId").value(1))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.runtimeCount").value(1));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/pauseCluster").contentType("application/json").content(body))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isConflict());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/resumeCluster").contentType("application/json").content("{}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        jdbc.update("UPDATE cluster SET deploy_status_type = 'PAUSE_SUCCESS'");
        jdbc.update("UPDATE runtime SET deploy_status_type = 'PAUSE_SUCCESS' WHERE id = 10");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/resumeCluster").contentType("application/json").content(body))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.deployStatusType").value("RESET"));
        jdbc.update("UPDATE cluster SET deploy_status_type = 'RESET_SUCCESS'");
        jdbc.update("UPDATE runtime SET deploy_status_type = 'RESET_SUCCESS' WHERE id = 10");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/uninstallCluster").contentType("application/json").content(body))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.deployStatusType").value("UNINSTALL"));
    }

}
