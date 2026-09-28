package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyExamRecord;

import java.util.List;
import java.util.Map;

/**
 * 安全考试记录 服务类
 */
public interface SafetyExamRecordService extends IService<SafetyExamRecord> {

    void submitExam(String username, Map<String, Object> params);

    /**
     * 获取当前用户的考试记录（返回已完成的培训ID列表）
     */
    List<Long> getMyCompletedTrainingIds(String username);

    /**
     * 获取各培训完成人数统计
     */
    Map<Long, Long> getExamStats();

    /**
     * 获取指定用户的考试记录（含培训名称）
     */
    List<Map<String, Object>> getUserExamRecordsWithTrainingName(Long userId);
}
