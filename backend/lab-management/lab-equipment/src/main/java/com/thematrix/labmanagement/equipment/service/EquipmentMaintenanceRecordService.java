package com.thematrix.labmanagement.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;

import java.util.List;

/**
 * 设备维修记录 服务类
 */
public interface EquipmentMaintenanceRecordService extends IService<EquipmentMaintenanceRecord> {

    List<EquipmentMaintenanceRecord> getByEquipmentId(Long equipmentId);
}
