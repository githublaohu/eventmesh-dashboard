package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import org.apache.eventmesh.dashboard.console.entity.message.TopicEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TopicDO extends ConfigDO {

    private TopicEntity topicEntity;

}
