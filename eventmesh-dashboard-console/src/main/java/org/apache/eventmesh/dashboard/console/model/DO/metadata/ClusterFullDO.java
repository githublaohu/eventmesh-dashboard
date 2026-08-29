package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import org.apache.eventmesh.dashboard.console.entity.cluster.ClusterEntity;
import org.apache.eventmesh.dashboard.console.entity.cluster.ClusterRelationshipEntity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hahaha
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClusterFullDO extends BaseFullDO {

    private ClusterEntity current;

    private List<ClusterFullDO> capCluster;

    private List<ClusterFullDO> ordinaryCluster;

    private List<ClusterRelationshipEntity> relationship;

    private List<RuntimeFullDO> runtimes;

}
