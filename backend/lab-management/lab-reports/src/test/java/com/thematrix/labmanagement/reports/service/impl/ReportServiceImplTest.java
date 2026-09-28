package com.thematrix.labmanagement.reports.service.impl;

import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.reports.entity.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReportServiceImpl 单元测试
 * 聚焦：submitReport/gradeReport/rejectReport 的返回值检查、报告不存在校验
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private ReportServiceImpl reportService;

    // ==================== submitReport ====================

    @Test
    void submitReport_saveFails_throwsException() {
        Report report = new Report();
        report.setUserId(1L);
        report.setTitle("测试报告");

        doReturn(false).when(reportService).save(any(Report.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.submitReport(report));
        assertEquals("报告提交失败", ex.getMessage());
    }

    @Test
    void submitReport_success_setsStatusAndSendsNotification() {
        Report report = new Report();
        report.setUserId(1L);
        report.setTitle("实验报告A");

        doReturn(true).when(reportService).save(any(Report.class));

        Report result = reportService.submitReport(report);

        assertEquals("pending", result.getStatus());
        assertNotNull(result.getSubmitTime());
        verify(notificationService).sendNotification(
                isNull(), eq(1L), eq("实验报告提交成功"), contains("实验报告A"), eq("info"));
    }

    // ==================== gradeReport ====================

    @Test
    void gradeReport_reportNotFound_throwsException() {
        doReturn(null).when(reportService).getById(1L);

        Report gradeInfo = new Report();
        gradeInfo.setScore(90);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reportService.gradeReport(1L, gradeInfo));
        assertEquals("报告不存在", ex.getMessage());
    }

    @Test
    void gradeReport_updateFails_throwsException() {
        Report existing = new Report();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setTitle("测试报告");

        doReturn(existing).when(reportService).getById(1L);
        doReturn(false).when(reportService).updateById(any(Report.class));

        Report gradeInfo = new Report();
        gradeInfo.setScore(90);
        gradeInfo.setComment("不错");
        gradeInfo.setReviewerId(3L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.gradeReport(1L, gradeInfo));
        assertEquals("批改失败", ex.getMessage());
    }

    @Test
    void gradeReport_success_setsApprovedAndSendsNotification() {
        Report existing = new Report();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setTitle("实验报告B");

        doReturn(existing).when(reportService).getById(1L);
        doReturn(true).when(reportService).updateById(any(Report.class));

        Report gradeInfo = new Report();
        gradeInfo.setScore(95);
        gradeInfo.setComment("优秀");
        gradeInfo.setReviewerId(3L);

        reportService.gradeReport(1L, gradeInfo);

        assertEquals("approved", existing.getStatus());
        assertEquals(95, existing.getScore());
        assertNotNull(existing.getReviewTime());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("实验报告已批改"), contains("95"), eq("success"));
    }

    // ==================== rejectReport ====================

    @Test
    void rejectReport_reportNotFound_throwsException() {
        doReturn(null).when(reportService).getById(1L);

        Report rejectInfo = new Report();
        rejectInfo.setComment("不合格");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reportService.rejectReport(1L, rejectInfo));
        assertEquals("报告不存在", ex.getMessage());
    }

    @Test
    void rejectReport_updateFails_throwsException() {
        Report existing = new Report();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setTitle("测试报告");

        doReturn(existing).when(reportService).getById(1L);
        doReturn(false).when(reportService).updateById(any(Report.class));

        Report rejectInfo = new Report();
        rejectInfo.setComment("不合格");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.rejectReport(1L, rejectInfo));
        assertEquals("退回失败", ex.getMessage());
    }

    @Test
    void rejectReport_success_setsRejectedAndSendsNotification() {
        Report existing = new Report();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setTitle("实验报告C");

        doReturn(existing).when(reportService).getById(1L);
        doReturn(true).when(reportService).updateById(any(Report.class));

        Report rejectInfo = new Report();
        rejectInfo.setComment("需要重写");

        reportService.rejectReport(1L, rejectInfo);

        assertEquals("rejected", existing.getStatus());
        assertNotNull(existing.getReviewTime());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("实验报告已退回"), contains("需要重写"), eq("warning"));
    }
}
