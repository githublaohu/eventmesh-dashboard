package org.apache.eventmesh.dashboard.console.function.metadata;

import java.util.List;

public interface SyncDataService<T> {



    List<T> syncsData();


    String getUnique(T t);

}
