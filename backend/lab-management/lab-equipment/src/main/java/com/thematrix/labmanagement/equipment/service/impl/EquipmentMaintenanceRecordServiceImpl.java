package com.thematrix.labmanagement.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;
import com.thematrix.labmanagement.equipment.mapper.EquipmentMaintenanceRecordMapper;
import com.thematrix.labmanagement.equipment.service.EquipmentMaintenanceRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentMaintenanceRecordServiceImpl
        extends ServiceImpl<EquipmentMaintenanceRecordMapper, EquipmentMaintenanceRecord>
        implements EquipmentMaintenanceRecordService {

    @Override
    public List<EquipmentMaintenanceRecord> getByEquipmentId(Long equipmentId) {
        return list(new LambdaQueryWrapper<EquipmentMaintenanceRecord>()
                .eq(EquipmentMaintenanceRecord::getEquipmentId, equipmentId));
    }
}
