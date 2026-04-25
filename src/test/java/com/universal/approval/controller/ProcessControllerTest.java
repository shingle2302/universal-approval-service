package com.universal.approval.controller;

import com.universal.approval.service.ProcessService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProcessControllerTest {

    private ProcessController processController;
    private ProcessService processService;
    private RuntimeService runtimeService;

    @BeforeEach
    void setUp() {
        processController = new ProcessController();
        processService = mock(ProcessService.class);
        runtimeService = mock(RuntimeService.class);
        ReflectionTestUtils.setField(processController, "processService", processService);
        ReflectionTestUtils.setField(processController, "runtimeService", runtimeService);
    }

    @Test
    void startProcess_shouldReturnProcessInstanceId() {
        ProcessController.ProcessStartRequest request = new ProcessController.ProcessStartRequest();
        request.setTemplateCode("LEAVE_001");
        request.setBusinessKey("BK-001");
        request.setVariables(Map.of("days", 3));

        when(processService.startProcess("LEAVE_001", "BK-001", Map.of("days", 3))).thenReturn("pi-001");

        ApiResponse<String> response = processController.startProcess(request);

        assertEquals(200, response.getCode());
        assertEquals("pi-001", response.getData());
    }

    @Test
    void getProcessByBusinessKey_shouldUseRuntimeQuery() {
        ProcessInstanceQuery query = mock(ProcessInstanceQuery.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
        when(query.processInstanceBusinessKey("BK-002")).thenReturn(query);
        when(query.list()).thenReturn(List.of(processInstance));
        when(processInstance.getId()).thenReturn("pi-002");
        when(processInstance.getBusinessKey()).thenReturn("BK-002");
        when(processInstance.getProcessDefinitionId()).thenReturn("leave:1:1");
        when(processInstance.getName()).thenReturn("请假流程");

        ApiResponse<List<ProcessController.ProcessInfo>> response = processController.getProcessByBusinessKey("BK-002");

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("pi-002", response.getData().get(0).getId());
    }

    @Test
    void updateProcessVariables_shouldCallService() {
        ApiResponse<Void> response = processController.updateProcessVariables("pi-888", Map.of("approved", true));

        assertEquals(200, response.getCode());
        verify(processService).updateProcessVariables("pi-888", Map.of("approved", true));
    }
}
