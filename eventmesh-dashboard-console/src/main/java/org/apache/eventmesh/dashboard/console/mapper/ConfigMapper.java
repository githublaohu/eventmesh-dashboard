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

package org.apache.eventmesh.dashboard.console.mapper;

import org.apache.eventmesh.dashboard.console.entity.ConfigEntity;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;



import java.util.List;



/**
 * methods used  for interaction between dashboard with database in config table.
 */

@Mapper
public interface ConfigMapper {

    @Select("select * from config where cluster_phy_id = #{clusterPhyId} and instance_id =#{instanceId}")
    ConfigEntity selectOne(ConfigEntity config);

    @Select("select * from config where cluster_phy_id=#{clusterPhyId} and config_type=#{configType}")
    List<ConfigEntity> selectAll(ConfigEntity config);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @Insert("insert into config(cluster_phy_id, config_type, instance_id,config_properties, config_name, config_value, source, start_version, "
        + "end_version, diff_type, create_time, update_time) VALUES(#{clusterPhyId},#{configType},#{instanceId},"
        + "#{configProperties},#{configName},#{configValue},#{source},#{startVersion},#{endVersion},#{diffType},#{createTime},#{updateTime}) ")
    void insertConfig(ConfigEntity config);

    @Delete("delete from config where cluster_phy_id=#{clutsterPhyId} and instance_id=#{instanceId}")
    void deleteConfig(ConfigEntity config);

    @Update("update config set  config_properties=#{configProperties},config_name=#{configName} ,config_value=#{configValue} where "
        + "cluster_phy_id=#{clusterPhyId} and instance_id=#{instanceId} and config_type = #{configType}")
    void updateOneConfig(ConfigEntity config);

    @Update("update config set config_properties=${configProperties},config_name=#{configName}"
        + ",config_value=#{configValue} where cluster_phy_id=#{clusterPhyId} and config_type = #{configType}")
    void updateAllConfig(ConfigEntity config);
}
