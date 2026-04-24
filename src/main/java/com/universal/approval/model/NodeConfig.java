package com.universal.approval.model;

import lombok.Data;
import java.util.List;

@Data
public class NodeConfig {
    private String id;
    private String name;
    private String type;
    private String assignee;
    private Boolean allowReject;
    private String rejectTo;
    private String next;
    private List<ConditionConfig> conditions;
}