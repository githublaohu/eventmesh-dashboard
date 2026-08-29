package org.apache.eventmesh.dashboard.core.remoting;

import org.apache.eventmesh.dashboard.common.model.base.BaseRuntimeIdBase;
import org.apache.eventmesh.dashboard.common.model.remoting.GlobalResult;
import org.apache.eventmesh.dashboard.common.model.remoting.RemotingActionType;

import java.util.List;

/**
 * @author hahaha
 */
public interface RemotingResultHook {


    void success(RemotingActionType remotingActionType, BaseRuntimeIdBase baseRuntimeIdBase, GlobalResult<Object> result);

    default void success(RemotingActionType remotingActionType, List<BaseRuntimeIdBase> baseRuntimeIdBase){

    }

    void fail(RemotingActionType remotingActionType, BaseRuntimeIdBase baseRuntimeIdBase, GlobalResult<Object> result, Throwable e);

}
