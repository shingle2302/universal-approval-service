package com.universal.approval.command;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.TaskEntity;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;

import java.util.List;

public class JumpToNodeCommand implements Command<Void> {
    private String taskId;
    private String targetNodeKey;
    private List<String> cleanVars;

    public JumpToNodeCommand(String taskId, String targetNodeKey, List<String> cleanVars) {
        this.taskId = taskId;
        this.targetNodeKey = targetNodeKey;
        this.cleanVars = cleanVars;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity task = commandContext.getTaskService().findById(taskId);
        if (task == null) {
            throw new FlowableException("Task not found: " + taskId);
        }

        ExecutionEntity execution = task.getExecution();
        Process process = ProcessDefinitionUtil.getProcess(execution.getProcessDefinitionId());
        if (process == null) {
            throw new FlowableException("Process definition not found");
        }

        FlowElement targetElement = process.getFlowElement(targetNodeKey);
        if (targetElement == null) {
            throw new FlowableException("Target node not found: " + targetNodeKey);
        }

        if (cleanVars != null && !cleanVars.isEmpty()) {
            for (String var : cleanVars) {
                execution.setVariable(var, null);
            }
        }

        execution.setCurrentFlowElement(targetElement);
        commandContext.getTaskService().deleteTask(task, "jump_rollback", false);
        commandContext.getAgenda().planContinueProcessOperation(execution);

        return null;
    }
}
