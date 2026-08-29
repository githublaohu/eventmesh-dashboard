package org.apache.eventmesh.dashboard.console.utils.data.adaptation.eventmesh.topic;

import org.apache.eventmesh.dashboard.console.entity.message.TopicEntity;
import org.apache.eventmesh.dashboard.console.model.dto.topic.CreateTopicDTO;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.eventmesh.EventmeshMQAdaptation;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.operation.CreateAdaptation;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.rocketmq.RocketMQAdaptation;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.type.TopicAdaptation;

import java.util.Objects;

/**
 * @author hahaha
 */
public class EventMeshCreateTopicAdaptation implements TopicAdaptation<CreateTopicDTO>,
    EventmeshMQAdaptation<TopicEntity, CreateTopicDTO>,
    CreateAdaptation<TopicEntity, CreateTopicDTO> {

    @Override
    public void adaptation(TopicEntity topicEntity, CreateTopicDTO createTopicDTO) {
        if(Objects.isNull(createTopicDTO.getWriteQueueNum())){
            topicEntity.setWriteQueueNum(topicEntity.getWriteQueueNum());
        }else{
            topicEntity.setWriteQueueNum(createTopicDTO.getWriteQueueNum());
        }
    }
}
