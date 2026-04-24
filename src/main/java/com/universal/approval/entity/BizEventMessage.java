package com.universal.approval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_event_message")
public class BizEventMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String eventType;
    
    private String businessKey;
    
    private String processInstanceId;
    
    private String eventData;
    
    private String status;
    
    private Integer retryCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}