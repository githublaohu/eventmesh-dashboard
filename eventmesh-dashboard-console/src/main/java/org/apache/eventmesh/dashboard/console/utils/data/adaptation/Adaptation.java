package org.apache.eventmesh.dashboard.console.utils.data.adaptation;

import org.apache.eventmesh.dashboard.common.enums.ClusterType;

/**
 * @author hahaha
 */
public interface Adaptation<T, V> {

    void handler(T t, V v);

    default ClusterType[] clusterType() {
        return new ClusterType[0];
    }
}
