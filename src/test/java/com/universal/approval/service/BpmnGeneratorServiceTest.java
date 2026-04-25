package com.universal.approval.service;

import com.universal.approval.model.ConditionConfig;
import com.universal.approval.model.GlobalConfig;
import com.universal.approval.model.JsonTemplate;
import com.universal.approval.model.NodeConfig;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BpmnGeneratorServiceTest extends BaseServiceTest {

    private BpmnGeneratorService bpmnGeneratorService;

    @BeforeEach
    public void setup() {
        bpmnGeneratorService = new BpmnGeneratorService();
    }

    @Test
    public void testConvertSimpleProcess() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("TEST_001");
        template.setTemplateName("测试流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "End");
        startNode.setNextNodes(startNextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
        assertEquals(1, bpmnModel.getProcesses().size());

        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        assertEquals("TEST_001", process.getId());
        assertEquals("测试流程", process.getName());

        FlowElement startElement = process.getFlowElement("Start");
        assertNotNull(startElement);
        assertTrue(startElement instanceof StartEvent);
        assertEquals("开始", startElement.getName());

        FlowElement endElement = process.getFlowElement("End");
        assertNotNull(endElement);
        assertTrue(endElement instanceof EndEvent);
        assertEquals("结束", endElement.getName());
    }

    @Test
    public void testConvertProcessWithUserTask() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("TASK_001");
        template.setTemplateName("任务审批流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "ApproveTask");
        startNode.setNextNodes(startNextNodes);

        NodeConfig userTask = new NodeConfig();
        userTask.setNodeId("ApproveTask");
        userTask.setNodeName("审批任务");
        userTask.setNodeType("userTask");
        userTask.setAssigneeType("user");
        userTask.setAssigneeValue("manager");
        Map<String, String> taskNextNodes = new HashMap<>();
        taskNextNodes.put("next", "End");
        userTask.setNextNodes(taskNextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, userTask, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);

        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);

        FlowElement taskElement = process.getFlowElement("ApproveTask");
        assertNotNull(taskElement);
        assertTrue(taskElement instanceof UserTask);
        UserTask userTaskElement = (UserTask) taskElement;
        assertEquals("审批任务", userTaskElement.getName());
        assertEquals("manager", userTaskElement.getAssignee());
    }

    @Test
    public void testConvertProcessWithExclusiveGateway() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("GATEWAY_001");
        template.setTemplateName("条件分支流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("condition1", "Gateway");
        startNextNodes.put("condition2", "End");
        startNode.setNextNodes(startNextNodes);

        NodeConfig gateway = new NodeConfig();
        gateway.setNodeId("Gateway");
        gateway.setNodeName("条件网关");
        gateway.setNodeType("exclusiveGateway");

        Map<String, ConditionConfig> conditions = new HashMap<>();
        ConditionConfig cond1 = new ConditionConfig();
        cond1.setExpression("${days <= 3}");
        cond1.setNext("ApproveTask");
        conditions.put("condition1", cond1);

        ConditionConfig cond2 = new ConditionConfig();
        cond2.setExpression("${days > 3}");
        cond2.setNext("End");
        conditions.put("condition2", cond2);
        gateway.setConditions(conditions);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, gateway, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);

        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);

        FlowElement gatewayElement = process.getFlowElement("Gateway");
        assertNotNull(gatewayElement);
        assertTrue(gatewayElement instanceof ExclusiveGateway);
        assertEquals("条件网关", gatewayElement.getName());
    }

    @Test
    public void testConvertLeaveProcess() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("LEAVE_001");
        template.setTemplateName("请假审批流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "DaysCheck");
        startNode.setNextNodes(startNextNodes);

        NodeConfig daysCheckGateway = new NodeConfig();
        daysCheckGateway.setNodeId("DaysCheck");
        daysCheckGateway.setNodeName("请假天数检查");
        daysCheckGateway.setNodeType("exclusiveGateway");
        Map<String, String> gatewayNextNodes = new HashMap<>();
        gatewayNextNodes.put("short", "DirectManagerShort");
        gatewayNextNodes.put("long", "DirectManagerLong");
        daysCheckGateway.setNextNodes(gatewayNextNodes);
        Map<String, ConditionConfig> gatewayConditions = new HashMap<>();
        ConditionConfig shortCond = new ConditionConfig();
        shortCond.setExpression("${leave_days <= 3}");
        shortCond.setNext("DirectManagerShort");
        gatewayConditions.put("short", shortCond);
        ConditionConfig longCond = new ConditionConfig();
        longCond.setExpression("${leave_days > 3}");
        longCond.setNext("DirectManagerLong");
        gatewayConditions.put("long", longCond);
        daysCheckGateway.setConditions(gatewayConditions);

        NodeConfig directManagerShort = new NodeConfig();
        directManagerShort.setNodeId("DirectManagerShort");
        directManagerShort.setNodeName("直接主管审批(≤3天)");
        directManagerShort.setNodeType("userTask");
        directManagerShort.setAssigneeValue("direct_manager");
        Map<String, String> shortNextNodes = new HashMap<>();
        shortNextNodes.put("next", "HR");
        directManagerShort.setNextNodes(shortNextNodes);

        NodeConfig directManagerLong = new NodeConfig();
        directManagerLong.setNodeId("DirectManagerLong");
        directManagerLong.setNodeName("直接主管审批(>3天)");
        directManagerLong.setNodeType("userTask");
        directManagerLong.setAssigneeValue("direct_manager");
        Map<String, String> longNextNodes = new HashMap<>();
        longNextNodes.put("next", "SeniorManager");
        directManagerLong.setNextNodes(longNextNodes);

        NodeConfig seniorManager = new NodeConfig();
        seniorManager.setNodeId("SeniorManager");
        seniorManager.setNodeName("上级主管审批");
        seniorManager.setNodeType("userTask");
        seniorManager.setAssigneeValue("senior_manager");
        Map<String, String> seniorNextNodes = new HashMap<>();
        seniorNextNodes.put("next", "HR");
        seniorManager.setNextNodes(seniorNextNodes);

        NodeConfig hr = new NodeConfig();
        hr.setNodeId("HR");
        hr.setNodeName("HR审批");
        hr.setNodeType("userTask");
        hr.setAssigneeValue("hr");
        Map<String, String> hrNextNodes = new HashMap<>();
        hrNextNodes.put("next", "End");
        hr.setNextNodes(hrNextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, daysCheckGateway, directManagerShort, directManagerLong, seniorManager, hr, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        assertEquals("LEAVE_001", process.getId());
        assertEquals("请假审批流程", process.getName());

        FlowElement startElement = process.getFlowElement("Start");
        assertNotNull(startElement);
        assertTrue(startElement instanceof StartEvent);

        FlowElement daysCheckElement = process.getFlowElement("DaysCheck");
        assertNotNull(daysCheckElement);
        assertTrue(daysCheckElement instanceof ExclusiveGateway);

        FlowElement directManagerShortElement = process.getFlowElement("DirectManagerShort");
        assertNotNull(directManagerShortElement);
        assertTrue(directManagerShortElement instanceof UserTask);
        assertEquals("direct_manager", ((UserTask) directManagerShortElement).getAssignee());

        FlowElement directManagerLongElement = process.getFlowElement("DirectManagerLong");
        assertNotNull(directManagerLongElement);
        assertTrue(directManagerLongElement instanceof UserTask);
        assertEquals("direct_manager", ((UserTask) directManagerLongElement).getAssignee());

        FlowElement seniorManagerElement = process.getFlowElement("SeniorManager");
        assertNotNull(seniorManagerElement);
        assertTrue(seniorManagerElement instanceof UserTask);
        assertEquals("senior_manager", ((UserTask) seniorManagerElement).getAssignee());

        FlowElement hrElement = process.getFlowElement("HR");
        assertNotNull(hrElement);
        assertTrue(hrElement instanceof UserTask);
        assertEquals("hr", ((UserTask) hrElement).getAssignee());

        FlowElement endElement = process.getFlowElement("End");
        assertNotNull(endElement);
        assertTrue(endElement instanceof EndEvent);
    }

    @Test
    public void testConvertProcessWithSequenceFlows() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("FLOW_001");
        template.setTemplateName("顺序流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "Task1");
        startNode.setNextNodes(startNextNodes);

        NodeConfig task1 = new NodeConfig();
        task1.setNodeId("Task1");
        task1.setNodeName("任务1");
        task1.setNodeType("userTask");
        task1.setAssigneeValue("user1");
        Map<String, String> task1NextNodes = new HashMap<>();
        task1NextNodes.put("next", "Task2");
        task1.setNextNodes(task1NextNodes);

        NodeConfig task2 = new NodeConfig();
        task2.setNodeId("Task2");
        task2.setNodeName("任务2");
        task2.setNodeType("userTask");
        task2.setAssigneeValue("user2");
        Map<String, String> task2NextNodes = new HashMap<>();
        task2NextNodes.put("next", "End");
        task2.setNextNodes(task2NextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, task1, task2, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);

        java.util.Collection<FlowElement> elements = process.getFlowElements();
        long sequenceFlowCount = elements.stream()
                .filter(e -> e instanceof SequenceFlow)
                .count();
        assertEquals(3, sequenceFlowCount, "Should have 3 sequence flows");
    }

    @Test
    public void testConvertProcessWithConditions() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("COND_001");
        template.setTemplateName("条件流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("approve", "ApproveTask");
        startNextNodes.put("reject", "End");
        startNode.setNextNodes(startNextNodes);

        Map<String, ConditionConfig> conditions = new HashMap<>();
        ConditionConfig approveCond = new ConditionConfig();
        approveCond.setExpression("${result == 'APPROVE'}");
        approveCond.setNext("ApproveTask");
        conditions.put("approve", approveCond);

        ConditionConfig rejectCond = new ConditionConfig();
        rejectCond.setExpression("${result == 'REJECT'}");
        rejectCond.setNext("End");
        conditions.put("reject", rejectCond);
        startNode.setConditions(conditions);

        NodeConfig approveTask = new NodeConfig();
        approveTask.setNodeId("ApproveTask");
        approveTask.setNodeName("审批任务");
        approveTask.setNodeType("userTask");
        approveTask.setAssigneeValue("manager");
        Map<String, String> taskNextNodes = new HashMap<>();
        taskNextNodes.put("next", "End");
        approveTask.setNextNodes(taskNextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, approveTask, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);

        java.util.Collection<FlowElement> elements = process.getFlowElements();
        java.util.List<SequenceFlow> flowsWithConditions = elements.stream()
                .filter(e -> e instanceof SequenceFlow)
                .map(e -> (SequenceFlow) e)
                .filter(f -> f.getConditionExpression() != null)
                .collect(java.util.stream.Collectors.toList());

        assertEquals(2, flowsWithConditions.size(), "Should have 2 conditional flows");

        boolean hasApproveCondition = flowsWithConditions.stream()
                .anyMatch(f -> "${result == 'APPROVE'}".equals(f.getConditionExpression()));
        assertTrue(hasApproveCondition, "Should have APPROVE condition");

        boolean hasRejectCondition = flowsWithConditions.stream()
                .anyMatch(f -> "${result == 'REJECT'}".equals(f.getConditionExpression()));
        assertTrue(hasRejectCondition, "Should have REJECT condition");
    }

    @Test
    public void testConvertEmptyProcess() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("EMPTY_001");
        template.setTemplateName("空流程");
        template.setVersion(1);
        template.setNodes(java.util.Collections.emptyList());

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
        assertEquals(1, bpmnModel.getProcesses().size());
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        assertEquals("EMPTY_001", process.getId());
    }

    @Test
    public void testConvertProcessWithNullNextNodes() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("NULL_001");
        template.setTemplateName("空后续节点流程");
        template.setVersion(1);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        startNode.setNextNodes(null);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);

        assertNotNull(bpmnModel);
    }

    @Test
    public void testConvertWithInvalidNodeType() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("INVALID_001");
        template.setTemplateName("无效节点类型流程");
        template.setVersion(1);

        NodeConfig invalidNode = new NodeConfig();
        invalidNode.setNodeId("InvalidNode");
        invalidNode.setNodeName("无效节点");
        invalidNode.setNodeType("invalidType");

        template.setNodes(java.util.Arrays.asList(invalidNode));

        assertThrows(IllegalArgumentException.class, () -> {
            bpmnGeneratorService.convertToBpmnModel(template);
        });
    }

    @Test
    public void testDeployAndExecuteSimpleProcess() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("SIMPLE_EXEC_001");
        template.setTemplateName("简单执行流程");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "Task1");
        startNode.setNextNodes(startNextNodes);

        NodeConfig task1 = new NodeConfig();
        task1.setNodeId("Task1");
        task1.setNodeName("任务1");
        task1.setNodeType("userTask");
        task1.setAssigneeValue("user1");
        Map<String, String> task1NextNodes = new HashMap<>();
        task1NextNodes.put("next", "End");
        task1.setNextNodes(task1NextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, task1, endNode));

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);
        assertNotNull(bpmnModel);

        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("simple_exec_001.bpmn", bpmnModel)
                .deploy();
        assertNotNull(deployment.getId());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SIMPLE_EXEC_001");
        assertNotNull(processInstance.getId());

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("user1")
                .singleResult();
        assertNotNull(task);
        assertEquals("任务1", task.getName());

        taskService.complete(task.getId());

        long taskCount = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, taskCount);
    }

    @Test
    public void testDeployAndExecuteLeaveProcess_3Days() {
        JsonTemplate template = createLeaveProcessTemplate();

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);
        assertNotNull(bpmnModel);

        Process process = bpmnModel.getProcesses().get(0);
        assertEquals("LEAVE_EXEC_001", process.getId());

        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("leave_exec_001.bpmn", bpmnModel)
                .deploy();
        assertNotNull(deployment.getId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 3);
        variables.put("requester", "employee1");
        variables.put("result", "APPROVE");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LEAVE_EXEC_001", variables);
        assertNotNull(processInstance.getId());

        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        taskService.complete(directManagerTask.getId());

        Task hrTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("hr")
                .singleResult();
        assertNotNull(hrTask);
        taskService.complete(hrTask.getId());

        long taskCount = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, taskCount);
    }

    @Test
    public void testDeployAndExecuteLeaveProcess_5Days() {
        JsonTemplate template = createLeaveProcessTemplate();

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);
        assertNotNull(bpmnModel);

        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("leave_exec_001.bpmn", bpmnModel)
                .deploy();
        assertNotNull(deployment.getId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 5);
        variables.put("requester", "employee2");
        variables.put("result", "APPROVE");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LEAVE_EXEC_001", variables);
        assertNotNull(processInstance.getId());

        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        taskService.complete(directManagerTask.getId());

        Task seniorManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("senior_manager")
                .singleResult();
        assertNotNull(seniorManagerTask);
        taskService.complete(seniorManagerTask.getId());

        Task hrTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("hr")
                .singleResult();
        assertNotNull(hrTask);
        taskService.complete(hrTask.getId());

        long taskCount = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, taskCount);
    }

    @Test
    public void testDeployAndExecuteLeaveProcess_Rejected() {
        JsonTemplate template = createLeaveProcessTemplate();

        BpmnModel bpmnModel = bpmnGeneratorService.convertToBpmnModel(template);
        assertNotNull(bpmnModel);

        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("leave_exec_001.bpmn", bpmnModel)
                .deploy();
        assertNotNull(deployment.getId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 3);
        variables.put("requester", "employee3");
        variables.put("result", "REJECT");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LEAVE_EXEC_001", variables);
        assertNotNull(processInstance.getId());

        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        taskService.complete(directManagerTask.getId());

        long taskCount = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .count();
        assertEquals(0, taskCount);
    }

    private JsonTemplate createLeaveProcessTemplate() {
        JsonTemplate template = new JsonTemplate();
        template.setTemplateCode("LEAVE_EXEC_001");
        template.setTemplateName("请假审批流程(执行测试)");
        template.setVersion(1);

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setStartNode("Start");
        globalConfig.setEndNode("End");
        template.setGlobalConfig(globalConfig);

        NodeConfig startNode = new NodeConfig();
        startNode.setNodeId("Start");
        startNode.setNodeName("开始");
        startNode.setNodeType("startEvent");
        Map<String, String> startNextNodes = new HashMap<>();
        startNextNodes.put("next", "DaysCheck");
        startNode.setNextNodes(startNextNodes);

        NodeConfig daysCheckGateway = new NodeConfig();
        daysCheckGateway.setNodeId("DaysCheck");
        daysCheckGateway.setNodeName("请假天数检查");
        daysCheckGateway.setNodeType("exclusiveGateway");
        Map<String, String> gatewayNextNodes = new HashMap<>();
        gatewayNextNodes.put("short", "DirectManagerShort");
        gatewayNextNodes.put("long", "DirectManagerLong");
        daysCheckGateway.setNextNodes(gatewayNextNodes);
        Map<String, ConditionConfig> gatewayConditions = new HashMap<>();
        ConditionConfig shortCond = new ConditionConfig();
        shortCond.setExpression("${leave_days <= 3}");
        shortCond.setNext("DirectManagerShort");
        gatewayConditions.put("short", shortCond);
        ConditionConfig longCond = new ConditionConfig();
        longCond.setExpression("${leave_days > 3}");
        longCond.setNext("DirectManagerLong");
        gatewayConditions.put("long", longCond);
        daysCheckGateway.setConditions(gatewayConditions);

        NodeConfig directManagerShort = new NodeConfig();
        directManagerShort.setNodeId("DirectManagerShort");
        directManagerShort.setNodeName("直接主管审批(≤3天)");
        directManagerShort.setNodeType("userTask");
        directManagerShort.setAssigneeValue("direct_manager");
        Map<String, String> shortNextNodes = new HashMap<>();
        shortNextNodes.put("approve", "HR");
        shortNextNodes.put("reject", "End");
        directManagerShort.setNextNodes(shortNextNodes);
        Map<String, ConditionConfig> shortConditions = new HashMap<>();
        ConditionConfig shortApproveCond = new ConditionConfig();
        shortApproveCond.setExpression("${result == 'APPROVE'}");
        shortApproveCond.setNext("HR");
        shortConditions.put("approve", shortApproveCond);
        ConditionConfig shortRejectCond = new ConditionConfig();
        shortRejectCond.setExpression("${result == 'REJECT'}");
        shortRejectCond.setNext("End");
        shortConditions.put("reject", shortRejectCond);
        directManagerShort.setConditions(shortConditions);

        NodeConfig directManagerLong = new NodeConfig();
        directManagerLong.setNodeId("DirectManagerLong");
        directManagerLong.setNodeName("直接主管审批(>3天)");
        directManagerLong.setNodeType("userTask");
        directManagerLong.setAssigneeValue("direct_manager");
        Map<String, String> longNextNodes = new HashMap<>();
        longNextNodes.put("approve", "SeniorManager");
        longNextNodes.put("reject", "End");
        directManagerLong.setNextNodes(longNextNodes);
        Map<String, ConditionConfig> longConditions = new HashMap<>();
        ConditionConfig longApproveCond = new ConditionConfig();
        longApproveCond.setExpression("${result == 'APPROVE'}");
        longApproveCond.setNext("SeniorManager");
        longConditions.put("approve", longApproveCond);
        ConditionConfig longRejectCond = new ConditionConfig();
        longRejectCond.setExpression("${result == 'REJECT'}");
        longRejectCond.setNext("End");
        longConditions.put("reject", longRejectCond);
        directManagerLong.setConditions(longConditions);

        NodeConfig seniorManager = new NodeConfig();
        seniorManager.setNodeId("SeniorManager");
        seniorManager.setNodeName("上级主管审批");
        seniorManager.setNodeType("userTask");
        seniorManager.setAssigneeValue("senior_manager");
        Map<String, String> seniorNextNodes = new HashMap<>();
        seniorNextNodes.put("approve", "HR");
        seniorNextNodes.put("reject", "End");
        seniorManager.setNextNodes(seniorNextNodes);
        Map<String, ConditionConfig> seniorConditions = new HashMap<>();
        ConditionConfig seniorApproveCond = new ConditionConfig();
        seniorApproveCond.setExpression("${result == 'APPROVE'}");
        seniorApproveCond.setNext("HR");
        seniorConditions.put("approve", seniorApproveCond);
        ConditionConfig seniorRejectCond = new ConditionConfig();
        seniorRejectCond.setExpression("${result == 'REJECT'}");
        seniorRejectCond.setNext("End");
        seniorConditions.put("reject", seniorRejectCond);
        seniorManager.setConditions(seniorConditions);

        NodeConfig hr = new NodeConfig();
        hr.setNodeId("HR");
        hr.setNodeName("HR审批");
        hr.setNodeType("userTask");
        hr.setAssigneeValue("hr");
        Map<String, String> hrNextNodes = new HashMap<>();
        hrNextNodes.put("next", "End");
        hr.setNextNodes(hrNextNodes);

        NodeConfig endNode = new NodeConfig();
        endNode.setNodeId("End");
        endNode.setNodeName("结束");
        endNode.setNodeType("endEvent");

        template.setNodes(java.util.Arrays.asList(startNode, daysCheckGateway, directManagerShort, directManagerLong, seniorManager, hr, endNode));

        return template;
    }
}