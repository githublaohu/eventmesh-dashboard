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


package org.apache.eventmesh.dashboard.console.mapper.cluster;

import org.apache.eventmesh.dashboard.common.enums.DeployStatusType;
import org.apache.eventmesh.dashboard.console.entity.cluster.ClusterEntity;
import org.apache.eventmesh.dashboard.console.entity.cluster.RuntimeEntity;
import org.apache.eventmesh.dashboard.console.model.deploy.ClusterLifecycleDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** Locks and updates only the requested cluster and its directly owned runtimes. */
@Mapper
public interface ClusterLifecycleMapper {
    @Select("""
        SELECT id, deploy_status_type FROM cluster
        WHERE id = #{clusterId} AND organization_id = #{organizationId}
          AND is_delete = 0 AND status = 1 FOR UPDATE
        """)
    ClusterEntity lockCluster(ClusterLifecycleDTO request);

    @Select("""
        SELECT id, deploy_status_type FROM runtime
        WHERE cluster_id = #{clusterId} AND organization_id = #{organizationId}
          AND is_delete = 0 AND status = 1 ORDER BY id FOR UPDATE
        """)
    List<RuntimeEntity> lockRuntimes(ClusterLifecycleDTO request);

    @Update("""
        UPDATE cluster SET deploy_status_type = #{target}, update_time = CURRENT_TIMESTAMP
        WHERE id = #{request.clusterId} AND organization_id = #{request.organizationId}
          AND deploy_status_type = #{previous} AND is_delete = 0 AND status = 1
        """)
    int updateCluster(@Param("request") ClusterLifecycleDTO request,
        @Param("previous") DeployStatusType previous, @Param("target") DeployStatusType target);

    @Update("""
        UPDATE runtime SET deploy_status_type = #{target}, update_time = CURRENT_TIMESTAMP
        WHERE id = #{id} AND cluster_id = #{request.clusterId} AND organization_id = #{request.organizationId}
          AND deploy_status_type = #{previous} AND is_delete = 0 AND status = 1
        """)
    int updateRuntime(@Param("request") ClusterLifecycleDTO request, @Param("id") Long id,
        @Param("previous") DeployStatusType previous, @Param("target") DeployStatusType target);
}
