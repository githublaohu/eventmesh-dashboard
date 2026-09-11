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


package org.apache.eventmesh.dashboard.console.controller.deploy;

import org.apache.eventmesh.dashboard.common.enums.DeployStatusType;
import org.apache.eventmesh.dashboard.console.model.deploy.ClusterLifecycleDTO;
import org.apache.eventmesh.dashboard.console.model.deploy.ClusterLifecycleVO;
import org.apache.eventmesh.dashboard.console.service.cluster.ClusterLifecycleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Homepage lifecycle endpoints acknowledge database changes only. */
@RestController
@RequestMapping("organization/clusterCycleDeploy")
public class ClusterLifecycleController {
    private final ClusterLifecycleService lifecycleService;

    public ClusterLifecycleController(ClusterLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @PostMapping("pauseCluster")
    public ClusterLifecycleVO pauseCluster(@RequestBody @Validated ClusterLifecycleDTO request) {
        return lifecycleService.submit(request, DeployStatusType.PAUSE);
    }

    @PostMapping("resumeCluster")
    public ClusterLifecycleVO resumeCluster(@RequestBody @Validated ClusterLifecycleDTO request) {
        return lifecycleService.submit(request, DeployStatusType.RESET);
    }

    @PostMapping("uninstallCluster")
    public ClusterLifecycleVO uninstallCluster(@RequestBody @Validated ClusterLifecycleDTO request) {
        return lifecycleService.submit(request, DeployStatusType.UNINSTALL);
    }
}
