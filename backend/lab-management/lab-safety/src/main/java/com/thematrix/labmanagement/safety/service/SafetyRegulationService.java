package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyRegulation;

import java.time.LocalDateTime;

/**
 * 安全制度 服务类
 */
public interface SafetyRegulationService extends IService<SafetyRegulation> {

    boolean saveRegulation(SafetyRegulation regulation);

    boolean updateRegulation(Long id, SafetyRegulation regulation);
}
