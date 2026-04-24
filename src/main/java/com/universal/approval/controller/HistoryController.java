package com.universal.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.universal.approval.entity.BizApprovalLog;
import com.universal.approval.mapper.BizApprovalLogMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approval/history")
public class HistoryController {

    @Resource
    private BizApprovalLogMapper approvalLogMapper;

    @GetMapping("/log")
    public ApiResponse<List<BizApprovalLog>> getApprovalLogs(@RequestParam String businessKey) {
        List<BizApprovalLog> logs = approvalLogMapper.selectList(new LambdaQueryWrapper<BizApprovalLog>()
                .eq(BizApprovalLog::getBusinessKey, businessKey)
                .orderByDesc(BizApprovalLog::getCreateTime));
        return ApiResponse.success(logs);
    }
}
