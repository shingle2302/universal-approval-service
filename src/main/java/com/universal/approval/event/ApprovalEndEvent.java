package com.universal.approval.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalEndEvent extends ApprovalEvent {
    private String finalResult;
    
    public ApprovalEndEvent(String templateCode, String businessKey, String processInstanceId, String finalResult) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_END");
        this.finalResult = finalResult;
    }
}