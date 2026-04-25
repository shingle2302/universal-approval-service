package com.universal.approval.service;

import org.flowable.engine.TaskService;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LeaveProcessTest extends BaseServiceTest {

    @Autowired
    private ProcessService processService;

    private String templateCode = "LEAVE_001";

    @BeforeEach
    public void setup() {
        // 部署请假流程
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:flowable=\"http://flowable.org/bpmn\" targetNamespace=\"http://www.flowable.org/processdef\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n" +
                "  <bpmn2:process id=\"LEAVE_001\" name=\"请假审批流程\" isExecutable=\"true\">\n" +
                "    <bpmn2:startEvent id=\"Start\" name=\"开始\">\n" +
                "      <bpmn2:outgoing>Start-DaysCheck</bpmn2:outgoing>\n" +
                "    </bpmn2:startEvent>\n" +
                "    <bpmn2:exclusiveGateway id=\"DaysCheck\" name=\"请假天数检查\">\n" +
                "      <bpmn2:incoming>Start-DaysCheck</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>DaysCheck-DirectManagerShort</bpmn2:outgoing>\n" +
                "      <bpmn2:outgoing>DaysCheck-DirectManagerLong</bpmn2:outgoing>\n" +
                "    </bpmn2:exclusiveGateway>\n" +
                "    <bpmn2:userTask id=\"DirectManagerShort\" name=\"直接主管审批(≤3天)\">\n" +
                "      <bpmn2:incoming>DaysCheck-DirectManagerShort</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>DirectManagerShort-Gateway</bpmn2:outgoing>\n" +
                "      <bpmn2:humanPerformer>\n" +
                "        <bpmn2:resourceAssignmentExpression>\n" +
                "          <bpmn2:formalExpression>direct_manager</bpmn2:formalExpression>\n" +
                "        </bpmn2:resourceAssignmentExpression>\n" +
                "      </bpmn2:humanPerformer>\n" +
                "    </bpmn2:userTask>\n" +
                "    <bpmn2:exclusiveGateway id=\"DirectManagerShortGateway\" name=\"直接主管审批结果(≤3天)\">\n" +
                "      <bpmn2:incoming>DirectManagerShort-Gateway</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>DirectManagerShortGateway-Approved</bpmn2:outgoing>\n" +
                "      <bpmn2:outgoing>DirectManagerShortGateway-Rejected</bpmn2:outgoing>\n" +
                "    </bpmn2:exclusiveGateway>\n" +
                "    <bpmn2:userTask id=\"DirectManagerLong\" name=\"直接主管审批(>3天)\">\n" +
                "      <bpmn2:incoming>DaysCheck-DirectManagerLong</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>DirectManagerLong-Gateway</bpmn2:outgoing>\n" +
                "      <bpmn2:humanPerformer>\n" +
                "        <bpmn2:resourceAssignmentExpression>\n" +
                "          <bpmn2:formalExpression>direct_manager</bpmn2:formalExpression>\n" +
                "        </bpmn2:resourceAssignmentExpression>\n" +
                "      </bpmn2:humanPerformer>\n" +
                "    </bpmn2:userTask>\n" +
                "    <bpmn2:exclusiveGateway id=\"DirectManagerLongGateway\" name=\"直接主管审批结果(>3天)\">\n" +
                "      <bpmn2:incoming>DirectManagerLong-Gateway</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>DirectManagerLongGateway-Approved</bpmn2:outgoing>\n" +
                "      <bpmn2:outgoing>DirectManagerLongGateway-Rejected</bpmn2:outgoing>\n" +
                "    </bpmn2:exclusiveGateway>\n" +
                "    <bpmn2:userTask id=\"SeniorManager\" name=\"上级主管审批\">\n" +
                "      <bpmn2:incoming>DirectManagerLongGateway-Approved</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>SeniorManager-Gateway</bpmn2:outgoing>\n" +
                "      <bpmn2:humanPerformer>\n" +
                "        <bpmn2:resourceAssignmentExpression>\n" +
                "          <bpmn2:formalExpression>senior_manager</bpmn2:formalExpression>\n" +
                "        </bpmn2:resourceAssignmentExpression>\n" +
                "      </bpmn2:humanPerformer>\n" +
                "    </bpmn2:userTask>\n" +
                "    <bpmn2:exclusiveGateway id=\"SeniorManagerGateway\" name=\"上级主管审批结果\">\n" +
                "      <bpmn2:incoming>SeniorManager-Gateway</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>SeniorManagerGateway-Approved</bpmn2:outgoing>\n" +
                "      <bpmn2:outgoing>SeniorManagerGateway-Rejected</bpmn2:outgoing>\n" +
                "    </bpmn2:exclusiveGateway>\n" +
                "    <bpmn2:userTask id=\"HR\" name=\"HR审批\">\n" +
                "      <bpmn2:incoming>DirectManagerShortGateway-Approved</bpmn2:incoming>\n" +
                "      <bpmn2:incoming>SeniorManagerGateway-Approved</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>HR-Gateway</bpmn2:outgoing>\n" +
                "      <bpmn2:humanPerformer>\n" +
                "        <bpmn2:resourceAssignmentExpression>\n" +
                "          <bpmn2:formalExpression>hr</bpmn2:formalExpression>\n" +
                "        </bpmn2:resourceAssignmentExpression>\n" +
                "      </bpmn2:humanPerformer>\n" +
                "    </bpmn2:userTask>\n" +
                "    <bpmn2:exclusiveGateway id=\"HRGateway\" name=\"HR审批结果\">\n" +
                "      <bpmn2:incoming>HR-Gateway</bpmn2:incoming>\n" +
                "      <bpmn2:outgoing>HRGateway-Approved</bpmn2:outgoing>\n" +
                "      <bpmn2:outgoing>HRGateway-Rejected</bpmn2:outgoing>\n" +
                "    </bpmn2:exclusiveGateway>\n" +
                "    <bpmn2:endEvent id=\"End\" name=\"结束\">\n" +
                "      <bpmn2:incoming>HRGateway-Approved</bpmn2:incoming>\n" +
                "      <bpmn2:incoming>DirectManagerShortGateway-Rejected</bpmn2:incoming>\n" +
                "      <bpmn2:incoming>DirectManagerLongGateway-Rejected</bpmn2:incoming>\n" +
                "      <bpmn2:incoming>SeniorManagerGateway-Rejected</bpmn2:incoming>\n" +
                "      <bpmn2:incoming>HRGateway-Rejected</bpmn2:incoming>\n" +
                "    </bpmn2:endEvent>\n" +
                "    <bpmn2:sequenceFlow id=\"Start-DaysCheck\" sourceRef=\"Start\" targetRef=\"DaysCheck\" />\n" +
                "    <bpmn2:sequenceFlow id=\"DaysCheck-DirectManagerShort\" sourceRef=\"DaysCheck\" targetRef=\"DirectManagerShort\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${leave_days &lt;= 3}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"DaysCheck-DirectManagerLong\" sourceRef=\"DaysCheck\" targetRef=\"DirectManagerLong\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${leave_days &gt; 3}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerShort-Gateway\" sourceRef=\"DirectManagerShort\" targetRef=\"DirectManagerShortGateway\" />\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerShortGateway-Approved\" sourceRef=\"DirectManagerShortGateway\" targetRef=\"HR\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'APPROVE'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerShortGateway-Rejected\" sourceRef=\"DirectManagerShortGateway\" targetRef=\"End\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'REJECT'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerLong-Gateway\" sourceRef=\"DirectManagerLong\" targetRef=\"DirectManagerLongGateway\" />\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerLongGateway-Approved\" sourceRef=\"DirectManagerLongGateway\" targetRef=\"SeniorManager\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'APPROVE'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"DirectManagerLongGateway-Rejected\" sourceRef=\"DirectManagerLongGateway\" targetRef=\"End\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'REJECT'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"SeniorManager-Gateway\" sourceRef=\"SeniorManager\" targetRef=\"SeniorManagerGateway\" />\n" +
                "    <bpmn2:sequenceFlow id=\"SeniorManagerGateway-Approved\" sourceRef=\"SeniorManagerGateway\" targetRef=\"HR\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'APPROVE'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"SeniorManagerGateway-Rejected\" sourceRef=\"SeniorManagerGateway\" targetRef=\"End\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'REJECT'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"HR-Gateway\" sourceRef=\"HR\" targetRef=\"HRGateway\" />\n" +
                "    <bpmn2:sequenceFlow id=\"HRGateway-Approved\" sourceRef=\"HRGateway\" targetRef=\"End\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'APPROVE'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "    <bpmn2:sequenceFlow id=\"HRGateway-Rejected\" sourceRef=\"HRGateway\" targetRef=\"End\">\n" +
                "      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${approve_result == 'REJECT'}</bpmn2:conditionExpression>\n" +
                "    </bpmn2:sequenceFlow>\n" +
                "  </bpmn2:process>\n" +
                "</bpmn2:definitions>";

        // 部署流程
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addString("leave_process.bpmn", bpmnXml);
        deploymentBuilder.deploy();
    }

    @Test
    public void testLeaveProcess_3Days() {
        // 启动3天请假流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 3);
        variables.put("requester", "employee1");
        String businessKey = "LEAVE_" + System.currentTimeMillis();
        String processInstanceId = processService.startProcess(templateCode, businessKey, variables);
        assertNotNull(processInstanceId);

        // 直接主管审批
        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        processService.completeTask(directManagerTask.getId(), "direct_manager", "APPROVE", "同意", null);

        // HR审批
        Task hrTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("hr")
                .singleResult();
        assertNotNull(hrTask);
        processService.completeTask(hrTask.getId(), "hr", "APPROVE", "同意", null);

        // 流程应该结束
        long activeTaskCount = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .count();
        assertEquals(0, activeTaskCount);
    }

    @Test
    public void testLeaveProcess_5Days() {
        // 启动5天请假流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 5);
        variables.put("requester", "employee2");
        String businessKey = "LEAVE_" + System.currentTimeMillis();
        String processInstanceId = processService.startProcess(templateCode, businessKey, variables);
        assertNotNull(processInstanceId);

        // 直接主管审批
        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        processService.completeTask(directManagerTask.getId(), "direct_manager", "APPROVE", "同意", null);

        // 上级主管审批
        Task seniorManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("senior_manager")
                .singleResult();
        assertNotNull(seniorManagerTask);
        processService.completeTask(seniorManagerTask.getId(), "senior_manager", "APPROVE", "同意", null);

        // HR审批
        Task hrTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("hr")
                .singleResult();
        assertNotNull(hrTask);
        processService.completeTask(hrTask.getId(), "hr", "APPROVE", "同意", null);

        // 流程应该结束
        long activeTaskCount = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .count();
        assertEquals(0, activeTaskCount);
    }

    @Test
    public void testLeaveProcess_Rejected() {
        // 启动请假流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("leave_days", 3);
        variables.put("requester", "employee3");
        String businessKey = "LEAVE_" + System.currentTimeMillis();
        String processInstanceId = processService.startProcess(templateCode, businessKey, variables);
        assertNotNull(processInstanceId);

        // 直接主管拒绝
        Task directManagerTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee("direct_manager")
                .singleResult();
        assertNotNull(directManagerTask);
        processService.completeTask(directManagerTask.getId(), "direct_manager", "REJECT", "拒绝", null);

        // 流程应该结束（被拒绝）
        long activeTaskCount = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .count();
        assertEquals(0, activeTaskCount);
    }
}