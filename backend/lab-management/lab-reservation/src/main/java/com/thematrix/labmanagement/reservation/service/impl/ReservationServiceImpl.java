package com.thematrix.labmanagement.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.reservation.entity.Reservation;
import com.thematrix.labmanagement.reservation.mapper.ReservationMapper;
import com.thematrix.labmanagement.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Reservation> getReservationsByUsername(String username) {
        SysUser currentUser = sysUserService.getUserByAccount(username);

        if (hasManagePermission() || currentUser == null) {
            return this.list();
        } else {
            return this.list(new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getUserId, currentUser.getUserId()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation createReservation(Reservation reservation) {
        reservation.setStatus("pending");
        reservation.setCreateTime(LocalDateTime.now());
        boolean saved = this.save(reservation);
        if (!saved) {
            throw new RuntimeException("预约提交失败");
        }

        notificationService.sendNotification(null, reservation.getUserId(),
                "预约申请已提交", "您的预约申请已提交，请等待管理员审批。", "info");
        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation approveReservation(Long id, Long approverId, String remark) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return null;
        }
        reservation.setStatus("approved");
        reservation.setApproverId(approverId);
        reservation.setApproveTime(LocalDateTime.now());
        if (remark != null && !remark.isEmpty()) {
            reservation.setApproveRemark(remark);
        }
        boolean success = this.updateById(reservation);
        if (!success) {
            throw new RuntimeException("审批失败");
        }

        Long senderId = approverId != null ? approverId : 0L;
        notificationService.sendNotification(senderId, reservation.getUserId(),
                "预约审批通过",
                "您的预约已通过审批" + (remark != null && !remark.isEmpty() ? "，备注：" + remark : ""),
                "system");
        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation rejectReservation(Long id, String remark) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return null;
        }
        reservation.setStatus("rejected");
        reservation.setApproveTime(LocalDateTime.now());
        if (remark != null && !remark.isEmpty()) {
            reservation.setApproveRemark(remark);
        }
        boolean success = this.updateById(reservation);
        if (!success) {
            throw new RuntimeException("拒绝失败");
        }

        notificationService.sendNotification(null, reservation.getUserId(),
                "预约已拒绝",
                "您的预约已被拒绝" + (remark != null && !remark.isEmpty() ? "，原因：" + remark : ""),
                "system");
        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReservationAndNotify(Long id) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            return;
        }
        this.removeById(id);
        notificationService.sendNotification(null, reservation.getUserId(),
                "预约已删除", "您的预约已被删除，如有疑问请联系管理员。", "system");
    }

    /**
     * 检查当前登录用户是否有管理权限
     */
    private boolean hasManagePermission() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "reservation:set".equals(a.getAuthority())
                            || "resource:all".equals(a.getAuthority()));
        } catch (Exception e) {
            return false;
        }
    }
}
