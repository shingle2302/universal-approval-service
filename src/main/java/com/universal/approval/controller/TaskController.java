package com.universal.approval.controller;

import com.universal.approval.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/approval/task")
public class TaskController {
    
    @Resource
    private ProcessService processService;
    
    @GetMapping
    public ApiResponse<List<Task>> getTasks(@RequestParam String assignee) {
        List<Task> tasks = processService.getTasksByAssignee(assignee);
        return ApiResponse.success(tasks);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Task> getTask(@PathVariable String id) {
        // TODO: 实现获取任务详情的逻辑
        return ApiResponse.success(null);
    }
    
    @PostMapping("/{id}")
    public ApiResponse<Void> completeTask(@PathVariable String id, @RequestBody TaskCompleteRequest request) {
        processService.completeTask(
                id,
                request.getAssignee(),
                request.getResult(),
                request.getComments(),
                request.getVariables()
        );
        return ApiResponse.success();
    }
    
    @PostMapping("/{id}/delegate")
    public ApiResponse<Void> delegateTask(@PathVariable String id, @RequestBody TaskDelegateRequest request) {
        processService.delegateTask(id, request.getAssignee(), request.getDelegateTo());
        return ApiResponse.success();
    }
    
    @PostMapping("/{id}/rollback")
    public ApiResponse<Void> rollbackTask(@PathVariable String id, @RequestBody TaskRollbackRequest request) {
        // TODO: 实现任务回退逻辑
        return ApiResponse.success();
    }
    
    // 任务完成请求类
    public static class TaskCompleteRequest {
        private String assignee;
        private String result;
        private String comments;
        private Map<String, Object> variables;
        
        // getters and setters
        public String getAssignee() {
            return assignee;
        }
        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }
        public String getResult() {
            return result;
        }
        public void setResult(String result) {
            this.result = result;
        }
        public String getComments() {
            return comments;
        }
        public void setComments(String comments) {
            this.comments = comments;
        }
        public Map<String, Object> getVariables() {
            return variables;
        }
        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }
    
    // 任务委派请求类
    public static class TaskDelegateRequest {
        private String assignee;
        private String delegateTo;
        
        // getters and setters
        public String getAssignee() {
            return assignee;
        }
        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }
        public String getDelegateTo() {
            return delegateTo;
        }
        public void setDelegateTo(String delegateTo) {
            this.delegateTo = delegateTo;
        }
    }
    
    // 任务回退请求类
    public static class TaskRollbackRequest {
        private String assignee;
        private String targetNode;
        
        // getters and setters
        public String getAssignee() {
            return assignee;
        }
        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }
        public String getTargetNode() {
            return targetNode;
        }
        public void setTargetNode(String targetNode) {
            this.targetNode = targetNode;
        }
    }
}