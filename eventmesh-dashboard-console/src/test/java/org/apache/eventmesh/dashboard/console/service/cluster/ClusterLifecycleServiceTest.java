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
import org.apache.eventmesh.dashboard.console.mapper.cluster.ClusterLifecycleMapper;
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
            configuration.addMapper(ClusterLifecycleMapper.class);
            factory.setConfiguration(configuration);
            return factory.getObject();
        }
        @Bean
        ClusterLifecycleMapper mapper(SqlSessionFactory factory) {
            return new SqlSessionTemplate(factory).getMapper(ClusterLifecycleMapper.class);
        }
        @Bean
        ClusterLifecycleService service(ClusterLifecycleMapper mapper) {
            return new ClusterLifecycleServiceImpl(mapper);
        }
    }
    @Autowired
    private ClusterLifecycleService service;
    @Autowired
    private DataSource source;
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepare() {
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
        assertEquals(1, service.submit(request(), DeployStatusType.PAUSE).getRuntimeCount());
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
    void emptyClusterAndInvalidAction() {
        jdbc.update("DELETE FROM runtime WHERE cluster_id = 1");
        assertThrows(ResponseStatusException.class, () -> service.submit(request(), DeployStatusType.CREATE));
        assertEquals(0, service.submit(request(), DeployStatusType.PAUSE).getRuntimeCount());
    }
    @Test
    void httpEndpointsValidateAndAcknowledgePersistence() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
            new org.apache.eventmesh.dashboard.console.controller.deploy.ClusterLifecycleController(service)).build();
        String body = "{\"organizationId\":1,\"clusterId\":1}";
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
            "/organization/clusterCycleDeploy/pauseCluster").contentType("application/json").content(body))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.deployStatusType").value("PAUSE"));
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
