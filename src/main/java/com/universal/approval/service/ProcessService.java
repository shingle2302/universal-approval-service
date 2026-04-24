package com.universal.approval.service;

import com.universal.approval.event.ApprovalStartEvent;
import com.universal.approval.event.ApprovalCompletedEvent;
import com.universal.approval.event.ApprovalEndEvent;
import com.universal.approval.event.ApprovalMessagePublisher;
import com.universal.approval.entity.BizApprovalLog;
import com.universal.approval.mapper.BizApprovalLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
public class ProcessService {
    
    @Resource
    private RuntimeService runtimeService;
    
    @Resource
    private TaskService taskService;
    
    @Resource
    private ApprovalMessagePublisher messagePublisher;
    
    @Resource
    private BizApprovalLogMapper approvalLogMapper;
    
    @Transactional
    public String startProcess(String templateCode, String businessKey, Map<String, Object> variables) {
        // 启动流程实例
        String processInstanceId = runtimeService.startProcessInstanceByKey(templateCode, businessKey, variables).getId();
        
        // 记录审批日志
        BizApprovalLog log = new BizApprovalLog();
        log.setTemplateCode(templateCode);
        log.setBusinessKey(businessKey);
        log.setProcessInstanceId(processInstanceId);
        log.setOperation("START");
        log.setStatus("RUNNING");
        log.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(log);
        
        // 发布流程启动事件
        messagePublisher.publish(new ApprovalStartEvent(templateCode, businessKey, processInstanceId));
        
        log.info("Process started: templateCode={}, businessKey={}, processInstanceId={}", templateCode, businessKey, processInstanceId);
        return processInstanceId;
    }
    
    public List<Task> getTasksByAssignee(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }
    
    @Transactional
    public void completeTask(String taskId, String assignee, String result, String comments, Map<String, Object> variables) {
        // 获取任务信息
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }
        
        // 获取流程实例信息
        String processInstanceId = task.getProcessInstanceId();
        String businessKey = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getBusinessKey();
        String templateCode = task.getProcessDefinitionId().split(':')[0];
        
        // 完成任务
        variables.put("approve_result", result);
        taskService.complete(taskId, variables);
        
        // 记录审批日志
        BizApprovalLog log = new BizApprovalLog();
        log.setTemplateCode(templateCode);
        log.setBusinessKey(businessKey);
        log.setProcessInstanceId(processInstanceId);
        log.setTaskId(taskId);
        log.setOperation("COMPLETE");
        log.setOperator(assignee);
        log.setComments(comments);
        log.setStatus(result);
        log.setCreateTime(LocalDateTime.now());
        approvalLogMapper.insert(log);
        
        // 发布任务完成事件
        messagePublisher.publish(new ApprovalCompletedEvent(templateCode, businessKey, processInstanceId, taskId, assignee, result, comments));
        
        // 检查流程是否结束
        if (runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count() == 0) {
            // 流程已结束，发布流程结束事件
            messagePublisher.publish(new ApprovalEndEvent(templateCode, businessKey, processInstanceId, result));
        }
        
        log.info("Task completed: taskId={}, assignee={}, result={}", taskId, assignee, result);
    }
    
    public void delegateTask(String taskId, String assignee, String delegateTo) {
        taskService.delegateTask(taskId, delegateTo);
        log.info("Task delegated: taskId={}, from={}, to={}", taskId, assignee, delegateTo);
    }
    
    public Object getProcessVariables(String processInstanceId) {
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