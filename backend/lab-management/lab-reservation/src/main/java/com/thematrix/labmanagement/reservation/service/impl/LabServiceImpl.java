package com.thematrix.labmanagement.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.reservation.entity.Lab;
import com.thematrix.labmanagement.reservation.mapper.LabMapper;
import com.thematrix.labmanagement.reservation.service.LabService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LabServiceImpl extends ServiceImpl<LabMapper, Lab> implements LabService {

    @Override
    public boolean saveLab(Lab lab) {
        lab.setCreateTime(LocalDateTime.now());
        lab.setUpdateTime(LocalDateTime.now());
        lab.setStatus("available");
        return save(lab);
    }

    @Override
    public boolean updateLab(Long id, Lab lab) {
        lab.setId(id);
        lab.setUpdateTime(LocalDateTime.now());
        return updateById(lab);
    }
}
