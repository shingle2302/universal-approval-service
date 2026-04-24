package com.universal.approval.command;

import org.flowable.engine.RuntimeService;

public class MultiInstanceJumpCommand {
    private final RuntimeService runtimeService;
    private final String processInstanceId;
    private final String targetNodeKey;

    public MultiInstanceJumpCommand(RuntimeService runtimeService, String processInstanceId, String targetNodeKey) {
        this.runtimeService = runtimeService;
        this.processInstanceId = processInstanceId;
        this.targetNodeKey = targetNodeKey;
    }

    public void execute() {
        runtimeService.setVariable(processInstanceId, "multi_instance_reject_target", targetNodeKey);
    }
}
