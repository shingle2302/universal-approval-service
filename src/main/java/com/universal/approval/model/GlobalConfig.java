package com.universal.approval.model;

import java.util.List;

public class GlobalConfig {
    private List<String> autoResetVarsOnReject;
    private String dataSource;

    public List<String> getAutoResetVarsOnReject() { return autoResetVarsOnReject; }
    public void setAutoResetVarsOnReject(List<String> autoResetVarsOnReject) { this.autoResetVarsOnReject = autoResetVarsOnReject; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}
