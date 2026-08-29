package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hahaha
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseFullDO extends ConfigDO {

    private List<TopicDO> topicDOList;

    private List<GroupDO> groupDOList;
}
