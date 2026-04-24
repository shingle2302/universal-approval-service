package com.universal.approval.command;

import org.flowable.engine.RuntimeService;

public class MultiInstanceRejectWithCompensationCommand {
    private final RuntimeService runtimeService;
    private final String processInstanceId;

    public MultiInstanceRejectWithCompensationCommand(RuntimeService runtimeService, String processInstanceId) {
        this.runtimeService = runtimeService;
        this.processInstanceId = processInstanceId;
    }

    public void execute() {
        runtimeService.setVariable(processInstanceId, "compensation_required", true);
        new MultiInstanceJumpCommand(runtimeService, processInstanceId, "startNode").execute();
    }
}
