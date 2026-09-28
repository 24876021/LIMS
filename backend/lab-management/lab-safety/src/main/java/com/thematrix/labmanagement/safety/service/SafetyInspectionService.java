package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyInspection;

public interface SafetyInspectionService extends IService<SafetyInspection> {

    /**
     * 新增检查记录（含 createTime 初始化）
     */
    boolean saveInspection(SafetyInspection inspection);

    /**
     * 更新检查记录（含整改完成通知）
     */
    void updateInspection(Long id, SafetyInspection inspection);
}
