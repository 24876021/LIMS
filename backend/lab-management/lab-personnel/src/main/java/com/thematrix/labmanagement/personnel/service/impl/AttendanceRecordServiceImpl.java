package com.thematrix.labmanagement.personnel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.personnel.entity.AttendanceActivity;
import com.thematrix.labmanagement.personnel.entity.AttendanceRecord;
import com.thematrix.labmanagement.personnel.mapper.AttendanceRecordMapper;
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
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord>
        implements AttendanceRecordService {

    @Autowired
    private AttendanceActivityService attendanceActivityService;
    @Autowired
    @Lazy
    private PersonnelService personnelService;

    @Override
    public List<Map<String, Object>> getAttendanceRecordsWithActivityInfo(Long personnelId) {
        List<AttendanceRecord> list = list(
                new LambdaQueryWrapper<AttendanceRecord>().eq(AttendanceRecord::getPersonnelId, personnelId)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AttendanceRecord record : list) {
            AttendanceActivity activity = attendanceActivityService.getById(record.getActivityId());
            if (activity == null) continue;

            Map<String, Object> dto = new HashMap<>();
            dto.put("id", record.getId());
            dto.put("date", record.getSignTime() != null ? record.getSignTime().format(dateFormatter)
                    : activity.getStartTime() != null ? activity.getStartTime().format(dateFormatter) : "-");
            dto.put("activityTitle", activity.getName());
            dto.put("checkIn", record.getSignTime() != null
                    ? record.getSignTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "未签到");
            dto.put("workStart", activity.getStartTime() != null
                    ? activity.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            dto.put("workEnd", activity.getEndTime() != null
                    ? activity.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            dto.put("status", record.getStatus());
            dto.put("attendanceStatus", convertStatusToLabel(record.getStatus()));
            dto.put("sortTime", record.getSignTime() != null
                    ? record.getSignTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : activity.getStartTime() != null
                        ? activity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "9999-99-99 99:99:99");
            result.add(dto);
        }
        return result;
    }

    @Override
    public String convertStatusToLabel(String status) {
        if (status == null) return "正常";
        switch (status) {
            case "normal": return "正常";
            case "late": return "迟到";
            case "early_leave": return "早退";
            case "absent": return "缺勤";
            case "leave": return "请假";
            default: return "正常";
        }
    }
}
