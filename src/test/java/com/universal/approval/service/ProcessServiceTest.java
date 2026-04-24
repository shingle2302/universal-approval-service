package com.universal.approval.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ProcessServiceTest {
    
    @Autowired
    private ProcessService processService;
    
    @Test
    public void testStartProcess() {
        // 测试流程启动
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptManager", "user1");
        variables.put("amount", 6000);
        
        String processInstanceId = processService.startProcess(
                "PURCHASE_001",
                "PO-2026-001",
                variables
        );
        
        assert processInstanceId != null;
        System.out.println("Process started: " + processInstanceId);
    }
    
    @Test
    public void testCompleteTask() {
        // 测试任务完成
        // TODO: 先启动流程，然后获取任务并完成
    }
}