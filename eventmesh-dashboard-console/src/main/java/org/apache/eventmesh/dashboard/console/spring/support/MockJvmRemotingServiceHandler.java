package org.apache.eventmesh.dashboard.console.spring.support;

import org.apache.eventmesh.dashboard.common.enums.MetadataType;
import org.apache.eventmesh.dashboard.core.metadata.SyncMetadataCreateFactory;
import org.apache.eventmesh.dashboard.core.remoting.RemotingServiceHandler;
import org.apache.eventmesh.dashboard.core.remoting.jvm.AbstractJvmService;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

/**
 * @author hahaha
 */
public class MockJvmRemotingServiceHandler implements RemotingServiceHandler {

    @Setter
    private Map<MetadataType, SyncMetadataCreateFactory> syncMetadataCreateFactoryMap = new HashMap<>();


    @Override
    public void serviceInit(Object object) {
        if (object.getClass().isAssignableFrom(AbstractJvmService.class)) {
            return;
        }
        AbstractJvmService abstractJvmService = (AbstractJvmService) object;
        abstractJvmService.setSyncMetadataCreateFactoryMap(syncMetadataCreateFactoryMap);
    }
}
