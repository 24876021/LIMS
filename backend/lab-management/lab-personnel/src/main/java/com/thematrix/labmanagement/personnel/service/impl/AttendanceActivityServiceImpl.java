package com.thematrix.labmanagement.personnel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.personnel.entity.AttendanceActivity;
import com.thematrix.labmanagement.personnel.entity.AttendanceRecord;
import com.thematrix.labmanagement.personnel.entity.Personnel;
import com.thematrix.labmanagement.personnel.mapper.AttendanceActivityMapper;
import com.thematrix.labmanagement.personnel.service.AttendanceActivityService;
import com.thematrix.labmanagement.personnel.service.AttendanceRecordService;
import com.thematrix.labmanagement.personnel.service.PersonnelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceActivityServiceImpl extends ServiceImpl<AttendanceActivityMapper, AttendanceActivity>
        implements AttendanceActivityService {

    @Autowired
    @Lazy
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    @Lazy
    private PersonnelService personnelService;
    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Override
    public void createActivity(AttendanceActivity activity, String participantIds) {
        activity.setStatus("active");
        activity.setCreateTime(LocalDateTime.now());
        // 保存参与人员ID到活动备注字段（用于结束考勤时处理未签到人员）
        activity.setRemark(participantIds);
        if (!save(activity)) {
            throw new RuntimeException("创建活动失败");
        }
        // 通知所有参与人员：新的考勤活动已发布
        if (participantIds != null && !participantIds.isEmpty()) {
            String timeRange = "";
            if (activity.getStartTime() != null && activity.getEndTime() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
                timeRange = "（" + activity.getStartTime().format(fmt) + " - " + activity.getEndTime().format(fmt) + "）";
            }
            for (String idStr : participantIds.split(",")) {
                try {
                    Long pid = Long.parseLong(idStr.trim());
                    Personnel p = personnelService.getById(pid);
                    if (p != null && p.getUserId() != null) {
                        notificationService.sendNotification(null, p.getUserId(),
                                "考勤通知",
                                "新的考勤活动「" + activity.getName() + "」已发布，请及时签到" + timeRange,
                                "info");
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public void signIn(Long activityId, Long personnelId) {
        // 查找已存在的签到记录
        AttendanceRecord record = attendanceRecordService.getOne(
                new LambdaQueryWrapper<AttendanceRecord>()
                        .eq(AttendanceRecord::getActivityId, activityId)
                        .eq(AttendanceRecord::getPersonnelId, personnelId)
        );
        if (record == null) {
            // 如果没有记录，创建一条新的
            record = new AttendanceRecord();
            record.setPersonnelId(personnelId);
            record.setActivityId(activityId);
        }
        record.setSignTime(LocalDateTime.now());
        record.setStatus("normal");
        if (!attendanceRecordService.saveOrUpdate(record)) {
            throw new RuntimeException("签到失败");
        }
        // 通知签到人签到成功
        Personnel personnel = personnelService.getById(personnelId);
        if (personnel != null && personnel.getUserId() != null) {
            AttendanceActivity act = getById(activityId);
            notificationService.sendNotification(null, personnel.getUserId(),
                    "签到成功",
                    "您已成功签到考勤活动「" + (act != null ? act.getName() : "") + "」",
                    "success");
        }
    }

    @Override
    public void endActivity(Long id) {
        AttendanceActivity activity = getById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        activity.setStatus("completed");
        if (!updateById(activity)) {
            throw new RuntimeException("结束活动失败");
        }
        // 为未签到的参与人员创建缺席记录
        String participantIds = activity.getRemark();
        if (participantIds != null && !participantIds.isEmpty()) {
            String[] ids = participantIds.split(",");
            for (String idStr : ids) {
                try {
                    Long personnelId = Long.parseLong(idStr.trim());
                    // 检查该人员是否已有签到记录
                    AttendanceRecord existingRecord = attendanceRecordService.getOne(
                            new LambdaQueryWrapper<AttendanceRecord>()
                                    .eq(AttendanceRecord::getActivityId, id)
                                    .eq(AttendanceRecord::getPersonnelId, personnelId)
                    );
                    if (existingRecord == null) {
                        // 没有签到记录，创建缺席记录
                        AttendanceRecord record = new AttendanceRecord();
                        record.setPersonnelId(personnelId);
                        record.setActivityId(id);
                        record.setStatus("absent");
                        attendanceRecordService.save(record);
                    }

                    // 通知每个参与人员考勤活动已结束
                    Personnel p = personnelService.getById(personnelId);
                    if (p != null && p.getUserId() != null) {
                        if (existingRecord != null) {
                            notificationService.sendNotification(null, p.getUserId(),
                                    "考勤活动已结束",
                                    "考勤活动「" + activity.getName() + "」已结束，您的签到记录已确认。",
                                    "success");
                        } else {
                            notificationService.sendNotification(null, p.getUserId(),
                                    "考勤缺勤提醒",
                                    "考勤活动「" + activity.getName() + "」已结束，您未签到，已记为缺勤。",
                                    "warning");
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public boolean isParticipant(Long activityId, Long personnelId) {
        AttendanceActivity activity = getById(activityId);
        if (activity == null || activity.getRemark() == null || activity.getRemark().isEmpty()) {
            return false;
        }
        for (String idStr : activity.getRemark().split(",")) {
            try {
                if (Long.parseLong(idStr.trim()) == personnelId) return true;
            } catch (NumberFormatException ignored) {}
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getActivityStatus(Long activityId) {
        AttendanceActivity activity = getById(activityId);
        if (activity == null) return new ArrayList<>();

        String participantIds = activity.getRemark();
        if (participantIds == null || participantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<AttendanceRecord> records = attendanceRecordService.list(
                new LambdaQueryWrapper<AttendanceRecord>().eq(AttendanceRecord::getActivityId, activityId)
        );
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        for (AttendanceRecord record : records) {
            recordMap.put(record.getPersonnelId(), record);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String idStr : participantIds.split(",")) {
            try {
                Long personnelId = Long.parseLong(idStr.trim());
                com.thematrix.labmanagement.personnel.entity.Personnel personnel =
                        personnelService.getById(personnelId);
                if (personnel == null) continue;

                Map<String, Object> dto = new HashMap<>();
                dto.put("personnelId", personnelId);
                dto.put("name", personnel.getName());
                dto.put("employeeNo", personnel.getEmployeeNo());

                AttendanceRecord record = recordMap.get(personnelId);
                if (record != null) {
                    dto.put("signed", true);
                    dto.put("signTime", record.getSignTime());
                    dto.put("status", record.getStatus());
                } else {
                    dto.put("signed", false);
                    dto.put("status", "absent");
                }
                result.add(dto);
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }
}
