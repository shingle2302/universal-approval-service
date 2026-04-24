package com.universal.approval.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringApprovalMessagePublisher implements ApprovalMessagePublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public SpringApprovalMessagePublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void publish(ApprovalEvent event) {
        eventPublisher.publishEvent(event);
    }
}