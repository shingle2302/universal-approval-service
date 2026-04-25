package com.universal.approval.event;

public class ApprovalDelegateEvent extends ApprovalEvent {
    private String taskId;
    private String fromAssignee;
    private String toAssignee;

    public ApprovalDelegateEvent(String templateCode, String businessKey, String processInstanceId,
                                  String taskId, String fromAssignee, String toAssignee) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_DELEGATE");
        this.taskId = taskId;
        this.fromAssignee = fromAssignee;
        this.toAssignee = toAssignee;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getFromAssignee() { return fromAssignee; }
    public void setFromAssignee(String fromAssignee) { this.fromAssignee = fromAssignee; }
    public String getToAssignee() { return toAssignee; }
    public void setToAssignee(String toAssignee) { this.toAssignee = toAssignee; }
}