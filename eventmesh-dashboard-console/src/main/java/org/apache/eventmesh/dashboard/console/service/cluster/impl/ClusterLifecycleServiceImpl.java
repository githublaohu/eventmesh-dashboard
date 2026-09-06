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


package org.apache.eventmesh.dashboard.console.service.cluster.impl;

import org.apache.eventmesh.dashboard.common.enums.DeployStatusType;
import org.apache.eventmesh.dashboard.console.entity.cluster.ClusterEntity;
import org.apache.eventmesh.dashboard.console.entity.cluster.RuntimeEntity;
import org.apache.eventmesh.dashboard.console.mapper.cluster.ClusterMapper;
import org.apache.eventmesh.dashboard.console.model.deploy.ClusterLifecycleDTO;
import org.apache.eventmesh.dashboard.console.service.cluster.ClusterLifecycleService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

/** Updates lifecycle state conditionally and rolls back all writes on conflict. */
@Service
@Slf4j
public class ClusterLifecycleServiceImpl implements ClusterLifecycleService {
    private static final Set<DeployStatusType> PAUSABLE = EnumSet.of(
        DeployStatusType.CREATE_SUCCESS, DeployStatusType.CREATE_FULL_SUCCESS,
        DeployStatusType.CREATE_CAP_UPDATE_SUCCESS, DeployStatusType.UPDATE_SUCCESS,
        DeployStatusType.UPDATE_FULL_SUCCESS, DeployStatusType.RESET_SUCCESS,
        DeployStatusType.PAUSE_FAIL, DeployStatusType.PAUSE_FULL_FAIL);
    private static final Set<DeployStatusType> RESUMABLE = EnumSet.of(
        DeployStatusType.PAUSE_SUCCESS, DeployStatusType.PAUSE_FULL_SUCCESS, DeployStatusType.RESET_FAIL);
    private static final Set<DeployStatusType> UNINSTALLABLE = EnumSet.of(
        DeployStatusType.SETTLE, DeployStatusType.BUILD_SUCCESS, DeployStatusType.CREATE_SUCCESS,
        DeployStatusType.CREATE_FULL_SUCCESS, DeployStatusType.CREATE_CAP_UPDATE_SUCCESS,
        DeployStatusType.UPDATE_SUCCESS, DeployStatusType.UPDATE_FULL_SUCCESS,
        DeployStatusType.RESET_SUCCESS, DeployStatusType.PAUSE_SUCCESS, DeployStatusType.PAUSE_FULL_SUCCESS,
        DeployStatusType.CREATE_FAIL, DeployStatusType.CREATE_FULL_FAIL, DeployStatusType.CREATE_CAP_UPDATE_FAIL,
        DeployStatusType.UPDATE_FAIL, DeployStatusType.UPDATE_FULL_FAIL, DeployStatusType.RESET_FAIL,
        DeployStatusType.PAUSE_FAIL, DeployStatusType.PAUSE_FULL_FAIL,
        DeployStatusType.UNINSTALL_FAIL, DeployStatusType.UNINSTALL_FAILED,
        DeployStatusType.RESOURCE_APPLY_FAILED, DeployStatusType.CHECKING_FAILED);
    @Autowired
    private ClusterMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submit(ClusterLifecycleDTO request, DeployStatusType target) {
        if (request == null || request.getOrganizationId() == null || request.getOrganizationId() <= 0
            || request.getClusterId() == null || request.getClusterId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cluster or organization ID");
        }
        Set<DeployStatusType> allowed = allowedStatuses(target);
        ClusterEntity cluster = mapper.selectCluster(request);
        if (cluster == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found in this organization");
        }
        validateStatus(cluster.getDeployStatusType(), allowed);
        List<RuntimeEntity> runtimes = mapper.selectRuntimes(request);
        for (RuntimeEntity runtime : runtimes) {
            validateStatus(runtime.getDeployStatusType(), allowed);
        }
        requireUpdated(mapper.updateCluster(request, cluster.getDeployStatusType(), target));
        for (RuntimeEntity runtime : runtimes) {
            requireUpdated(mapper.updateRuntime(request, runtime.getId(), runtime.getDeployStatusType(), target));
        }
        log.info("Cluster lifecycle request persisted: organizationId={}, clusterId={}, target={}, runtimeCount={}",
            request.getOrganizationId(), request.getClusterId(), target, runtimes.size());
        return runtimes.size();
    }

    private Set<DeployStatusType> allowedStatuses(DeployStatusType target) {
        if (target == DeployStatusType.PAUSE) {
            return PAUSABLE;
        }
        if (target == DeployStatusType.RESET) {
            return RESUMABLE;
        }
        if (target == DeployStatusType.UNINSTALL) {
            return UNINSTALLABLE;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported lifecycle action");
    }

    private void validateStatus(DeployStatusType current, Set<DeployStatusType> allowed) {
        if (current == null || !allowed.contains(current)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Cluster or runtime state does not allow this action; refresh before retrying");
        }
    }

    private void requireUpdated(int count) {
        if (count != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resource state changed; refresh before retrying");
        }
    }
}
