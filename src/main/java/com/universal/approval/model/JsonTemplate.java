package com.universal.approval.model;

import lombok.Data;
import java.util.List;

@Data
public class JsonTemplate {
    private String templateCode;
    private String templateName;
    private Integer version;
    private GlobalConfig globalConfigs;
    private List<NodeConfig> nodes;
}