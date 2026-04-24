package com.universal.approval.event;

public class ApprovalCompletedEvent extends ApprovalEvent {
    private String taskId;
    private String assignee;
    private String result;
    private String comments;

    public ApprovalCompletedEvent(String templateCode, String businessKey, String processInstanceId,
                                  String taskId, String assignee, String result, String comments) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_COMPLETED");
        this.taskId = taskId;
        this.assignee = assignee;
        this.result = result;
        this.comments = comments;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
