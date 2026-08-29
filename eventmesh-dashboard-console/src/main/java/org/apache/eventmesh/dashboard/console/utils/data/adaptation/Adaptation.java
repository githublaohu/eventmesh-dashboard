package org.apache.eventmesh.dashboard.console.utils.data.adaptation;

import org.apache.eventmesh.dashboard.common.enums.ClusterType;
import org.apache.eventmesh.dashboard.common.enums.MetadataType;

/**
 * @author hahaha
 */
public interface Adaptation<T, V> {

    ClusterType[] clusterType();

    String operation();

    MetadataType metadataType();

    void adaptation(T t, V v);

    void fill(T t);

    default String match() {
        return "";
    }
}
