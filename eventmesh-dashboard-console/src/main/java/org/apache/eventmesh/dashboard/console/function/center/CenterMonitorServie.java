package org.apache.eventmesh.dashboard.console.function.center;

import org.apache.eventmesh.dashboard.console.function.ServierConfig;
import org.apache.eventmesh.dashboard.console.function.metadata.seriver.RuntimeMetaService;

public interface CenterMonitorServie {


    void init(RuntimeMetaService runtimeMetaService , ServierConfig servierConfig);

    void close();

}
