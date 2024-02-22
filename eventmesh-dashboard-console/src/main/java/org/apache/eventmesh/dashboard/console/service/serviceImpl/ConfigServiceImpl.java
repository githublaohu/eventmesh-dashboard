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

package org.apache.eventmesh.dashboard.console.service.serviceImpl;

import org.apache.eventmesh.dashboard.console.entity.ConfigEntity;
import org.apache.eventmesh.dashboard.console.mapper.ConfigMapper;
import org.apache.eventmesh.dashboard.console.service.ConfigService;
import org.apache.eventmesh.dashboard.console.utils.ParsePropertiesUtils;
import org.apache.eventmesh.dashboard.console.utils.Yml2PropertiesUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * service for Config
 */

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    @Override
    public List<ConfigEntity> selectAll(ConfigEntity config) {
        return configMapper.selectAll(config);
    }

    @Override
    public ConfigEntity selectOne(ConfigEntity config) {
        return configMapper.selectOne(config);
    }


    /**
     * unfinished,the configProperties is expected to be divided into many config,one config only
     * represents one key-value
     * @param config the config got from the frontend
     * @return undoen
     */
    @Override
    public ConfigEntity insertConfig(ConfigEntity config) {
        String properties = config.getConfigProperties();
        if (!properties.split("\\n")[0].contains("=")) {
            properties = Yml2PropertiesUtils.castProperties(properties);
        }
        String[] configs = ParsePropertiesUtils.parseProperties(properties);
        config.setConfigName(configs[0]);
        config.setConfigValue(configs[1]);
        // unfinished
        return null;
    }

    @Override
    public ConfigEntity updateOneConfig(ConfigEntity config) {
        configMapper.updateOneConfig(config);
        return config;
    }

    @Override
    public ConfigEntity updateAllConfig(ConfigEntity config) {
        configMapper.updateAllConfig(config);
        return config;
    }

    @Override
    public ConfigEntity deleteConfig(ConfigEntity config) {
        configMapper.deleteConfig(config);
        return config;
    }
}
