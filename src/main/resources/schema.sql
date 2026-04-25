-- 业务表：审批模板
CREATE TABLE IF NOT EXISTS biz_approval_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    version INT DEFAULT 1,
    config_json TEXT,
    process_definition_key VARCHAR(100),
    deployment_id VARCHAR(100),
    status VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 业务表：审批日志
CREATE TABLE IF NOT EXISTS biz_approval_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(100),
    business_key VARCHAR(200),
    process_instance_id VARCHAR(100),
    task_id VARCHAR(100),
    operation VARCHAR(50),
    operator VARCHAR(100),
    comments TEXT,
    status VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 业务表：事件消息
CREATE TABLE IF NOT EXISTS biz_event_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    business_key VARCHAR(200),
    process_instance_id VARCHAR(100),
    event_content TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 业务表：补偿日志
CREATE TABLE IF NOT EXISTS biz_compensation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compensation_type VARCHAR(50) NOT NULL,
    business_key VARCHAR(200),
    process_instance_id VARCHAR(100),
    compensation_topic VARCHAR(200),
    compensation_params TEXT,
    status VARCHAR(20),
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
