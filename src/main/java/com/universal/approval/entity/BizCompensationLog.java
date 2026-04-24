package com.universal.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_compensation_log")
public class BizCompensationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String processInstanceId;
    
    private String businessKey;
    
    private String compensationTopic;
    
    private String compensationParams;
    
    private String status;
    
    private String errorMessage;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}