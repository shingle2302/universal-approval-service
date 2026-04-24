package com.universal.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_approval_log")
public class BizApprovalLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String templateCode;
    
    private String businessKey;
    
    private String processInstanceId;
    
    private String taskId;
    
    private String operation;
    
    private String operator;
    
    private String comments;
    
    private String status;
    
    private LocalDateTime createTime;
}