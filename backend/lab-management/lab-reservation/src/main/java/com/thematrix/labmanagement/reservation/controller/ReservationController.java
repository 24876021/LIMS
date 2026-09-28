package com.thematrix.labmanagement.reservation.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.reservation.entity.Reservation;
import com.thematrix.labmanagement.reservation.service.ReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "预约管理相关接口")
@RequestMapping("/api")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:get')")
    @GetMapping("/reservations")
    @ApiOperation("预约列表")
    public Result getReservations() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(reservationService.getReservationsByUsername(username));
    }

    @PostMapping("/reservations")
    @ApiOperation("新建预约")
    public Result createReservation(@RequestBody Reservation reservation) {
        reservationService.createReservation(reservation);
        return Result.success("预约成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:set')")
    @PostMapping("/reservations/{id}/approve")
    @ApiOperation("通过预约")
    public Result approveReservation(@PathVariable Long id,
                                     @RequestParam(required = false) Long approverId,
                                     @RequestParam(required = false) String remark) {
        Reservation result = reservationService.approveReservation(id, approverId, remark);
        return result != null ? Result.success("审批通过") : Result.error("预约不存在");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:set')")
    @PostMapping("/reservations/{id}/reject")
    @ApiOperation("拒绝预约")
    public Result rejectReservation(@PathVariable Long id,
                                    @RequestParam(required = false) String remark) {
        Reservation result = reservationService.rejectReservation(id, remark);
        return result != null ? Result.success("已拒绝") : Result.error("预约不存在");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:set')")
    @DeleteMapping("/reservations/{id}")
    @ApiOperation("删除预约")
    public Result deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationAndNotify(id);
        return Result.success("删除成功");
    }
}
