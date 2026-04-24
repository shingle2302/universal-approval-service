package com.universal.approval.model;

import java.util.List;

public class NodeConfig {
    private String id;
    private String name;
    private String type;
    private String assignee;
    private Boolean allowReject;
    private String rejectTo;
    private String next;
    private List<ConditionConfig> conditions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public Boolean getAllowReject() { return allowReject; }
    public void setAllowReject(Boolean allowReject) { this.allowReject = allowReject; }
    public String getRejectTo() { return rejectTo; }
    public void setRejectTo(String rejectTo) { this.rejectTo = rejectTo; }
    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }
    public List<ConditionConfig> getConditions() { return conditions; }
    public void setConditions(List<ConditionConfig> conditions) { this.conditions = conditions; }
}
