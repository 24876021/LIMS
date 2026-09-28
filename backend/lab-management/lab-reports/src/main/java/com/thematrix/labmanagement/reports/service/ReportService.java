package com.thematrix.labmanagement.reports.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.reports.entity.Report;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public interface ReportService extends IService<Report> {

    /**
     * 根据用户名获取报告列表（管理员看全部，普通用户只看自己的）
     */
    List<Report> getReportsByUsername(String username);

    /**
     * 提交报告（含通知）
     */
    Report submitReport(Report report);

    /**
     * 评分报告（含通知）
     */
    void gradeReport(Long id, Report gradeInfo);

    /**
     * 退回报告（含通知）
     */
    void rejectReport(Long id, Report rejectInfo);

    /**
     * 导出 CSV
     */
    void exportCsv(PrintWriter writer);

    /**
     * 上传附件
     */
    Map<String, String> uploadAttachment(MultipartFile file);
}
