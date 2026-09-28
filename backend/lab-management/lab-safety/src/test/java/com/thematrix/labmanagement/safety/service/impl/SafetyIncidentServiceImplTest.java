package com.thematrix.labmanagement.safety.service.impl;

import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.safety.entity.SafetyIncident;
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
 * SafetyIncidentServiceImpl 单元测试
 * 聚焦：save/updateById 返回值检查、通知发送逻辑
 */
@ExtendWith(MockitoExtension.class)
class SafetyIncidentServiceImplTest {

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private SafetyIncidentServiceImpl safetyIncidentService;

    // ==================== addIncident ====================

    @Test
    void addIncident_saveFails_throwsException() {
        SafetyIncident incident = new SafetyIncident();
        incident.setReporterId(null); // 不触发通知

        doReturn(false).when(safetyIncidentService).save(any(SafetyIncident.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> safetyIncidentService.addIncident(incident));
        assertEquals("新增失败", ex.getMessage());
    }

    @Test
    void addIncident_withReporterId_sendsNotification() {
        SafetyIncident incident = new SafetyIncident();
        incident.setReporterId(1L);
        incident.setDescription("实验室着火");

        doReturn(true).when(safetyIncidentService).save(any(SafetyIncident.class));

        safetyIncidentService.addIncident(incident);

        verify(notificationService).sendNotification(
                isNull(), eq(1L), eq("安全事故已记录"), contains("实验室着火"), eq("warning"));
    }

    @Test
    void addIncident_nullDescription_sendsNotificationWithDefaultText() {
        SafetyIncident incident = new SafetyIncident();
        incident.setReporterId(1L);
        incident.setDescription(null);

        doReturn(true).when(safetyIncidentService).save(any(SafetyIncident.class));

        safetyIncidentService.addIncident(incident);

        verify(notificationService).sendNotification(
                isNull(), eq(1L), eq("安全事故已记录"), contains("（无描述）"), eq("warning"));
    }

    // ==================== updateIncident ====================

    @Test
    void updateIncident_updateFails_throwsException() {
        SafetyIncident incident = new SafetyIncident();
        incident.setStatus("processing");

        doReturn(false).when(safetyIncidentService).updateById(any(SafetyIncident.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> safetyIncidentService.updateIncident(1L, incident));
        assertEquals("更新失败", ex.getMessage());
    }

    @Test
    void updateIncident_resolvedStatus_sendsNotification() {
        SafetyIncident incident = new SafetyIncident();
        incident.setStatus("已解决");

        SafetyIncident existing = new SafetyIncident();
        existing.setId(1L);
        existing.setReporterId(2L);
        existing.setDescription("短描述");

        doReturn(true).when(safetyIncidentService).updateById(any(SafetyIncident.class));
        doReturn(existing).when(safetyIncidentService).getById(1L);

        safetyIncidentService.updateIncident(1L, incident);

        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("安全事故已解决"), contains("短描述"), eq("success"));
    }

    @Test
    void updateIncident_longDescription_truncatedInNotification() {
        String longDesc = "这是一段非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的描述";
        SafetyIncident incident = new SafetyIncident();
        incident.setStatus("closed");

        SafetyIncident existing = new SafetyIncident();
        existing.setId(1L);
        existing.setReporterId(2L);
        existing.setDescription(longDesc);

        doReturn(true).when(safetyIncidentService).updateById(any(SafetyIncident.class));
        doReturn(existing).when(safetyIncidentService).getById(1L);

        safetyIncidentService.updateIncident(1L, incident);

        // 超过30字会被截断，通知内容应包含 "..."
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("安全事故已解决"), contains("..."), eq("success"));
    }

    @Test
    void updateIncident_nonResolvedStatus_noNotification() {
        SafetyIncident incident = new SafetyIncident();
        incident.setStatus("processing");

        doReturn(true).when(safetyIncidentService).updateById(any(SafetyIncident.class));

        safetyIncidentService.updateIncident(1L, incident);

        verifyNoInteractions(notificationService);
    }
}
