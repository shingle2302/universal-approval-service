package com.universal.approval.event;

public class ApprovalRollbackEvent extends ApprovalEvent {
    private String taskId;
    private String targetNode;
    private String operator;

    public ApprovalRollbackEvent(String templateCode, String businessKey, String processInstanceId,
                                 String taskId, String targetNode, String operator) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_ROLLBACK");
        this.taskId = taskId;
        this.targetNode = targetNode;
        this.operator = operator;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTargetNode() { return targetNode; }
    public void setTargetNode(String targetNode) { this.targetNode = targetNode; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}
