package org.apache.eventmesh.dashboard.console.utils.data.adaptation.type;

import org.apache.eventmesh.dashboard.common.enums.MetadataType;
import org.apache.eventmesh.dashboard.common.enums.message.TopicType;
import org.apache.eventmesh.dashboard.console.entity.message.TopicEntity;
import org.apache.eventmesh.dashboard.console.utils.data.adaptation.Adaptation;

import java.util.Objects;

/**
 * @author hahaha
 */
public interface TopicAdaptation<V> extends Adaptation<TopicEntity, V> {

    @Override
    default MetadataType metadataType() {
        return MetadataType.TOPIC;
    }

    @Override
    default void fill(TopicEntity t) {
        if (Objects.isNull(t.getWriteQueueNum())) {
            t.setWriteQueueNum(0);
        }
        if (Objects.isNull(t.getTopicType())) {
            t.setTopicType(TopicType.NORMAL.name());
        }
        if (Objects.isNull(t.getOrder())){
            t.setOrder(0);
        }
        if (Objects.isNull(t.getReplicationFactor())) {
            t.setReplicationFactor(1);
        }
        t.setCreateProgress(1);
        if (Objects.isNull(t.getRetentionMs())) {
            t.setRetentionMs(3600*24*3L);
        }
    }
}
