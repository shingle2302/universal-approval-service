package com.universal.approval.controller;

import com.universal.approval.entity.BizApprovalTemplate;
import com.universal.approval.mapper.BizApprovalTemplateMapper;
import com.universal.approval.service.BpmnGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/approval/modeler")
public class FlowableModelerController {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private BizApprovalTemplateMapper templateMapper;

    @Resource
    private BpmnGeneratorService bpmnGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/models")
    public Map<String, Object> getModels() {
        List<Map<String, Object>> models = new ArrayList<>();
        repositoryService.createModelQuery().list().forEach(model -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", model.getId());
            m.put("name", model.getName());
            m.put("key", model.getKey());
            m.put("version", model.getVersion());
            m.put("createTime", model.getCreateTime());
            m.put("lastUpdateTime", model.getLastUpdateTime());
            models.add(m);
        });
        return Map.of("code", 200, "data", models);
    }

    @GetMapping("/process-definitions")
    public Map<String, Object> getProcessDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        repositoryService.createProcessDefinitionQuery().list().forEach(pd -> {
            Map<String, Object> d = new HashMap<>();
            d.put("id", pd.getId());
            d.put("key", pd.getKey());
            d.put("name", pd.getName());
            d.put("version", pd.getVersion());
            d.put("deploymentId", pd.getDeploymentId());
            d.put("resourceName", pd.getResourceName());
            d.put("isSuspended", pd.isSuspended());
            definitions.add(d);
        });
        return Map.of("code", 200, "data", definitions);
    }

    @GetMapping("/deployments")
    public Map<String, Object> getDeployments() {
        List<Map<String, Object>> deployments = new ArrayList<>();
        repositoryService.createDeploymentQuery().list().forEach(d -> {
            Map<String, Object> dep = new HashMap<>();
            dep.put("id", d.getId());
            dep.put("name", d.getName());
            dep.put("key", d.getKey());
            dep.put("deploymentTime", d.getDeploymentTime());
            deployments.add(dep);
        });
        return Map.of("code", 200, "data", deployments);
    }

    @DeleteMapping("/deployment/{deploymentId}")
    public Map<String, Object> deleteDeployment(@PathVariable String deploymentId) {
        try {
            repositoryService.deleteDeployment(deploymentId, true);
            return Map.of("code", 200, "message", "删除成功");
        } catch (Exception e) {
            log.error("删除部署失败", e);
            return Map.of("code", 500, "message", "删除失败: " + e.getMessage());
        }
    }
}
