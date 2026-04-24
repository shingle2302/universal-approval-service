package com.universal.approval.listener;

import com.universal.approval.command.MultiInstanceRejectWithCompensationCommand;
import org.flowable.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MultiInstanceRejectListener {
    private static final Logger log = LoggerFactory.getLogger(MultiInstanceRejectListener.class);

    private final RuntimeService runtimeService;

    public MultiInstanceRejectListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void onRejected(String processInstanceId, String result) {
        if ("REJECT".equalsIgnoreCase(result)) {
            log.info("Multi-instance reject detected: processInstanceId={}", processInstanceId);
            new MultiInstanceRejectWithCompensationCommand(runtimeService, processInstanceId).execute();
        }
    }
}
