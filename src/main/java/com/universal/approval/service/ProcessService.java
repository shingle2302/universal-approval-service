package com.universal.approval.service;

import com.universal.approval.command.JumpToNodeCommand;
import com.universal.approval.entity.BizApprovalLog;
import com.universal.approval.event.ApprovalCompletedEvent;
import com.universal.approval.event.ApprovalDelegateEvent;
import com.universal.approval.event.ApprovalEndEvent;
import com.universal.approval.event.ApprovalMessagePublisher;
import com.universal.approval.event.ApprovalRollbackEvent;
import com.universal.approval.event.ApprovalStartEvent;
import com.universal.approval.mapper.BizApprovalLogMapper;
import jakarta.annotation.Resource;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private ApprovalMessagePublisher messagePublisher;

    @Resource
    private BizApprovalLogMapper approvalLogMapper;

    @Resource
    private ProcessEngineConfiguration processEngineConfiguration;

    @Transactional
    public String startProcess(String templateCode, String businessKey, Map<String, Object> variables) {
        Map<String, Object> startVariables = variables == null ? Collections.emptyMap() : variables;
        String processInstanceId = runtimeService.startProcessInstanceByKey(templateCode, businessKey, startVariables).getId();

        BizApprovalLog approvalLog = new BizApprovalLog();
        approvalLog.setTemplateCode(templateCode);
        approvalLog.setBusinessKey(businessKey);
        approvalLog.setProcessInstanceId(processInstanceId);
        approvalLog.setOperation("START");
        approvalLog.setStatus("RUNNING");
        approvalLog.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(approvalLog);

        messagePublisher.publish(new ApprovalStartEvent(templateCode, businessKey, processInstanceId));
        log.info("Process started: templateCode={}, businessKey={}, processInstanceId={}", templateCode, businessKey, processInstanceId);
        return processInstanceId;
    }

    public List<Task> getTasksByAssignee(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public List<Task> getAllTasks() {
        return taskService.createTaskQuery().list();
    }

    public Task getTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Transactional
    public void completeTask(String taskId, String assignee, String result, String comments, Map<String, Object> variables) {
        Task task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        String processInstanceId = task.getProcessInstanceId();
        var processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String businessKey = processInstance == null ? "" : processInstance.getBusinessKey();
        String templateCode = task.getProcessDefinitionId().split(":")[0];

        Map<String, Object> completeVariables = variables == null ? new HashMap<>() : new HashMap<>(variables);
        completeVariables.put("approve_result", result);

        if (task.getDelegationState() != null && task.getDelegationState().toString().equals("PENDING")) {
            taskService.resolveTask(taskId, completeVariables);
        } else {
            taskService.complete(taskId, completeVariables);
        }

        BizApprovalLog approvalLog = new BizApprovalLog();
        approvalLog.setTemplateCode(templateCode);
        approvalLog.setBusinessKey(businessKey);
        approvalLog.setProcessInstanceId(processInstanceId);
        approvalLog.setTaskId(taskId);
        approvalLog.setOperation("COMPLETE");
        approvalLog.setOperator(assignee);
        approvalLog.setComments(comments);
        approvalLog.setStatus(result);
        approvalLog.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(approvalLog);

        messagePublisher.publish(new ApprovalCompletedEvent(templateCode, businessKey, processInstanceId, taskId, assignee, result, comments));

        if (runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count() == 0) {
            messagePublisher.publish(new ApprovalEndEvent(templateCode, businessKey, processInstanceId, result));
        }

        log.info("Task completed: taskId={}, assignee={}, result={}", taskId, assignee, result);
    }

    @Transactional
    public void delegateTask(String taskId, String assignee, String delegateTo) {
        Task task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        String processInstanceId = task.getProcessInstanceId();
        var processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String businessKey = processInstance == null ? "" : processInstance.getBusinessKey();
        String templateCode = task.getProcessDefinitionId().split(":")[0];

        taskService.setAssignee(taskId, delegateTo);

        BizApprovalLog approvalLog = new BizApprovalLog();
        approvalLog.setTemplateCode(templateCode);
        approvalLog.setBusinessKey(businessKey);
        approvalLog.setProcessInstanceId(processInstanceId);
        approvalLog.setTaskId(taskId);
        approvalLog.setOperation("DELEGATE");
        approvalLog.setOperator(assignee);
        approvalLog.setComments("委托给 " + delegateTo);
        approvalLog.setStatus("DELEGATED");
        approvalLog.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(approvalLog);

        messagePublisher.publish(new ApprovalDelegateEvent(templateCode, businessKey, processInstanceId, taskId, assignee, delegateTo));
        log.info("Task delegated: taskId={}, from={}, to={}", taskId, assignee, delegateTo);
    }

    @Transactional
    public void rollbackTask(String taskId, String operator, String targetNode) {
        Task task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        String processInstanceId = task.getProcessInstanceId();
        String templateCode = task.getProcessDefinitionId().split(":")[0];
        var processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String businessKey = processInstance == null ? "" : processInstance.getBusinessKey();

        taskService.deleteTask(taskId);
        processEngineConfiguration.getCommandExecutor().execute(new JumpToNodeCommand(
                runtimeService,
                processInstanceId,
                targetNode,
                null
        ));

        BizApprovalLog approvalLog = new BizApprovalLog();
        approvalLog.setTemplateCode(templateCode);
        approvalLog.setBusinessKey(businessKey);
        approvalLog.setProcessInstanceId(processInstanceId);
        approvalLog.setTaskId(taskId);
        approvalLog.setOperation("ROLLBACK");
        approvalLog.setOperator(operator);
        approvalLog.setComments("rollback to " + targetNode);
        approvalLog.setStatus("ROLLBACK");
        approvalLog.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(approvalLog);

        messagePublisher.publish(new ApprovalRollbackEvent(templateCode, businessKey, processInstanceId, taskId, targetNode, operator));
        log.info("Task rollback executed: taskId={}, operator={}, targetNode={}", taskId, operator, targetNode);
    }

    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    public void updateProcessVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
        log.info("Process variables updated: processInstanceId={}", processInstanceId);
    }

    public void deleteProcessInstance(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Process instance deleted: processInstanceId={}, reason={}", processInstanceId, reason);
    }
}
