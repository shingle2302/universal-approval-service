package com.universal.approval.event;

public class ApprovalStartEvent extends ApprovalEvent {
    public ApprovalStartEvent(String templateCode, String businessKey, String processInstanceId) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_START");
    }
}