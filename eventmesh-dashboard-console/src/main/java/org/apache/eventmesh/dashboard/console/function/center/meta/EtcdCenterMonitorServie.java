package org.apache.eventmesh.dashboard.console.function.center.meta;

import org.apache.eventmesh.dashboard.console.function.ServierConfig;
import org.apache.eventmesh.dashboard.console.function.center.CenterMonitorServie;
import org.apache.eventmesh.dashboard.console.function.metadata.seriver.RuntimeMetaService;

public class EtcdCenterMonitorServie implements CenterMonitorServie {

    private RuntimeMetaService runtimeMetaService;

    @Override
    public void init(RuntimeMetaService runtimeMetaService , ServierConfig servierConfig) {
        this.runtimeMetaService = runtimeMetaService;

    }

    @Override
    public void close() {

    }
}
