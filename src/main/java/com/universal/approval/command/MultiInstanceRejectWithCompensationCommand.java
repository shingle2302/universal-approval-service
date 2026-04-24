package com.universal.approval.command;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.TaskEntity;

public class MultiInstanceRejectWithCompensationCommand implements Command<Void> {
    private String taskId;

    public MultiInstanceRejectWithCompensationCommand(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity task = commandContext.getTaskService().findById(taskId);
        if (task == null) {
            throw new FlowableException("Task not found: " + taskId);
        }

        String processInstanceId = task.getProcessInstanceId();
        new MultiInstanceJumpCommand(processInstanceId, "startNode").execute(commandContext);

        return null;
    }
}
