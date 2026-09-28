package com.thematrix.labmanagement.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.equipment.entity.EquipmentUsageRecord;

import java.util.List;

/**
 * 设备使用记录 服务类
 */
public interface EquipmentUsageRecordService extends IService<EquipmentUsageRecord> {

    List<EquipmentUsageRecord> getByEquipmentId(Long equipmentId);
}
