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

package org.apache.eventmesh.dashboard.service.remoting;

import org.apache.eventmesh.dashboard.common.model.remoting.BaseGlobalResult;
import org.apache.eventmesh.dashboard.common.model.remoting.config.AddConfigRequest;
import org.apache.eventmesh.dashboard.common.model.remoting.config.DeleteConfigRequest;
import org.apache.eventmesh.dashboard.common.model.remoting.config.GetConfigRequest;
import org.apache.eventmesh.dashboard.common.model.remoting.config.UpdateConfigRequest;
import org.apache.eventmesh.dashboard.common.model.remoting.topic.GetTopicsRequest;
import org.apache.eventmesh.dashboard.common.model.remoting.topic.GetTopicsResult;

/**
 * A remoting service for config operations. Getting configs from different sources 1. runtime 2. RocketMQ 3. connector to db
 */
public interface ConfigRemotingService {


    BaseGlobalResult addConfig(AddConfigRequest addConfigRequest);

    default BaseGlobalResult updateConfig(UpdateConfigRequest updateConfigRequest) {
        return addConfig(updateConfigRequest);
    }

    default BaseGlobalResult deleteConfig(DeleteConfigRequest deleteConfigRequest) {
        return addConfig(deleteConfigRequest);
    }

    GetTopicsResult getConfig(GetConfigRequest getConfigRequest);

    GetTopicsResult getAllTopics(GetTopicsRequest getTopicsRequest);
}
