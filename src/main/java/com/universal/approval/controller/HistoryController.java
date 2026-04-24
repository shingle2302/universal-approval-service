package com.universal.approval.controller;

import com.universal.approval.entity.BizApprovalLog;
import com.universal.approval.mapper.BizApprovalLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/approval/history")
public class HistoryController {
    
    @Resource
    private BizApprovalLogMapper approvalLogMapper;
    
    @GetMapping("/log")
    public ApiResponse<List<BizApprovalLog>> getApprovalLogs(@RequestParam String businessKey) {
        // TODO: 根据 businessKey 查询审批日志
        List<BizApprovalLog> logs = approvalLogMapper.selectList(null);
        return ApiResponse.success(logs);
    }
}