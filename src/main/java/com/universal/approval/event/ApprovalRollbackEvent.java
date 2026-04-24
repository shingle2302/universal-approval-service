package com.universal.approval.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalRollbackEvent extends ApprovalEvent {
    private String taskId;
    private String targetNode;
    private String operator;
    
    public ApprovalRollbackEvent(String templateCode, String businessKey, String processInstanceId, String taskId, String targetNode, String operator) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_ROLLBACK");
        this.taskId = taskId;
        this.targetNode = targetNode;
        this.operator = operator;
    }
}