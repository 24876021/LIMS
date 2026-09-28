package com.thematrix.labmanagement.personnel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.personnel.entity.AttendanceActivity;
import com.thematrix.labmanagement.personnel.entity.Personnel;

import java.util.List;
import java.util.Map;

/**
 * 考勤活动 服务类
 */
public interface AttendanceActivityService extends IService<AttendanceActivity> {

    void createActivity(AttendanceActivity activity, String participantIds);

    void signIn(Long activityId, Long personnelId);

    void endActivity(Long id);

    /**
     * 判断指定人员是否参与该活动
     */
    boolean isParticipant(Long activityId, Long personnelId);

    /**
     * 获取活动签到状态（返回包含人员签到信息的列表）
     */
    List<Map<String, Object>> getActivityStatus(Long activityId);
}
