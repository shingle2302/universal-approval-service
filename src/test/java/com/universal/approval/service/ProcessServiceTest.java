package com.universal.approval.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProcessServiceTest {

    @Autowired
    private ProcessService processService;

    @Test
    void processServiceBeanLoaded() {
        assertNotNull(processService);
    }
}
