package com.universal.approval.model;

import lombok.Data;

@Data
public class ConditionConfig {
    private String expression;
    private String next;
}