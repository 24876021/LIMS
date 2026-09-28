package com.thematrix.labmanagement.safety.service.impl;

import com.thematrix.labmanagement.safety.entity.SafetyRegulation;
import com.thematrix.labmanagement.safety.mapper.SafetyRegulationMapper;
import com.thematrix.labmanagement.safety.service.SafetyRegulationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SafetyRegulationServiceImpl extends ServiceImpl<SafetyRegulationMapper, SafetyRegulation>
        implements SafetyRegulationService {

    @Override
    public boolean saveRegulation(SafetyRegulation regulation) {
        regulation.setCreateTime(LocalDateTime.now());
        regulation.setUpdateTime(LocalDateTime.now());
        return save(regulation);
    }

    @Override
    public boolean updateRegulation(Long id, SafetyRegulation regulation) {
        regulation.setId(id);
        regulation.setUpdateTime(LocalDateTime.now());
        return updateById(regulation);
    }
}
