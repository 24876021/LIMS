package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyTraining;

import java.time.LocalDateTime;

/**
 * 安全培训 服务类
 */
public interface SafetyTrainingService extends IService<SafetyTraining> {

    boolean saveTraining(SafetyTraining training);

    boolean updateTraining(Long id, SafetyTraining training);
}
