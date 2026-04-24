package com.universal.approval.command;

import org.flowable.engine.RuntimeService;

import java.util.List;

public class JumpToNodeCommand {
    private final RuntimeService runtimeService;
    private final String processInstanceId;
    private final String targetNodeKey;
    private final List<String> cleanVars;

    public JumpToNodeCommand(RuntimeService runtimeService, String processInstanceId, String targetNodeKey, List<String> cleanVars) {
        this.runtimeService = runtimeService;
        this.processInstanceId = processInstanceId;
        this.targetNodeKey = targetNodeKey;
        this.cleanVars = cleanVars;
    }

    public void execute() {
        if (cleanVars != null) {
            for (String var : cleanVars) {
                runtimeService.removeVariable(processInstanceId, var);
            }
        }
        runtimeService.setVariable(processInstanceId, "rollback_target_node", targetNodeKey);
    }
}
