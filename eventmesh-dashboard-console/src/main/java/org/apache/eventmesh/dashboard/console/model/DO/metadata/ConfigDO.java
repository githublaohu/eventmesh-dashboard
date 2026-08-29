package org.apache.eventmesh.dashboard.console.model.DO.metadata;

import org.apache.eventmesh.dashboard.console.entity.function.ConfigEntity;

import java.util.List;

import lombok.Data;

@Data
public class ConfigDO {

    private List<ConfigEntity> configEntityList;

}
