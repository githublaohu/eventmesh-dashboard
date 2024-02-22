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

import org.apache.eventmesh.dashboard.console.entity.StoreEntity;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 *
 */

@Mapper
public interface StoreMapper {

    @Insert("insert into store (cluster_phy_id, store_id, store_type, host, runtime_id, topic_list, diff_type, port, jmx_port, start_timestamp,"
        + " rack, status, create_time, update_time, endpoint_map) VALUES (#{clusterPhyId},#{storeId},#{storeType},#{host},#{runtimeId},#{topicList},"
        + "#{diffType},#{port},#{jmxPort},#{startTimestamp},#{rack},#{status},#{createTime},#{updateTime},#{endpointMap})")
    void inset(StoreEntity store);

    @Delete("delete from store where store_id=#{storeId} and runtime_id = #{runtimeId} and cluster_phy_id =#{clusterPhyId}")
    void deleteOne(StoreEntity store);


    @Update("<script> "
        + "update store "
        + " <trim prefix='set' suffixOverrides=','>"
        + "  <if test='storeType!=null'>store_type=#{storeType},</if>"
        + "  <if test='host!=null'>host=#{host},</if>"
        + "  <if test='topicList!=null'>topic_list=#{topicList},</if>"
        + "  <if test='diffType!=null'>diff_type=#{diffType},</if>"
        + "  <if test='port!=null'>port=#{port},</if>"
        + "  <if test='jmxPort!=null'>jmx_port=#{jmxPort},</if>"
        + "  <if test='rack!=null'>rack=#{rack},</if>"
        + "  <if test='status!=null'>status=#{status},</if>"
        + "  <if test='updateTime!=null'>update_time=#{updateTime},</if>"
        + "  <if test='endpoint_map!=null'>endpoint_map=#{endpointMap},</if>"
        + " </trim> where cluster_phy_id=#{clusterPhyId} and host = #{host} and storage_cluster_id = #{storageClusterId}"
        + "</script>")
    void updateOne(StoreEntity store);

    @Select("select * from store where store_id=#{store_id}")
    void selectByStore(StoreEntity store);

    @Select("select * from store where cluster_phy_id=#{clusterPhyId} and store_id =#{storeId} and runtime_id=#{runtimeId}")
    void selectById(StoreEntity store);
}
