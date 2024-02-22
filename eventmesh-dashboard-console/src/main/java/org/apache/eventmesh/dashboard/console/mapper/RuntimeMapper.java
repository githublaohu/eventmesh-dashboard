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

import org.apache.eventmesh.dashboard.console.entity.RuntimeEntity;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 *
 */
@Mapper
public interface RuntimeMapper {

    @Insert("insert into runtime (cluster_phy_id, host, storage_cluster_id, port, jmx_port, start_timestamp, rack, status, create_time,"
        + " update_time, endpoint_map) VALUES (#{clusterPhyId},#{host},#{storageClusterId},#{port},#{jmxPort},#{startTimestamp},"
        + "#{rack},#{status},#{createTime},#{updateTime},#{endpointMap})")
    void insert(RuntimeEntity runtime);

    @Delete("delete from runtime where id = #{id}")
    void deleteByPrimaryId(RuntimeEntity runtime);

    @Delete("delete from runtime where cluster_phy_id=#{clusterPhyId} and host = #{host} and storage_cluster_id = #{storageClusterId}")
    void deleteByIndexId(RuntimeEntity runtime);

    @Update("<script> "
        + "update runtime "
        + " <trim prefix='set' suffixOverrides=','>"
        + "  <if test='port!=null'>port=#{port},</if>"
        + "  <if test='jmxPort!=null'>jmx_port=#{jmxPort},</if>"
        + "  <if test='rack!=null'>rack=#{rack},</if>"
        + "  <if test='status!=null'>status=#{status},</if>"
        + "  <if test='updateTime!=null'>update_time=#{updateTime},</if>"
        + "  <if test='endpointMap!=null'>endpoint_map=#{endpointMap},</if>"
        + " </trim> where cluster_phy_id=#{clusterPhyId} and host = #{host} and storage_cluster_id = #{storageClusterId}"
        + "</script>")
    void updateOne(RuntimeEntity runtime);

    @Select("select * from runtime where cluster_phy_id=#{clusterPhyId} and host = #{host}")
    RuntimeEntity selectByName(RuntimeEntity runtime);

    @Select("select * from runtime where cluster_phy_id=#{clusterPhyId} and storage_cluster_id=#{storageClusterId}")
    RuntimeEntity selectById(RuntimeEntity runtime);
}
