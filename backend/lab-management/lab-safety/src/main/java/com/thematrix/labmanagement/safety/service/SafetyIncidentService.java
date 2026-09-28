package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyIncident;

public interface SafetyIncidentService extends IService<SafetyIncident> {

    /**
     * 新增事故（含通知）
     */
    void addIncident(SafetyIncident incident);

    /**
     * 更新事故（含解决通知）
     */
    void updateIncident(Long id, SafetyIncident incident);
}
