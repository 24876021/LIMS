package com.thematrix.labmanagement.reports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.reports.entity.Report;
import com.thematrix.labmanagement.reports.mapper.ReportMapper;
import com.thematrix.labmanagement.reports.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Report> getReportsByUsername(String username) {
        SysUser currentUser = sysUserService.getUserByAccount(username);

        if (hasManagePermission() || currentUser == null) {
            return this.list();
        } else {
            return this.list(new LambdaQueryWrapper<Report>()
                    .eq(Report::getUserId, currentUser.getUserId()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report submitReport(Report report) {
        report.setStatus("pending");
        report.setSubmitTime(LocalDateTime.now());
        boolean saved = this.save(report);
        if (!saved) {
            throw new RuntimeException("报告提交失败");
        }

        notificationService.sendNotification(null, report.getUserId(),
                "实验报告提交成功",
                "您的实验报告《" + (report.getTitle() != null ? report.getTitle() : "未命名") + "》已提交，请等待教师批改。",
                "info");
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeReport(Long id, Report gradeInfo) {
        Report report = this.getById(id);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }

        report.setStatus("approved");
        report.setScore(gradeInfo.getScore());
        report.setComment(gradeInfo.getComment());
        report.setReviewerId(gradeInfo.getReviewerId());
        report.setReviewTime(LocalDateTime.now());
        boolean success = this.updateById(report);
        if (!success) {
            throw new RuntimeException("批改失败");
        }

        String scoreStr = gradeInfo.getScore() != null ? "，得分：" + gradeInfo.getScore() + " 分" : "";
        String commentStr = gradeInfo.getComment() != null && !gradeInfo.getComment().isEmpty()
                ? "，评语：" + gradeInfo.getComment() : "";
        notificationService.sendNotification(null, report.getUserId(),
                "实验报告已批改",
                "您的报告《" + (report.getTitle() != null ? report.getTitle() : "未命名") + "》已批改" + scoreStr + commentStr,
                "success");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReport(Long id, Report rejectInfo) {
        Report report = this.getById(id);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }

        report.setStatus("rejected");
        report.setReviewTime(LocalDateTime.now());
        if (rejectInfo != null && rejectInfo.getComment() != null) {
            report.setComment(rejectInfo.getComment());
        }
        boolean success = this.updateById(report);
        if (!success) {
            throw new RuntimeException("退回失败");
        }

        String reasonStr = (rejectInfo != null && rejectInfo.getComment() != null && !rejectInfo.getComment().isEmpty())
                ? "，退回原因：" + rejectInfo.getComment() : "";
        notificationService.sendNotification(null, report.getUserId(),
                "实验报告已退回",
                "您的报告《" + (report.getTitle() != null ? report.getTitle() : "未命名") + "》已被退回，请修改后重新提交" + reasonStr,
                "warning");
    }

    @Override
    public void exportCsv(PrintWriter writer) {
        List<Report> list = this.list();
        writer.println("ID,标题,提交人ID,课程,提交时间,状态,分数,审核人ID,审核时间,评语,附件");

        for (Report r : list) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    r.getId() == null ? "" : r.getId(),
                    escapeCsv(r.getTitle()),
                    r.getUserId() == null ? "" : r.getUserId(),
                    escapeCsv(r.getCourse()),
                    r.getSubmitTime() == null ? "" : r.getSubmitTime().toString(),
                    escapeCsv(r.getStatus()),
                    r.getScore() == null ? "" : r.getScore(),
                    r.getReviewerId() == null ? "" : r.getReviewerId(),
                    r.getReviewTime() == null ? "" : r.getReviewTime().toString(),
                    escapeCsv(r.getComment()),
                    escapeCsv(r.getAttachment())
            ));
        }
    }

    @Override
    public Map<String, String> uploadAttachment(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "report_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000) + extension;

            String uploadDir = System.getProperty("user.dir") + "/uploads/attachment/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(uploadDir + filename);
            file.transferTo(destFile);

            String fileUrl = "/uploads/attachment/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", originalFilename);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    private String escapeCsv(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private boolean hasManagePermission() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "report:set".equals(a.getAuthority())
                            || "resource:all".equals(a.getAuthority()));
        } catch (Exception e) {
            return false;
        }
    }
}
