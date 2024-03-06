package org.apache.eventmesh.dashboard.console.function.metadata;

import lombok.Data;

@Data
public class MetaDataServiceWrapper {

    private SingleMetaDataServiceWrapper dbToService;

    private SingleMetaDataServiceWrapper serviceToDb;

    private Class<?> clazz;


    @Data
    public static class SingleMetaDataServiceWrapper{
        SyncDataService<Object> syncsService;
        MetaDataHandler<Object> metaService;
        public SingleMetaDataServiceWrapper(SyncDataService<Object> syncsService, MetaDataHandler<Object> metaService) {
            this.syncsService = syncsService;
            this.metaService = metaService;
        }
    }
}
