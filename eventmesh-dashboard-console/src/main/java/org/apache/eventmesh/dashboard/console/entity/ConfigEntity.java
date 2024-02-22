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

package org.apache.eventmesh.dashboard.console.entity;

import java.sql.Timestamp;

import lombok.Data;


@Data
public class ConfigEntity {

    private Long id;

    private Long clusterPhyId;

    /**
    private  * the type of config.
    private  * For example 0 represents runtime
    private  */
    private Integer configType;

    /**
    private  * the id of related instance with certain type.
    private  * For example,it represents the id corresponding to the runtime
    private  */
    private Long instanceId;

    private String configProperties;

    /**
    private  * the name of the config.
    private  */
    private String configName;

    /**
    private  * the value of the config.
    private  */
    private String configValue;

    /**
    private  * 0 means default and 1 means customized
    private  */
    private Short source;

    /**
    private  * the version of the config when the instance starts.
    private  */
    private String startVersion;

    /**
    private  * the version of the config when the instance ends.
    private  */
    private String endVersion;

    private Short diffType;

    private Timestamp createTime;

    private Timestamp updateTime;


}
