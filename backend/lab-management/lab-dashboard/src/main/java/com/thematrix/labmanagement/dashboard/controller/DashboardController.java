package com.thematrix.labmanagement.dashboard.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.dashboard.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "仪表盘相关接口")
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('dashboard:get')")
    @GetMapping("/dashboard/stats")
    @ApiOperation("统计概览")
    public Result getStats() {
        return Result.success(dashboardService.getStats());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('dashboard:get')")
    @GetMapping("/dashboard/equipment-status")
    @ApiOperation("设备状态分布")
    public Result getEquipmentStatus() {
        return Result.success(dashboardService.getEquipmentStatus());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('dashboard:get')")
    @GetMapping("/dashboard/weekly-usage")
    @ApiOperation("本周使用趋势")
    public Result getWeeklyUsage() {
        return Result.success(dashboardService.getWeeklyUsage());
    }

}
