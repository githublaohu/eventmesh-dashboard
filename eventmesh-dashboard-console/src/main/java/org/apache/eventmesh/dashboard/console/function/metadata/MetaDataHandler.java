package org.apache.eventmesh.dashboard.console.function.metadata;

import java.util.List;

public interface MetaDataHandler<T> {



    default void handler(List<T> addData , List<T> updateData , List<T> deleteData){
    }

    void addMeta(T meta);


    default  void updateMeta(T meta){
        this.addMeta(meta);
    }

    void deleteMeta(T meta);

}
