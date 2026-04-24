package com.universal.approval.event;

public interface ApprovalMessagePublisher {
    void publish(ApprovalEvent event);
}