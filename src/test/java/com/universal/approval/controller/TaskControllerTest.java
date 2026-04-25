package com.universal.approval.controller;

import com.universal.approval.service.ProcessService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    private TaskController taskController;
    private ProcessService processService;

    @BeforeEach
    void setUp() {
        taskController = new TaskController();
        processService = mock(ProcessService.class);
        ReflectionTestUtils.setField(taskController, "processService", processService);
    }

    @Test
    void getTasks_shouldReturnAllTasksWhenAssigneeIsEmpty() {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn("task-1");
        when(task.getName()).thenReturn("审批任务");
        when(task.getAssignee()).thenReturn("manager");

        when(processService.getAllTasks()).thenReturn(List.of(task));

        ApiResponse<List<com.universal.approval.model.TaskDTO>> response = taskController.getTasks(null);

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("task-1", response.getData().get(0).getId());
        verify(processService).getAllTasks();
        verify(processService, never()).getTasksByAssignee(anyString());
    }

    @Test
    void completeTask_shouldForwardRequestToService() {
        TaskController.TaskCompleteRequest request = new TaskController.TaskCompleteRequest();
        request.setAssignee("manager");
        request.setResult("approved");
        request.setComments("ok");
        request.setVariables(Map.of("amount", 1000));

        ApiResponse<Void> response = taskController.completeTask("task-99", request);

        assertEquals(200, response.getCode());
        verify(processService).completeTask("task-99", "manager", "approved", "ok", Map.of("amount", 1000));
    }

    @Test
    void delegateTask_shouldForwardRequestToService() {
        TaskController.TaskDelegateRequest request = new TaskController.TaskDelegateRequest();
        request.setAssignee("manager");
        request.setDelegateTo("director");

        ApiResponse<Void> response = taskController.delegateTask("task-2", request);

        assertEquals(200, response.getCode());
        verify(processService).delegateTask("task-2", "manager", "director");
    }
}
