package com.universal.approval.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalCompletedEvent extends ApprovalEvent {
    private String taskId;
    private String assignee;
    private String result;
    private String comments;
    
    public ApprovalCompletedEvent(String templateCode, String businessKey, String processInstanceId, String taskId, String assignee, String result, String comments) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_COMPLETED");
        this.taskId = taskId;
        this.assignee = assignee;
        this.result = result;
        this.comments = comments;
    }
}