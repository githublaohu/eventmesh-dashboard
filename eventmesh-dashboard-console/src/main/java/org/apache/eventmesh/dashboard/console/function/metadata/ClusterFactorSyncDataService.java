package org.apache.eventmesh.dashboard.console.function.metadata;

import java.util.List;

public abstract class ClusterFactorSyncDataService<T> implements SyncDataService<T> {


    @Override
    public List<T> syncsData(){

        return null;
    }

    abstract void syncsData(Object clusterFactor);

}
