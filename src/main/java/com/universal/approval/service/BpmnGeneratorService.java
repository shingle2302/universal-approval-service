package com.universal.approval.service;

import com.universal.approval.model.ConditionConfig;
import com.universal.approval.model.JsonTemplate;
import com.universal.approval.model.NodeConfig;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BpmnGeneratorService {

    public BpmnModel convertToBpmnModel(JsonTemplate template) {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId(template.getTemplateCode());
        process.setName(template.getTemplateName());
        model.addProcess(process);

        for (NodeConfig node : template.getNodes()) {
            FlowElement element = createFlowElement(node);
            process.addFlowElement(element);
        }

        for (NodeConfig node : template.getNodes()) {
            if (node.getNextNodes() != null && !node.getNextNodes().isEmpty()) {
                for (Map.Entry<String, String> entry : node.getNextNodes().entrySet()) {
                    String targetNode = entry.getValue();
                    if (targetNode != null && !targetNode.isEmpty()) {
                        SequenceFlow flow = new SequenceFlow(node.getNodeId(), targetNode);
                        if (node.getConditions() != null && node.getConditions().containsKey(entry.getKey())) {
                            ConditionConfig cond = node.getConditions().get(entry.getKey());
                            if (cond != null && cond.getExpression() != null) {
                                flow.setConditionExpression(cond.getExpression());
                            }
                        }
                        process.addFlowElement(flow);
                    }
                }
            }
        }

        return model;
    }

    private FlowElement createFlowElement(NodeConfig config) {
        switch (config.getNodeType()) {
            case "startEvent": {
                StartEvent startEvent = new StartEvent();
                startEvent.setId(config.getNodeId());
                startEvent.setName(config.getNodeName());
                return startEvent;
            }
            case "endEvent": {
                EndEvent endEvent = new EndEvent();
                endEvent.setId(config.getNodeId());
                endEvent.setName(config.getNodeName());
                return endEvent;
            }
            case "userTask": {
                UserTask task = new UserTask();
                task.setId(config.getNodeId());
                task.setName(config.getNodeName());
                if (config.getAssigneeValue() != null && !config.getAssigneeValue().isEmpty()) {
                    String assignee = config.getAssigneeValue();
                    task.setAssignee(assignee);
                }
                return task;
            }
            case "exclusiveGateway": {
                ExclusiveGateway gateway = new ExclusiveGateway();
                gateway.setId(config.getNodeId());
                gateway.setName(config.getNodeName());
                return gateway;
            }
            default:
                throw new IllegalArgumentException("Unknown node type: " + config.getNodeType());
        }
    }
}
