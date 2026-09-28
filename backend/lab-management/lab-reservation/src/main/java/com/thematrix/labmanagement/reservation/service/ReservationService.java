package com.thematrix.labmanagement.reservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.reservation.entity.Reservation;

import java.util.List;

public interface ReservationService extends IService<Reservation> {

    /**
     * 根据用户名获取预约列表（管理员看全部，普通用户只看自己的）
     */
    List<Reservation> getReservationsByUsername(String username);

    /**
     * 创建预约（含状态初始化和通知）
     */
    Reservation createReservation(Reservation reservation);

    /**
     * 通过预约（含状态流转和通知）
     */
    Reservation approveReservation(Long id, Long approverId, String remark);

    /**
     * 拒绝预约（含状态流转和通知）
     */
    Reservation rejectReservation(Long id, String remark);

    /**
     * 删除预约并发送通知
     */
    void deleteReservationAndNotify(Long id);
}
