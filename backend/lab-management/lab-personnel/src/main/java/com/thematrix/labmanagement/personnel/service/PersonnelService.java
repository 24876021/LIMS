package com.thematrix.labmanagement.personnel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.personnel.entity.Personnel;

import java.util.List;
import java.util.Map;

/**
 * 人员管理 服务类
 */
public interface PersonnelService extends IService<Personnel> {

    void addPersonnel(Personnel personnel);

    void updatePersonnel(Long id, Personnel personnel);

    /**
     * 获取人员列表（含用户姓名）
     */
    List<Map<String, Object>> getPersonnelListWithNames();

    /**
     * 根据 userId 查找人员
     */
    Personnel getByUserId(Long userId);

    /**
     * 获取当前登录用户的认证状态
     */
    Personnel getCertification(String username);

    void certify(Personnel personnel);

    /**
     * 获取当前登录用户的考勤信息（待签到活动 + 历史记录）
     */
    Map<String, Object> getMyAttendanceInfo(String username);
}
