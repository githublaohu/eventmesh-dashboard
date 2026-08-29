package org.apache.eventmesh.dashboard.console.utils.data.adaptation.eventmesh;

import org.apache.eventmesh.dashboard.common.enums.ClusterType;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.Adaptation;

/**
 * @author hahaha
 */
public interface EventmeshMQAdaptation<T, V> extends Adaptation<T, V> {

    @Override
    default ClusterType[] clusterType() {
        return new ClusterType[] {ClusterType.EVENTMESH_JVM_CLUSTER, ClusterType.EVENTMESH_CLUSTER, ClusterType.EVENTMESH_JVM_RUNTIME,
            ClusterType.EVENTMESH_RUNTIME};
    }

}
