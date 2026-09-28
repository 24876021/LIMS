package com.thematrix.labmanagement.reservation.service.impl;

import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.reservation.entity.Reservation;
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
 * ReservationServiceImpl 单元测试
 * 聚焦：createReservation/approveReservation/rejectReservation 的返回值检查、预约不存在处理
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private ReservationServiceImpl reservationService;

    // ==================== createReservation ====================

    @Test
    void createReservation_saveFails_throwsException() {
        Reservation reservation = new Reservation();
        reservation.setUserId(1L);

        doReturn(false).when(reservationService).save(any(Reservation.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(reservation));
        assertEquals("预约提交失败", ex.getMessage());
    }

    @Test
    void createReservation_success_setsStatusAndSendsNotification() {
        Reservation reservation = new Reservation();
        reservation.setUserId(1L);

        doReturn(true).when(reservationService).save(any(Reservation.class));

        Reservation result = reservationService.createReservation(reservation);

        assertEquals("pending", result.getStatus());
        assertNotNull(result.getCreateTime());
        verify(notificationService).sendNotification(
                isNull(), eq(1L), eq("预约申请已提交"), anyString(), eq("info"));
    }

    // ==================== approveReservation ====================

    @Test
    void approveReservation_notFound_returnsNull() {
        doReturn(null).when(reservationService).getById(1L);

        Reservation result = reservationService.approveReservation(1L, 2L, "通过");

        assertNull(result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void approveReservation_updateFails_throwsException() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setUserId(3L);

        doReturn(existing).when(reservationService).getById(1L);
        doReturn(false).when(reservationService).updateById(any(Reservation.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.approveReservation(1L, 2L, "通过"));
        assertEquals("审批失败", ex.getMessage());
    }

    @Test
    void approveReservation_success_setsApprovedAndSendsNotification() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setUserId(3L);

        doReturn(existing).when(reservationService).getById(1L);
        doReturn(true).when(reservationService).updateById(any(Reservation.class));

        Reservation result = reservationService.approveReservation(1L, 2L, "同意");

        assertEquals("approved", result.getStatus());
        assertEquals(2L, result.getApproverId());
        assertNotNull(result.getApproveTime());
        assertEquals("同意", result.getApproveRemark());
        verify(notificationService).sendNotification(
                eq(2L), eq(3L), eq("预约审批通过"), contains("同意"), eq("system"));
    }

    // ==================== rejectReservation ====================

    @Test
    void rejectReservation_notFound_returnsNull() {
        doReturn(null).when(reservationService).getById(1L);

        Reservation result = reservationService.rejectReservation(1L, "时间冲突");

        assertNull(result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void rejectReservation_updateFails_throwsException() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setUserId(3L);

        doReturn(existing).when(reservationService).getById(1L);
        doReturn(false).when(reservationService).updateById(any(Reservation.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.rejectReservation(1L, "时间冲突"));
        assertEquals("拒绝失败", ex.getMessage());
    }

    @Test
    void rejectReservation_success_setsRejectedAndSendsNotification() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setUserId(3L);

        doReturn(existing).when(reservationService).getById(1L);
        doReturn(true).when(reservationService).updateById(any(Reservation.class));

        Reservation result = reservationService.rejectReservation(1L, "时间段已被占用");

        assertEquals("rejected", result.getStatus());
        assertNotNull(result.getApproveTime());
        assertEquals("时间段已被占用", result.getApproveRemark());
        verify(notificationService).sendNotification(
                isNull(), eq(3L), eq("预约已拒绝"), contains("时间段已被占用"), eq("system"));
    }
}
