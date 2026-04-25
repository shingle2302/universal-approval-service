package com.universal.approval.command;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;

import java.util.List;

public class JumpToNodeCommand implements Command<Void> {
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

    @Override
    public Void execute(CommandContext commandContext) {
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .onlyChildExecutions()
                .list();

        if (executions.isEmpty()) {
            throw new FlowableException("No executions found for process instance: " + processInstanceId);
        }

        Execution execution = executions.get(0);

        if (cleanVars != null) {
            for (String var : cleanVars) {
                runtimeService.removeVariable(processInstanceId, var);
            }
        }
        runtimeService.setVariable(processInstanceId, "rollback_target_node", targetNodeKey);
        runtimeService.trigger(execution.getId());

        return null;
    }
}