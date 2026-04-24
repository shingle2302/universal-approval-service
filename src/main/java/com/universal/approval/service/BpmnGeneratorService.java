package com.universal.approval.service;

import com.universal.approval.model.JsonTemplate;
import com.universal.approval.model.NodeConfig;
import com.universal.approval.model.ConditionConfig;
import org.flowable.bpmn.model.*;
import org.springframework.stereotype.Service;

@Service
public class BpmnGeneratorService {
    
    public BpmnModel convertToBpmnModel(JsonTemplate template) {
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        process.setId(template.getTemplateCode());
        process.setName(template.getTemplateName());
        model.addProcess(process);

        // 1. 第一遍遍历：创建所有基础节点
        for (NodeConfig node : template.getNodes()) {
            FlowElement element = createFlowElement(node);
            process.addFlowElement(element);
        }

        // 2. 第二遍遍历：构建连线 (SequenceFlow)
        for (NodeConfig node : template.getNodes()) {
            if (node.getNext() != null) {
                // 普通连线
                process.addFlowElement(new SequenceFlow(node.getId(), node.getNext()));
            } else if (node.getConditions() != null) {
                // 网关分支连线
                for (ConditionConfig cond : node.getConditions()) {
                    SequenceFlow flow = new SequenceFlow(node.getId(), cond.getNext());
                    // 注入动态变量表达式
                    flow.setConditionExpression(cond.getExpression());
                    process.addFlowElement(flow);
                }
            }
        }

        // 3. 关键：自动计算节点坐标
        new BpmnAutoLayout(model).execute();
        return model;
    }

    private FlowElement createFlowElement(NodeConfig config) {
        switch (config.getType()) {
            case "start":
                return new StartEvent() {{ setId(config.getId()); }};
            case "end":
                return new EndEvent() {{ setId(config.getId()); }};
            case "userTask":
                UserTask task = new UserTask();
                task.setId(config.getId());
                task.setName(config.getName());
                task.setAssignee(config.getAssignee());
                return task;
            case "exclusiveGateway":
                ExclusiveGateway gateway = new ExclusiveGateway();
                gateway.setId(config.getId());
                gateway.setName(config.getName());
                return gateway;
            default:
                throw new IllegalArgumentException("Unknown node type: " + config.getType());
        }
    }
}