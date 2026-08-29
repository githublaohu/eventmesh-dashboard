package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import org.apache.eventmesh.dashboard.console.entity.cluster.RuntimeEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hahaha
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RuntimeFullDO extends BaseFullDO {

    private RuntimeEntity runtimeEntity;

}
