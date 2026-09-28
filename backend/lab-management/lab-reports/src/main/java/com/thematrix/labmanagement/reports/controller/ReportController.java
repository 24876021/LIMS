package com.thematrix.labmanagement.reports.controller;

import com.thematrix.labmanagement.common.annotation.NoLog;
import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.reports.entity.Report;
import com.thematrix.labmanagement.reports.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

@RestController
@Api(tags = "报告管理相关接口")
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('report:get')")
    @GetMapping("/reports")
    @ApiOperation("报告列表")
    public Result getReports() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(reportService.getReportsByUsername(username));
    }

    @PostMapping("/reports")
    @ApiOperation("提交报告")
    public Result submitReport(@RequestBody Report report) {
        reportService.submitReport(report);
        return Result.success("提交成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('report:set')")
    @PostMapping("/reports/{id}/grade")
    @ApiOperation("评分")
    public Result gradeReport(@PathVariable Long id, @RequestBody Report body) {
        reportService.gradeReport(id, body);
        return Result.success("评分成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('report:set')")
    @PostMapping("/reports/{id}/reject")
    @ApiOperation("退回报告")
    public Result rejectReport(@PathVariable Long id, @RequestBody(required = false) Report body) {
        reportService.rejectReport(id, body);
        return Result.success("已退回");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('report:get')")
    @GetMapping("/reports/export")
    @ApiOperation("导出CSV")
    @NoLog("导出接口，AOP 日志无意义（返回 void 且参数含 HttpServletResponse）")
    public void exportReports(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("实验室报告", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            reportService.exportCsv(writer);
        }
    }

    @PostMapping("/reports/uploadAttachment")
    @ApiOperation("上传报告附件")
    @NoLog("附件上传含文件流，无需记录日志")
    public Result uploadAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        return Result.success("上传成功", reportService.uploadAttachment(file));
    }
}
