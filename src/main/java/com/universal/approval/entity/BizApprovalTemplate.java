package com.universal.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_approval_template")
public class BizApprovalTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String templateCode;
    
    private String templateName;
    
    private Integer version;
    
    private String configJson;
    
    private String processDefinitionKey;
    
    private String deploymentId;
    
    private String status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}