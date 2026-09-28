package com.thematrix.labmanagement.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.equipment.entity.Equipment;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;

import java.util.List;

/**
 * 设备 服务类
 */
public interface EquipmentService extends IService<Equipment> {

    /**
     * 新增设备（含时间戳和状态初始化）
     */
    Equipment addEquipment(Equipment equipment);

    /**
     * 更新设备（含更新时间戳）
     */
    Equipment updateEquipment(Long id, Equipment equipment);

    /**
     * 借用设备（状态流转 + 创建使用记录 + 通知）
     */
    void borrowEquipment(Long equipmentId, Long userId, String purpose);

    /**
     * 归还设备（状态流转 + 更新使用记录 + 通知）
     */
    void returnEquipment(Long equipmentId, Long userId);

    /**
     * 提交维修（状态流转 + 创建维修记录 + 通知）
     */
    void submitMaintenance(Long equipmentId, EquipmentMaintenanceRecord record);

    /**
     * 完成维修（状态流转 + 通知）
     */
    void completeMaintenance(Long maintenanceRecordId, EquipmentMaintenanceRecord updateInfo);
}
