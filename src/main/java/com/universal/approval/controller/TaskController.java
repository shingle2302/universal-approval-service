package com.universal.approval.controller;

import com.universal.approval.model.TaskDTO;
import com.universal.approval.service.ProcessService;
import jakarta.annotation.Resource;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/approval/task")
public class TaskController {

    @Resource
    private ProcessService processService;

    @GetMapping
    public ApiResponse<List<TaskDTO>> getTasks(@RequestParam(required = false) String assignee) {
        List<Task> tasks;
        if (assignee != null && !assignee.isEmpty()) {
            tasks = processService.getTasksByAssignee(assignee);
        } else {
            // 获取所有任务
            tasks = processService.getAllTasks();
        }
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(taskDTOs);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTask(@PathVariable String id) {
        Task task = processService.getTask(id);
        if (task == null) {
            return ApiResponse.error(404, "Task not found");
        }
        return ApiResponse.success(convertToTaskDTO(task));
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setName(task.getName());
        taskDTO.setAssignee(task.getAssignee());
        taskDTO.setProcessInstanceId(task.getProcessInstanceId());
        taskDTO.setProcessDefinitionId(task.getProcessDefinitionId());
        taskDTO.setCreateTime(task.getCreateTime());
        taskDTO.setDueDate(task.getDueDate());
        return taskDTO;
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
        processService.rollbackTask(id, request.getAssignee(), request.getTargetNode());
        return ApiResponse.success();
    }

    public static class TaskCompleteRequest {
        private String assignee;
        private String result;
        private String comments;
        private Map<String, Object> variables;

        public String getAssignee() { return assignee; }
        public void setAssignee(String assignee) { this.assignee = assignee; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    }

    public static class TaskDelegateRequest {
        private String assignee;
        private String delegateTo;

        public String getAssignee() { return assignee; }
        public void setAssignee(String assignee) { this.assignee = assignee; }
        public String getDelegateTo() { return delegateTo; }
        public void setDelegateTo(String delegateTo) { this.delegateTo = delegateTo; }
    }

    public static class TaskRollbackRequest {
        private String assignee;
        private String targetNode;

        public String getAssignee() { return assignee; }
        public void setAssignee(String assignee) { this.assignee = assignee; }
        public String getTargetNode() { return targetNode; }
        public void setTargetNode(String targetNode) { this.targetNode = targetNode; }
    }
}
