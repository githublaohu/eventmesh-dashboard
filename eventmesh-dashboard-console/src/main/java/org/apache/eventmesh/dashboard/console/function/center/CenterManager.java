package org.apache.eventmesh.dashboard.console.function.center;

import lombok.Setter;
import org.apache.eventmesh.dashboard.console.function.SeriverTypeEnums;
import org.apache.eventmesh.dashboard.console.function.ServierConfig;
import org.apache.eventmesh.dashboard.console.function.center.meta.EtcdCenterMonitorServie;
import org.apache.eventmesh.dashboard.console.function.center.meta.NacosCenterMonitorServie;
import org.apache.eventmesh.dashboard.console.function.metadata.MetaDataHandler;
import org.apache.eventmesh.dashboard.console.function.metadata.data.CenterMeta;
import org.apache.eventmesh.dashboard.console.function.metadata.seriver.RuntimeMetaService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CenterManager {

    private static final Map<SeriverTypeEnums, Class<?>> SERIVER_TYPE_ENUMS_CLASS_MAP = new ConcurrentHashMap<>();

    static{
        SERIVER_TYPE_ENUMS_CLASS_MAP.put(SeriverTypeEnums.CENTER_ETCD, EtcdCenterMonitorServie.class);
        SERIVER_TYPE_ENUMS_CLASS_MAP.put(SeriverTypeEnums.CENTER_NACOS, NacosCenterMonitorServie.class);
    }

    private Map<String, CenterMonitorServie> centerMap = new ConcurrentHashMap<>();

    private CenterMetaService centerMetaService = new CenterMetaService();

    @Setter
    private RuntimeMetaService runtimeMetaService;

    public MetaDataHandler<CenterMeta> getCenterMetaService(){
        return centerMetaService;
    }


    public void monitor(ServierConfig servierConfig){
        Class<?> clazz = SERIVER_TYPE_ENUMS_CLASS_MAP.get(servierConfig.getSeriverTypeEnums());
        try {
            CenterMonitorServie centerMonitorServie = (CenterMonitorServie)clazz.newInstance();
            centerMonitorServie.init(runtimeMetaService, servierConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public class CenterMetaService implements MetaDataHandler<CenterMeta> {

        @Override
        public void addMeta(CenterMeta meta) {
            CenterManager.this.monitor(null);
        }

        @Override
        public void deleteMeta(CenterMeta meta) {

        }
    }


}
