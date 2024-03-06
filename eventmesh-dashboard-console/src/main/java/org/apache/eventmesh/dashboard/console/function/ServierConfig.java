package org.apache.eventmesh.dashboard.console.function;

import lombok.Data;

@Data
public class ServierConfig {

    private SeriverTypeEnums seriverTypeEnums;

    private String address;

    private Integer port;

}
