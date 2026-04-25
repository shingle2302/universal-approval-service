package com.universal.approval.controller;

import com.universal.approval.service.ProcessService;
import jakarta.annotation.Resource;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping
    public ApiResponse<List<ProcessInfo>> getProcessByBusinessKey(@RequestParam(required = false) String businessKey) {
        List<ProcessInstance> processInstances;
        if (businessKey != null) {
            processInstances = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey)
                    .list();
        } else {
            // 获取所有流程实例
            processInstances = runtimeService.createProcessInstanceQuery()
                    .list();
        }
        List<ProcessInfo> processInfos = processInstances.stream()
                .map(pi -> new ProcessInfo(pi.getId(), pi.getBusinessKey(), pi.getProcessDefinitionId(), pi.getName()))
                .collect(Collectors.toList());
        return ApiResponse.success(processInfos);
    }

    public static class ProcessInfo {
        private String id;
        private String businessKey;
        private String processDefinitionId;
        private String name;

        public ProcessInfo() {}

        public ProcessInfo(String id, String businessKey, String processDefinitionId, String name) {
            this.id = id;
            this.businessKey = businessKey;
            this.processDefinitionId = processDefinitionId;
            this.name = name;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getBusinessKey() { return businessKey; }
        public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
        public String getProcessDefinitionId() { return processDefinitionId; }
        public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @GetMapping("/{id}")
    public ApiResponse<ProcessInfo> getProcess(@PathVariable String id) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(id)
                .singleResult();
        if (processInstance == null) {
            return ApiResponse.error(404, "Process instance not found");
        }
        ProcessInfo processInfo = new ProcessInfo(
                processInstance.getId(),
                processInstance.getBusinessKey(),
                processInstance.getProcessDefinitionId(),
                processInstance.getName()
        );
        return ApiResponse.success(processInfo);
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
