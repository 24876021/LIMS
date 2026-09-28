package com.thematrix.labmanagement.personnel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.service.UserRoleService;
import com.thematrix.labmanagement.personnel.entity.AttendanceActivity;
import com.thematrix.labmanagement.personnel.entity.AttendanceRecord;
import com.thematrix.labmanagement.personnel.entity.Personnel;
import com.thematrix.labmanagement.personnel.mapper.PersonnelMapper;
import com.thematrix.labmanagement.personnel.service.AttendanceActivityService;
import com.thematrix.labmanagement.personnel.service.AttendanceRecordService;
import com.thematrix.labmanagement.personnel.service.PersonnelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PersonnelServiceImpl extends ServiceImpl<PersonnelMapper, Personnel>
        implements PersonnelService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private AttendanceActivityService attendanceActivityService;
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRoleService userRoleService;

    @Override
    public void addPersonnel(Personnel personnel) {
        // 如果传了 userId，从 SysUser 获取姓名、手机、邮箱
        if (personnel.getUserId() != null) {
            SysUser user = sysUserService.getById(personnel.getUserId());
            if (user != null) {
                if (personnel.getName() == null || personnel.getName().isEmpty()) {
                    personnel.setName(user.getName());
                }
                if (personnel.getPhone() == null || personnel.getPhone().isEmpty()) {
                    personnel.setPhone(user.getPhone());
                }
                if (personnel.getEmail() == null || personnel.getEmail().isEmpty()) {
                    personnel.setEmail(user.getEmail());
                }
                if (personnel.getGender() == null || personnel.getGender().isEmpty()) {
                    personnel.setGender(user.getSex());
                }
                if (personnel.getDepartmentId() == null && user.getDepartmentId() != null) {
                    personnel.setDepartmentId(user.getDepartmentId());
                }
            }
        }
        // 检查工号是否已被其他人员使用
        if (personnel.getEmployeeNo() != null && !personnel.getEmployeeNo().isEmpty()) {
            Personnel existByEmployeeNo = getOne(
                    new LambdaQueryWrapper<Personnel>().eq(Personnel::getEmployeeNo, personnel.getEmployeeNo())
            );
            if (existByEmployeeNo != null) {
                throw new RuntimeException("工号 " + personnel.getEmployeeNo() + " 已被其他人员使用，请勿重复添加");
            }
        }
        // 管理员创建/关联的人员记录默认为在职状态
        if (personnel.getStatus() == null || personnel.getStatus().isEmpty()) {
            personnel.setStatus("active");
        }
        personnel.setCreateTime(LocalDateTime.now());
        personnel.setUpdateTime(LocalDateTime.now());
        if (!save(personnel)) {
            throw new RuntimeException("新增人员失败");
        }
        // 如果关联了系统用户，将人员信息中的性别、邮箱、电话同步到系统用户
        if (personnel.getUserId() != null) {
            SysUser user = sysUserService.getById(personnel.getUserId());
            if (user != null) {
                boolean updated = false;
                if (personnel.getGender() != null && !personnel.getGender().isEmpty()) {
                    user.setSex(personnel.getGender());
                    updated = true;
                }
                if (personnel.getEmail() != null && !personnel.getEmail().isEmpty()) {
                    user.setEmail(personnel.getEmail());
                    updated = true;
                }
                if (personnel.getPhone() != null && !personnel.getPhone().isEmpty()) {
                    user.setPhone(personnel.getPhone());
                    updated = true;
                }
                if (personnel.getDepartmentId() != null) {
                    user.setDepartmentId(personnel.getDepartmentId());
                    updated = true;
                }
                if (updated) {
                    sysUserService.updateById(user);
                }
            }
        }
    }

    @Override
    public void updatePersonnel(Long id, Personnel personnel) {
        // 先读取旧记录，用于判断状态变更
        Personnel existing = getById(id);
        String oldStatus = existing != null ? existing.getStatus() : null;
        String newStatus = personnel.getStatus();

        personnel.setId(id);
        // 检查工号是否与其他记录重复（排除自己）
        if (personnel.getEmployeeNo() != null && !personnel.getEmployeeNo().isEmpty()) {
            Personnel existByEmployeeNo = getOne(
                    new LambdaQueryWrapper<Personnel>()
                            .eq(Personnel::getEmployeeNo, personnel.getEmployeeNo())
                            .ne(Personnel::getId, id)
            );
            if (existByEmployeeNo != null) {
                throw new RuntimeException("工号 " + personnel.getEmployeeNo() + " 已被其他人员使用，请勿重复");
            }
        }
        personnel.setUpdateTime(LocalDateTime.now());
        if (!updateById(personnel)) {
            throw new RuntimeException("更新人员失败");
        }
        // 如果关联了系统用户，将人员信息中的性别、邮箱、电话同步到系统用户
        Personnel updatedExisting = getById(id);
        if (updatedExisting != null && updatedExisting.getUserId() != null) {
            SysUser user = sysUserService.getById(updatedExisting.getUserId());
            if (user != null) {
                boolean updated = false;
                if (personnel.getGender() != null && !personnel.getGender().isEmpty()) {
                    user.setSex(personnel.getGender());
                    updated = true;
                }
                if (personnel.getEmail() != null && !personnel.getEmail().isEmpty()) {
                    user.setEmail(personnel.getEmail());
                    updated = true;
                }
                if (personnel.getPhone() != null && !personnel.getPhone().isEmpty()) {
                    user.setPhone(personnel.getPhone());
                    updated = true;
                }
                if (personnel.getDepartmentId() != null) {
                    user.setDepartmentId(personnel.getDepartmentId());
                    updated = true;
                }
                if (updated) {
                    sysUserService.updateById(user);
                }
            }
        }

        // 认证审核通知：状态从 pending 变更时推送
        if ("pending".equals(oldStatus) && newStatus != null && !"pending".equals(newStatus)) {
            Long userId = updatedExisting != null ? updatedExisting.getUserId() : null;
            if (userId != null) {
                String personName = String.valueOf(userId);
                SysUser u = sysUserService.getById(userId);
                if (u != null && u.getName() != null && !u.getName().isEmpty()) {
                    personName += "-" + u.getName();
                }
                if ("active".equals(newStatus)) {
                    notificationService.sendNotification(null, userId,
                            "认证审核通过",
                            "恭喜，" + personName + "，您的身份认证已通过审核！",
                            "success");
                } else {
                    notificationService.sendNotification(null, userId,
                            "认证审核未通过",
                            personName + "，很遗憾，您的身份认证未通过审核，请联系管理员了解详情。",
                            "warning");
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getPersonnelListWithNames() {
        List<Personnel> list = list();
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (Personnel p : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("userId", p.getUserId());
                item.put("name", p.getName());
                item.put("employeeNo", p.getEmployeeNo());
                item.put("gender", p.getGender());
                item.put("phone", p.getPhone());
                item.put("email", p.getEmail());
                item.put("department", p.getDepartment());
                item.put("departmentId", p.getDepartmentId());
                item.put("position", p.getPosition());
                item.put("status", p.getStatus());
                item.put("createTime", p.getCreateTime());
                item.put("updateTime", p.getUpdateTime());
                // 填充系统用户名
                if (p.getUserId() != null) {
                    SysUser u = sysUserService.getById(p.getUserId());
                    item.put("userName", u != null ? u.getName() : "");
                } else {
                    item.put("userName", "");
                }
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void certify(Personnel personnel) {
        // 从 SecurityContext 获取当前登录用户
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser currentUser = sysUserService.getUserByAccount(username);
        Long currentUserId = currentUser.getUserId();

        // 一个用户只能认证一次（一条人员记录）
        Personnel existing = getOne(
                new LambdaQueryWrapper<Personnel>().eq(Personnel::getUserId, currentUserId)
        );
        if (existing != null) {
            throw new RuntimeException("您已认证过，不能重复认证");
        }

        // 检查工号是否已被其他人员使用
        if (personnel.getEmployeeNo() != null && !personnel.getEmployeeNo().isEmpty()) {
            Personnel existByEmployeeNo = getOne(
                    new LambdaQueryWrapper<Personnel>().eq(Personnel::getEmployeeNo, personnel.getEmployeeNo())
            );
            if (existByEmployeeNo != null) {
                throw new RuntimeException("工号 " + personnel.getEmployeeNo() + " 已被其他用户认证，请勿重复提交");
            }
        }

        // 强制绑定到当前登录用户
        personnel.setUserId(currentUserId);

        // 从 SysUser 自动填充缺失字段
        if (personnel.getName() == null || personnel.getName().isEmpty()) {
            personnel.setName(currentUser.getName());
        }
        if (personnel.getPhone() == null || personnel.getPhone().isEmpty()) {
            personnel.setPhone(currentUser.getPhone());
        }
        if (personnel.getEmail() == null || personnel.getEmail().isEmpty()) {
            personnel.setEmail(currentUser.getEmail());
        }
        if (personnel.getGender() == null || personnel.getGender().isEmpty()) {
            personnel.setGender(currentUser.getSex());
        }
        if (personnel.getDepartmentId() == null && currentUser.getDepartmentId() != null) {
            personnel.setDepartmentId(currentUser.getDepartmentId());
        }

        // 设置为待确认状态
        personnel.setStatus("pending");
        personnel.setCreateTime(LocalDateTime.now());
        personnel.setUpdateTime(LocalDateTime.now());

        if (!save(personnel)) {
            throw new RuntimeException("提交失败，请稍后再试");
        }

        // 通知所有管理员：有新的认证申请
        List<Long> adminUserIds = userRoleService.getUserIdsByRoleId(1L); // 1 = 系统管理员
        if (adminUserIds != null && !adminUserIds.isEmpty()) {
            String certName = currentUserId + "-" + (currentUser.getName() != null ? currentUser.getName() : "");
            for (Long adminId : adminUserIds) {
                notificationService.sendNotification(currentUserId, adminId,
                        "新的认证申请",
                        "用户 " + certName + "（工号：" + (personnel.getEmployeeNo() != null ? personnel.getEmployeeNo() : "-") + "）提交了身份认证，请前往人员管理进行审核。",
                        "info");
            }
        }
    }

    @Override
    public Personnel getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Personnel>().eq(Personnel::getUserId, userId));
    }

    @Override
    public Personnel getCertification(String username) {
        SysUser user = sysUserService.getUserByAccount(username);
        if (user == null) return null;
        return getByUserId(user.getUserId());
    }

    @Override
    public Map<String, Object> getMyAttendanceInfo(String username) {
        SysUser currentUser = sysUserService.getUserByAccount(username);
        if (currentUser == null) return null;

        Personnel myPersonnel = getByUserId(currentUser.getUserId());
        Map<String, Object> result = new HashMap<>();

        if (myPersonnel == null) {
            // 没有关联人员，返回空数据
            result.put("personnelId", null);
            result.put("pendingActivities", new ArrayList<>());
            result.put("attendanceRecords", new ArrayList<>());
            return result;
        }
        result.put("personnelId", myPersonnel.getId());
        result.put("personnelName", myPersonnel.getName());

        // 待签到活动
        List<AttendanceActivity> allActive = attendanceActivityService.list(
                new LambdaQueryWrapper<AttendanceActivity>().eq(AttendanceActivity::getStatus, "active")
        );
        List<Map<String, Object>> pendingActivities = new ArrayList<>();
        for (AttendanceActivity activity : allActive) {
            if (!attendanceActivityService.isParticipant(activity.getId(), myPersonnel.getId())) continue;
            AttendanceRecord record = attendanceRecordService.getOne(
                    new LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getActivityId, activity.getId())
                            .eq(AttendanceRecord::getPersonnelId, myPersonnel.getId())
            );
            if (record == null || record.getStatus() == null || "absent".equals(record.getStatus())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", activity.getId());
                item.put("name", activity.getName());
                item.put("startTime", activity.getStartTime());
                item.put("endTime", activity.getEndTime());
                item.put("signed", record != null);
                pendingActivities.add(item);
            }
        }
        result.put("pendingActivities", pendingActivities);

        // 历史记录
        List<AttendanceRecord> myRecords = attendanceRecordService.list(
                new LambdaQueryWrapper<AttendanceRecord>().eq(AttendanceRecord::getPersonnelId, myPersonnel.getId())
        );
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter hmFormatter = DateTimeFormatter.ofPattern("HH:mm");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        for (AttendanceRecord record : myRecords) {
            AttendanceActivity act = attendanceActivityService.getById(record.getActivityId());
            if (act == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("date", record.getSignTime() != null ? record.getSignTime().format(dateFormatter)
                    : act.getStartTime() != null ? act.getStartTime().format(dateFormatter) : "-");
            item.put("activityTitle", act.getName());
            item.put("checkIn", record.getSignTime() != null
                    ? record.getSignTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "未签到");
            item.put("workStart", act.getStartTime() != null ? act.getStartTime().format(hmFormatter) : "-");
            item.put("workEnd", act.getEndTime() != null ? act.getEndTime().format(hmFormatter) : "-");
            item.put("status", record.getStatus());
            item.put("attendanceStatus", attendanceRecordService.convertStatusToLabel(record.getStatus()));
            item.put("sortTime", record.getSignTime() != null
                    ? record.getSignTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : act.getStartTime() != null
                        ? act.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "9999-99-99 99:99:99");
            recordsList.add(item);
        }
        result.put("attendanceRecords", recordsList);

        return result;
    }
}
