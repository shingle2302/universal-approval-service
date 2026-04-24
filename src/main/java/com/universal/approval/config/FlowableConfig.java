package com.universal.approval.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    
    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        // 配置流程引擎
        config.setAsyncExecutorActivate(true);
        config.setDatabaseSchemaUpdate("true");

        // 配置流程自动部署
        config.setDeploymentName("approval-service-deployment");
    }
}
