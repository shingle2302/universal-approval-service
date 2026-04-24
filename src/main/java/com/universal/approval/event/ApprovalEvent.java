package com.universal.approval.event;

import lombok.Data;

@Data
public abstract class ApprovalEvent {
    private String templateCode;
    private String businessKey;
    private String processInstanceId;
    private String eventType;
    
    public ApprovalEvent(String templateCode, String businessKey, String processInstanceId, String eventType) {
        this.templateCode = templateCode;
        this.businessKey = businessKey;
        this.processInstanceId = processInstanceId;
        this.eventType = eventType;
    }
}