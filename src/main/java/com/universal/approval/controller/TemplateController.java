package com.universal.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universal.approval.entity.BizApprovalTemplate;
import com.universal.approval.mapper.BizApprovalTemplateMapper;
import com.universal.approval.model.JsonTemplate;
import com.universal.approval.service.BpmnGeneratorService;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/approval/template")
public class TemplateController {
    private static final Logger log = LoggerFactory.getLogger(TemplateController.class);

    @Resource
    private BizApprovalTemplateMapper templateMapper;

    @Resource
    private BpmnGeneratorService bpmnGeneratorService;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ObjectMapper objectMapper;

    @PostMapping
    public ApiResponse<BizApprovalTemplate> createTemplate(@RequestBody JsonTemplate jsonTemplate) {
        BizApprovalTemplate existing = getTemplateByCode(jsonTemplate.getTemplateCode());
        if (existing != null) {
            return ApiResponse.error(409, "Template already exists: " + jsonTemplate.getTemplateCode());
        }

        BizApprovalTemplate template = buildTemplateEntity(jsonTemplate);
        template.setStatus("CREATED");
        templateMapper.insert(template);

        log.info("Template created: {}", jsonTemplate.getTemplateCode());
        return ApiResponse.success(template);
    }

    @GetMapping("/{code}")
    public ApiResponse<BizApprovalTemplate> getTemplate(@PathVariable String code) {
        BizApprovalTemplate template = getTemplateByCode(code);
        if (template == null) {
            return ApiResponse.error(404, "Template not found");
        }
        return ApiResponse.success(template);
    }

    @PutMapping("/{code}")
    public ApiResponse<BizApprovalTemplate> updateTemplate(@PathVariable String code, @RequestBody JsonTemplate jsonTemplate) {
        BizApprovalTemplate existing = getTemplateByCode(code);
        if (existing == null) {
            return ApiResponse.error(404, "Template not found");
        }

        existing.setTemplateName(jsonTemplate.getTemplateName());
        existing.setVersion(jsonTemplate.getVersion());
        existing.setConfigJson(writeConfig(jsonTemplate));
        existing.setUpdateTime(LocalDateTime.now());
        existing.setStatus("UPDATED");
        templateMapper.updateById(existing);

        return ApiResponse.success(existing);
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteTemplate(@PathVariable String code) {
        BizApprovalTemplate existing = getTemplateByCode(code);
        if (existing == null) {
            return ApiResponse.error(404, "Template not found");
        }

        if (existing.getDeploymentId() != null && !existing.getDeploymentId().isBlank()) {
            repositoryService.deleteDeployment(existing.getDeploymentId(), true);
        }
        templateMapper.deleteById(existing.getId());
        return ApiResponse.success();
    }

    @PostMapping("/{code}/deploy")
    public ApiResponse<BizApprovalTemplate> deployTemplate(@PathVariable String code) {
        BizApprovalTemplate existing = getTemplateByCode(code);
        if (existing == null) {
            return ApiResponse.error(404, "Template not found");
        }

        JsonTemplate jsonTemplate;
        try {
            jsonTemplate = objectMapper.readValue(existing.getConfigJson(), JsonTemplate.class);
        } catch (JsonProcessingException e) {
            return ApiResponse.error(400, "Template config parse failed: " + e.getMessage());
        }

        var bpmnModel = bpmnGeneratorService.convertToBpmnModel(jsonTemplate);
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(jsonTemplate.getTemplateCode() + ".bpmn20.xml", bpmnModel)
                .name(jsonTemplate.getTemplateName())
                .deploy();

        existing.setProcessDefinitionKey(jsonTemplate.getTemplateCode());
        existing.setDeploymentId(deployment.getId());
        existing.setStatus("DEPLOYED");
        existing.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(existing);

        return ApiResponse.success(existing);
    }

    @GetMapping
    public ApiResponse<List<BizApprovalTemplate>> listTemplates() {
        return ApiResponse.success(templateMapper.selectList(new LambdaQueryWrapper<>()));
    }

    private BizApprovalTemplate buildTemplateEntity(JsonTemplate jsonTemplate) {
        BizApprovalTemplate template = new BizApprovalTemplate();
        template.setTemplateCode(jsonTemplate.getTemplateCode());
        template.setTemplateName(jsonTemplate.getTemplateName());
        template.setVersion(jsonTemplate.getVersion());
        template.setConfigJson(writeConfig(jsonTemplate));
        template.setProcessDefinitionKey(jsonTemplate.getTemplateCode());
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        return template;
    }

    private BizApprovalTemplate getTemplateByCode(String code) {
        return templateMapper.selectOne(new LambdaQueryWrapper<BizApprovalTemplate>()
                .eq(BizApprovalTemplate::getTemplateCode, code)
                .last("limit 1"));
    }

    private String writeConfig(JsonTemplate jsonTemplate) {
        try {
            return objectMapper.writeValueAsString(jsonTemplate);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Serialize template failed", e);
        }
    }
}
