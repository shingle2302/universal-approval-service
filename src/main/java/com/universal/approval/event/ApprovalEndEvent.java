package com.universal.approval.event;

public class ApprovalEndEvent extends ApprovalEvent {
    private String finalResult;

    public ApprovalEndEvent(String templateCode, String businessKey, String processInstanceId, String finalResult) {
        super(templateCode, businessKey, processInstanceId, "APPROVAL_END");
        this.finalResult = finalResult;
    }

    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
}
