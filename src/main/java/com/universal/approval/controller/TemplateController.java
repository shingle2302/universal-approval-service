package com.universal.approval.controller;

import com.universal.approval.entity.BizApprovalTemplate;
import com.universal.approval.mapper.BizApprovalTemplateMapper;
import com.universal.approval.model.JsonTemplate;
import com.universal.approval.service.BpmnGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/approval/template")
public class TemplateController {
    
    @Resource
    private BizApprovalTemplateMapper templateMapper;
    
    @Resource
    private BpmnGeneratorService bpmnGeneratorService;
    
    @Resource
    private RepositoryService repositoryService;
    
    @PostMapping
    public ApiResponse<BizApprovalTemplate> createTemplate(@RequestBody JsonTemplate jsonTemplate) {
        // 转换为 BPMN 模型
        var bpmnModel = bpmnGeneratorService.convertToBpmnModel(jsonTemplate);
        
        // 部署流程
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(jsonTemplate.getTemplateCode() + ".bpmn20.xml", bpmnModel)
                .name(jsonTemplate.getTemplateName())
                .deploy();
        
        // 保存模板信息
        BizApprovalTemplate template = new BizApprovalTemplate();
        template.setTemplateCode(jsonTemplate.getTemplateCode());
        template.setTemplateName(jsonTemplate.getTemplateName());
        template.setVersion(jsonTemplate.getVersion());
        template.setConfigJson("{}"); // TODO: 序列化 jsonTemplate 为 JSON
        template.setProcessDefinitionKey(jsonTemplate.getTemplateCode());
        template.setDeploymentId(deployment.getId());
        template.setStatus("DEPLOYED");
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        templateMapper.insert(template);
        log.info("Template created: {}", jsonTemplate.getTemplateCode());
        return ApiResponse.success(template);
    }
    
    @GetMapping("/{code}")
    public ApiResponse<BizApprovalTemplate> getTemplate(@PathVariable String code) {
        var template = templateMapper.selectOne(null); // TODO: 根据 code 查询
        if (template == null) {
            return ApiResponse.error(404, "Template not found");
        }
        return ApiResponse.success(template);
    }
    
    @PutMapping("/{code}")
    public ApiResponse<BizApprovalTemplate> updateTemplate(@PathVariable String code, @RequestBody JsonTemplate jsonTemplate) {
        // TODO: 实现模板更新逻辑
        return ApiResponse.success(null);
    }
    
    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteTemplate(@PathVariable String code) {
        // TODO: 实现模板删除逻辑
        return ApiResponse.success();
    }
    
    @PostMapping("/{code}/deploy")
    public ApiResponse<Void> deployTemplate(@PathVariable String code) {
        // TODO: 实现模板部署逻辑
        return ApiResponse.success();
    }
    
    @GetMapping
    public ApiResponse<List<BizApprovalTemplate>> listTemplates() {
        var templates = templateMapper.selectList(null);
        return ApiResponse.success(templates);
    }
}