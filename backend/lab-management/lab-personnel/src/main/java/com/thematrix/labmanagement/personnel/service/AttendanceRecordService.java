package com.thematrix.labmanagement.personnel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.personnel.entity.AttendanceRecord;

import java.util.List;
import java.util.Map;

/**
 * 考勤记录 服务类
 */
public interface AttendanceRecordService extends IService<AttendanceRecord> {

    /**
     * 获取指定人员的考勤记录（含活动信息，返回 DTO 列表）
     */
    List<Map<String, Object>> getAttendanceRecordsWithActivityInfo(Long personnelId);

    /**
     * 转换考勤状态为标签
     */
    String convertStatusToLabel(String status);
}
