package com.thematrix.labmanagement.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.equipment.entity.EquipmentUsageRecord;
import com.thematrix.labmanagement.equipment.mapper.EquipmentUsageRecordMapper;
import com.thematrix.labmanagement.equipment.service.EquipmentUsageRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentUsageRecordServiceImpl
        extends ServiceImpl<EquipmentUsageRecordMapper, EquipmentUsageRecord>
        implements EquipmentUsageRecordService {

    @Override
    public List<EquipmentUsageRecord> getByEquipmentId(Long equipmentId) {
        return list(new LambdaQueryWrapper<EquipmentUsageRecord>()
                .eq(EquipmentUsageRecord::getEquipmentId, equipmentId));
    }
}
