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

import org.apache.eventmesh.dashboard.console.entity.PhysicalClusterEntity;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 *
 */
@Mapper
public interface PhysicalClusterMapper {


    @Insert("insert into physical_cluster (name, register_name_list, bootstrap_servers, eventmesh_version, client_properties,"
        + " jmx_properties, reg_properties, description, auth_type, run_state, create_time, update_time) VALUES (#{name},"
        + "#{registerNameList},#{bootstrapServer},#{eventmeshVersion},#{clientProperties},#{jmxProperties},#{regProperties}"
        + ",#{description},#{authType},#{runState},#{createTime},#{updateTime}) ")
    void insert(PhysicalClusterEntity cluster);

    @Delete("delete from physical_cluster where name = #{name}")
    void deleteOne(PhysicalClusterEntity cluster);

    @Update("update physical_cluster set name=#{name},register_name_list=#{registerNameList},bootstrap_servers="
        + "#{bootstrapServer},eventmesh_version=#{eventmeshVersion},client_properties=#{clientProperties},jmx_properties="
        + "#{jmxProperties},reg_properties=#{regProperties},description=#{description},auth_type=#{authType},run_state=#{runState},"
        + "update_time=#{updateTime} where id=#{id}")
    void updateOne(PhysicalClusterEntity cluster);

    @Select("select * from physical_cluster where name=#{name}")
    PhysicalClusterEntity selectOne(String name);
}
