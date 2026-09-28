package com.thematrix.labmanagement.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.equipment.entity.Equipment;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;
import com.thematrix.labmanagement.equipment.entity.EquipmentUsageRecord;
import com.thematrix.labmanagement.equipment.mapper.EquipmentMapper;
import com.thematrix.labmanagement.equipment.service.EquipmentMaintenanceRecordService;
import com.thematrix.labmanagement.equipment.service.EquipmentService;
import com.thematrix.labmanagement.equipment.service.EquipmentUsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 设备 服务实现类
 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Autowired
    private EquipmentUsageRecordService usageRecordService;

    @Autowired
    private EquipmentMaintenanceRecordService maintenanceRecordService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Equipment addEquipment(Equipment equipment) {
        equipment.setCreateTime(LocalDateTime.now());
        equipment.setUpdateTime(LocalDateTime.now());
        equipment.setStatus("normal");
        this.save(equipment);
        return equipment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Equipment updateEquipment(Long id, Equipment equipment) {
        equipment.setId(id);
        equipment.setUpdateTime(LocalDateTime.now());
        this.updateById(equipment);
        return equipment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowEquipment(Long equipmentId, Long userId, String purpose) {
        Equipment equipment = this.getById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        if (!"normal".equals(equipment.getStatus())) {
            throw new IllegalStateException("设备当前不可借用");
        }

        // 状态流转
        equipment.setStatus("borrowed");
        this.updateById(equipment);

        // 创建使用记录
        EquipmentUsageRecord record = new EquipmentUsageRecord();
        record.setEquipmentId(equipmentId);
        record.setUserId(userId);
        record.setBorrowTime(LocalDateTime.now());
        record.setPurpose(purpose);
        record.setStatus("borrowing");
        usageRecordService.save(record);

        // 发送通知
        notificationService.sendNotification(null, userId,
                "设备借用成功", "您已成功借用设备：" + equipment.getName(), "system");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnEquipment(Long equipmentId, Long userId) {
        Equipment equipment = this.getById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("设备不存在");
        }

        // 更新使用记录
        EquipmentUsageRecord record = usageRecordService.getOne(
                new LambdaQueryWrapper<EquipmentUsageRecord>()
                        .eq(EquipmentUsageRecord::getEquipmentId, equipmentId)
                        .eq(EquipmentUsageRecord::getUserId, userId)
                        .eq(EquipmentUsageRecord::getStatus, "borrowing")
        );
        if (record != null) {
            record.setReturnTime(LocalDateTime.now());
            record.setStatus("returned");
            usageRecordService.updateById(record);
        }

        // 状态流转
        equipment.setStatus("normal");
        this.updateById(equipment);

        notificationService.sendNotification(null, userId,
                "设备归还成功", "您已成功归还设备：" + equipment.getName(), "system");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMaintenance(Long equipmentId, EquipmentMaintenanceRecord record) {
        Equipment equipment = this.getById(equipmentId);
        if (equipment == null) {
            throw new IllegalArgumentException("设备不存在");
        }

        // 状态流转
        equipment.setStatus("maintenance");
        this.updateById(equipment);

        // 创建维修记录
        record.setEquipmentId(equipmentId);
        record.setReportTime(LocalDateTime.now());
        record.setStatus("pending");
        maintenanceRecordService.save(record);

        // 通知报修人
        if (record.getReporterId() != null) {
            notificationService.sendNotification(null, record.getReporterId(),
                    "设备报修申请已提交",
                    "设备《" + equipment.getName() + "》的维修申请已提交，请等待维修人员处理。",
                    "info");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeMaintenance(Long maintenanceRecordId, EquipmentMaintenanceRecord updateInfo) {
        EquipmentMaintenanceRecord record = maintenanceRecordService.getById(maintenanceRecordId);
        if (record == null) {
            throw new IllegalArgumentException("维修记录不存在");
        }

        // 更新维修记录
        record.setStatus("completed");
        record.setCompleteTime(LocalDateTime.now());
        record.setRepairResult(updateInfo.getRepairResult());
        record.setRepairPerson(updateInfo.getRepairPerson());
        record.setRepairCost(updateInfo.getRepairCost());
        maintenanceRecordService.updateById(record);

        // 恢复设备状态
        Equipment equipment = this.getById(record.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus("normal");
            this.updateById(equipment);
        }

        // 通知报修人
        if (record.getReporterId() != null) {
            String resultStr = updateInfo.getRepairResult() != null ? "，维修结果：" + updateInfo.getRepairResult() : "";
            notificationService.sendNotification(null, record.getReporterId(),
                    "设备维修完成",
                    "设备《" + (equipment != null ? equipment.getName() : "ID:" + record.getEquipmentId()) + "》已维修完成，可以正常使用" + resultStr,
                    "success");
        }
    }
}
