package com.universal.approval.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProcessServiceTest extends BaseServiceTest {

    @Test
    void processServiceBeanLoaded() {
        assertNotNull(repositoryService);
        assertNotNull(runtimeService);
        assertNotNull(taskService);
    }
}
