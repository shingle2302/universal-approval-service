package com.universal.approval.event;

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

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}
