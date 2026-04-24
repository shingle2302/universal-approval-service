package com.universal.approval.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.universal.approval.mapper")
public class MyBatisPlusConfig {
}