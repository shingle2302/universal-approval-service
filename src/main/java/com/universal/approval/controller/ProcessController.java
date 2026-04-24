package com.universal.approval.controller;

import com.universal.approval.service.ProcessService;
import jakarta.annotation.Resource;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/approval/process")
public class ProcessController {

    @Resource
    private ProcessService processService;

    @Resource
    private RuntimeService runtimeService;

    @PostMapping
    public ApiResponse<String> startProcess(@RequestBody ProcessStartRequest request) {
        String processInstanceId = processService.startProcess(
                request.getTemplateCode(),
                request.getBusinessKey(),
                request.getVariables()
        );
        return ApiResponse.success(processInstanceId);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProcessInstance> getProcess(@PathVariable String id) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(id)
                .singleResult();
        if (processInstance == null) {
            return ApiResponse.error(404, "Process instance not found");
        }
        return ApiResponse.success(processInstance);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProcess(@PathVariable String id,
                                           @RequestParam(defaultValue = "manual_terminate") String reason) {
        processService.deleteProcessInstance(id, reason);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/variables")
    public ApiResponse<Map<String, Object>> getProcessVariables(@PathVariable String id) {
        return ApiResponse.success(processService.getProcessVariables(id));
    }

    @PutMapping("/{id}/variables")
    public ApiResponse<Void> updateProcessVariables(@PathVariable String id, @RequestBody Map<String, Object> variables) {
        processService.updateProcessVariables(id, variables);
        return ApiResponse.success();
    }

    public static class ProcessStartRequest {
        private String templateCode;
        private String businessKey;
        private Map<String, Object> variables;

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getBusinessKey() {
            return businessKey;
        }

        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }
}
