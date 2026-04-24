package com.universal.approval.model;

import java.util.List;

public class JsonTemplate {
    private String templateCode;
    private String templateName;
    private Integer version;
    private GlobalConfig globalConfigs;
    private List<NodeConfig> nodes;

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public GlobalConfig getGlobalConfigs() { return globalConfigs; }
    public void setGlobalConfigs(GlobalConfig globalConfigs) { this.globalConfigs = globalConfigs; }
    public List<NodeConfig> getNodes() { return nodes; }
    public void setNodes(List<NodeConfig> nodes) { this.nodes = nodes; }
}
