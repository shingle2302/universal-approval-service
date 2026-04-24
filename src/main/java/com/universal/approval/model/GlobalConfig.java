package com.universal.approval.model;

import lombok.Data;
import java.util.List;

@Data
public class GlobalConfig {
    private List<String> autoResetVarsOnReject;
    private String dataSource;
}