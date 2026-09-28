package com.thematrix.labmanagement.personnel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.personnel.entity.AttendanceActivity;
import com.thematrix.labmanagement.personnel.entity.AttendanceRecord;
import com.thematrix.labmanagement.personnel.entity.Personnel;
import com.thematrix.labmanagement.personnel.service.AttendanceActivityService;
import com.thematrix.labmanagement.personnel.service.AttendanceRecordService;
import com.thematrix.labmanagement.personnel.service.PersonnelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 人员管理控制器（重构版：业务逻辑已下沉到 Service 层）
 */
@RestController
@Api(tags = "人员管理相关接口")
@RequestMapping("/api")
public class PersonnelController {

    @Autowired
    private PersonnelService personnelService;
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private AttendanceActivityService attendanceActivityService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private NotificationService notificationService;

    // ==================== 人员 CRUD ====================

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get')")
    @GetMapping("/personnel")
    @ApiOperation("获取人员列表")
    public Result getPersonnelList() {
        return Result.success(personnelService.getPersonnelListWithNames());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set')")
    @PostMapping("/personnel")
    @ApiOperation("新增人员")
    public Result addPersonnel(@RequestBody Personnel personnel) {
        personnelService.addPersonnel(personnel);
        return Result.success("新增人员成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set')")
    @PutMapping("/personnel/{id}")
    @ApiOperation("更新人员")
    public Result updatePersonnel(@PathVariable Long id, @RequestBody Personnel personnel) {
        personnelService.updatePersonnel(id, personnel);
        return Result.success("更新人员成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:remove')")
    @DeleteMapping("/personnel/{id}")
    @ApiOperation("删除人员")
    public Result deletePersonnel(@PathVariable Long id) {
        return personnelService.removeById(id) ? Result.success("删除人员成功") : Result.error("删除人员失败");
    }

    // ==================== 考勤记录 ====================

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get')")
    @GetMapping("/personnel/{id}/attendance")
    @ApiOperation("获取考勤记录")
    public Result getAttendanceRecords(@PathVariable Long id) {
        Long personnelId = id;
        Personnel personnel = personnelService.getByUserId(id);
        if (personnel != null) {
            personnelId = personnel.getId();
        }
        return Result.success(attendanceRecordService.getAttendanceRecordsWithActivityInfo(personnelId));
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set')")
    @PutMapping("/personnel/attendance/{recordId}")
    @ApiOperation("更新考勤状态")
    public Result updateAttendanceStatus(@PathVariable Long recordId, @RequestBody AttendanceRecord record) {
        AttendanceRecord existing = attendanceRecordService.getById(recordId);
        record.setId(recordId);
        attendanceRecordService.updateById(record);

        if (existing != null) {
            Personnel personnel = personnelService.getById(existing.getPersonnelId());
            if (personnel != null && personnel.getUserId() != null) {
                notificationService.sendNotification(null, personnel.getUserId(),
                        "考勤状态已更新",
                        "管理员已将您的考勤状态修改为：" + attendanceRecordService.convertStatusToLabel(record.getStatus()) + "，如有疑问请联系管理员。",
                        "info");
            }
        }
        return Result.success("更新考勤状态成功");
    }

    // ==================== 用户认证 ====================

    @GetMapping("/personnel/certification")
    @ApiOperation("查询当前登录用户的认证状态")
    public Result getCertification() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(personnelService.getCertification(username));
    }

    @PostMapping("/personnel/certify")
    @ApiOperation("用户身份认证")
    public Result certify(@RequestBody Personnel personnel) {
        personnelService.certify(personnel);
        return Result.success("认证信息已提交，等待管理员确认");
    }

    // ==================== 考勤活动管理 ====================

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get')")
    @GetMapping("/attendance-activities")
    @ApiOperation("获取考勤活动列表")
    public Result getAttendanceActivities() {
        return Result.success(attendanceActivityService.list());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get')")
    @GetMapping("/attendance-activities/active")
    @ApiOperation("获取活跃活动")
    public Result getActiveActivities() {
        return Result.success(attendanceActivityService.list(
                new LambdaQueryWrapper<AttendanceActivity>().eq(AttendanceActivity::getStatus, "active")
        ));
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set')")
    @PostMapping("/attendance-activities")
    @ApiOperation("创建活动")
    public Result createActivity(@RequestBody AttendanceActivity activity,
                                  @RequestParam(required = false) String participantIds) {
        attendanceActivityService.createActivity(activity, participantIds);
        return Result.success("创建活动成功");
    }

    @PostMapping("/attendance-activities/{id}/sign")
    @ApiOperation("签到")
    public Result signIn(@PathVariable Long id, @RequestParam Long personnelId) {
        attendanceActivityService.signIn(id, personnelId);
        return Result.success("签到成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set')")
    @PostMapping("/attendance-activities/{id}/end")
    @ApiOperation("结束活动")
    public Result endActivity(@PathVariable Long id) {
        attendanceActivityService.endActivity(id);
        return Result.success("活动已结束");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get')")
    @GetMapping("/attendance-activities/{id}/status")
    @ApiOperation("获取活动签到状态")
    public Result getActivityStatus(@PathVariable Long id) {
        return Result.success(attendanceActivityService.getActivityStatus(id));
    }

    // ==================== 公开考勤接口 ====================

    @GetMapping("/attendance/my")
    @ApiOperation("获取当前登录用户的考勤信息")
    public Result getMyAttendance() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> result = personnelService.getMyAttendanceInfo(username);
        if (result == null) {
            // 用户不存在，返回空数据
            result = new HashMap<>();
            result.put("personnelId", null);
            result.put("pendingActivities", new ArrayList<>());
            result.put("attendanceRecords", new ArrayList<>());
        }
        return Result.success(result);
    }

    @PostMapping("/attendance/my/{activityId}/sign")
    @ApiOperation("当前用户签到")
    public Result mySignIn(@PathVariable Long activityId) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        if (currentUser == null) return Result.error("未找到当前用户信息");

        Personnel myPersonnel = personnelService.getByUserId(currentUser.getUserId());
        if (myPersonnel == null) return Result.error("您尚未关联人员信息，无法签到");

        attendanceActivityService.signIn(activityId, myPersonnel.getId());
        return Result.success("签到成功");
    }

}
