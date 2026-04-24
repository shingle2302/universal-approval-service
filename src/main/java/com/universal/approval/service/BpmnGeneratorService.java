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
            if (node.getNext() != null) {
                process.addFlowElement(new SequenceFlow(node.getId(), node.getNext()));
            } else if (node.getConditions() != null) {
                for (ConditionConfig cond : node.getConditions()) {
                    SequenceFlow flow = new SequenceFlow(node.getId(), cond.getNext());
                    flow.setConditionExpression(cond.getExpression());
                    process.addFlowElement(flow);
                }
            }
        }

        return model;
    }

    private FlowElement createFlowElement(NodeConfig config) {
        switch (config.getType()) {
            case "start": {
                StartEvent startEvent = new StartEvent();
                startEvent.setId(config.getId());
                return startEvent;
            }
            case "end": {
                EndEvent endEvent = new EndEvent();
                endEvent.setId(config.getId());
                return endEvent;
            }
            case "userTask": {
                UserTask task = new UserTask();
                task.setId(config.getId());
                task.setName(config.getName());
                task.setAssignee(config.getAssignee());
                return task;
            }
            case "exclusiveGateway": {
                ExclusiveGateway gateway = new ExclusiveGateway();
                gateway.setId(config.getId());
                gateway.setName(config.getName());
                return gateway;
            }
            default:
                throw new IllegalArgumentException("Unknown node type: " + config.getType());
        }
    }
}
