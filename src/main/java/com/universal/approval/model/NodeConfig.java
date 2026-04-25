package com.universal.approval.model;

import java.util.Map;

public class NodeConfig {
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String assigneeType;
    private String assigneeValue;
    private Boolean allowReject;
    private String rejectTo;
    private Map<String, String> nextNodes;
    private Map<String, ConditionConfig> conditions;

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getAssigneeType() { return assigneeType; }
    public void setAssigneeType(String assigneeType) { this.assigneeType = assigneeType; }
    public String getAssigneeValue() { return assigneeValue; }
    public void setAssigneeValue(String assigneeValue) { this.assigneeValue = assigneeValue; }
    public Boolean getAllowReject() { return allowReject; }
    public void setAllowReject(Boolean allowReject) { this.allowReject = allowReject; }
    public String getRejectTo() { return rejectTo; }
    public void setRejectTo(String rejectTo) { this.rejectTo = rejectTo; }
    public Map<String, String> getNextNodes() { return nextNodes; }
    public void setNextNodes(Map<String, String> nextNodes) { this.nextNodes = nextNodes; }
    public Map<String, ConditionConfig> getConditions() { return conditions; }
    public void setConditions(Map<String, ConditionConfig> conditions) { this.conditions = conditions; }
}
