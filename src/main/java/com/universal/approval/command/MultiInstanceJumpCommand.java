package com.universal.approval.command;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;

import java.util.List;

public class MultiInstanceJumpCommand implements Command<Void> {
    private String processInstanceId;
    private String targetNodeKey;

    public MultiInstanceJumpCommand(String processInstanceId, String targetNodeKey) {
        this.processInstanceId = processInstanceId;
        this.targetNodeKey = targetNodeKey;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        List<ExecutionEntity> executions = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstanceId);

        for (ExecutionEntity exec : executions) {
            executionEntityManager.deleteExecutionAndRelatedData(exec, "MultiInstance_Reject", false);
        }

        ExecutionEntity rootExecution = executionEntityManager.findById(processInstanceId);
        if (rootExecution == null) {
            throw new FlowableException("Process instance not found: " + processInstanceId);
        }

        Process process = ProcessDefinitionUtil.getProcess(rootExecution.getProcessDefinitionId());
        if (process == null) {
            throw new FlowableException("Process definition not found");
        }

        FlowElement targetElement = process.getFlowElement(targetNodeKey);
        if (targetElement == null) {
            throw new FlowableException("Target node not found: " + targetNodeKey);
        }

        rootExecution.setCurrentFlowElement(targetElement);
        commandContext.getAgenda().planContinueProcessOperation(rootExecution);

        return null;
    }
}
