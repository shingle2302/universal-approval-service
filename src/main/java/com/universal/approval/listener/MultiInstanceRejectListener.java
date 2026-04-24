package com.universal.approval.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.interceptor.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.universal.approval.command.MultiInstanceRejectWithCompensationCommand;

@Slf4j
@Component
public class MultiInstanceRejectListener implements TaskListener {

    @Autowired
    private CommandExecutor commandExecutor;

    @Override
    public void notify(DelegateTask delegateTask) {
        String result = (String) delegateTask.getVariable("approve_result");

        if ("REJECT".equals(result)) {
            log.info("Multi-instance task rejected: taskId={}, processInstanceId={}",
                    delegateTask.getId(), delegateTask.getProcessInstanceId());

            commandExecutor.execute(
                new MultiInstanceRejectWithCompensationCommand(delegateTask.getId())
            );
        }
    }
}
