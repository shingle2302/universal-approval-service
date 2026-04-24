package com.universal.approval.service;

import com.universal.approval.model.JsonTemplate;
import com.universal.approval.model.GlobalConfig;
import com.universal.approval.model.NodeConfig;
import com.universal.approval.model.ConditionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.FlowElement;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class BpmnGeneratorServiceTest {
    
    @Autowired
    private BpmnGeneratorService bpmnGeneratorService;
    
    @Test
    public void testConvertToBpmnModel() {
        // 创建测试 JSON 模板
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("PURCHASE_001");
        template.setTemplateName("通用采购审批流");
        template.setVersion(1);
        
        // 设置全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        List<String> resetVars = new ArrayList<>();
        resetVars.add("is_urgent");
        resetVars.add("audit_remark");
        globalConfig.setAutoResetVarsOnReject(resetVars);
        globalConfig.setDataSource("springBean:flowTools");
        template.setGlobalConfigs(globalConfig);
        
        // 设置节点
        List<NodeConfig> nodes = new ArrayList<>();
        
        // 开始节点
        NodeConfig startNode = new NodeConfig();
        startNode.setId("startNode");
        startNode.setType("start");
        startNode.setNext("managerTask");
        nodes.add(startNode);
        
        // 主管审批节点
        NodeConfig managerTask = new NodeConfig();
        managerTask.setId("managerTask");
        managerTask.setName("主管审批");
        managerTask.setType("userTask");
        managerTask.setAssignee("${deptManager}");
        managerTask.setAllowReject(true);
        managerTask.setRejectTo("startNode");
        managerTask.setNext("amountGateway");
        nodes.add(managerTask);
        
        // 金额判定网关
        NodeConfig amountGateway = new NodeConfig();
        amountGateway.setId("amountGateway");
        amountGateway.setName("金额判定");
        amountGateway.setType("exclusiveGateway");
        
        List<ConditionConfig> conditions = new ArrayList<>();
        ConditionConfig condition1 = new ConditionConfig();
        condition1.setExpression("${flowTools.getAmount(execution) > 5000}");
        condition1.setNext("directorTask");
        conditions.add(condition1);
        
        ConditionConfig condition2 = new ConditionConfig();
        condition2.setExpression("${flowTools.getAmount(execution) <= 5000}");
        condition2.setNext("endNode");
        conditions.add(condition2);
        
        amountGateway.setConditions(conditions);
        nodes.add(amountGateway);
        
        // 总监审批节点
        NodeConfig directorTask = new NodeConfig();
        directorTask.setId("directorTask");
        directorTask.setName("总监审批");
        directorTask.setType("userTask");
        directorTask.setAssignee("ROLE_DIRECTOR");
        directorTask.setNext("endNode");
        nodes.add(directorTask);
        
        // 结束节点
        NodeConfig endNode = new NodeConfig();
        endNode.setId("endNode");
        endNode.setType("end");
        nodes.add(endNode);
        
        template.setNodes(nodes);
        
        // 转换为 BPMN 模型
        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);
        
        // 验证转换结果
        assert bpmnModel != null;
        assert bpmnModel.getProcesses().size() == 1;
        
        Process process = bpmnModel.getProcesses().get(0);
        assert process.getId().equals("PURCHASE_001");
        assert process.getName().equals("通用采购审批流");
        
        // 验证节点
        FlowElement startElement = process.getFlowElement("startNode");
        assert startElement != null;
        
        FlowElement managerElement = process.getFlowElement("managerTask");
        assert managerElement != null;
        
        FlowElement gatewayElement = process.getFlowElement("amountGateway");
        assert gatewayElement != null;
        
        FlowElement directorElement = process.getFlowElement("directorTask");
        assert directorElement != null;
        
        FlowElement endElement = process.getFlowElement("endNode");
        assert endElement != null;
        
        System.out.println("BPMN model converted successfully");
    }
}