package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import org.apache.eventmesh.dashboard.console.entity.message.GroupEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hahaha
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupDO extends ConfigDO {

    private GroupEntity groupEntity;
}
