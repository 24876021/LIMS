package com.thematrix.labmanagement.safety.service.impl;

import com.thematrix.labmanagement.safety.entity.SafetyTraining;
import com.thematrix.labmanagement.safety.mapper.SafetyTrainingMapper;
import com.thematrix.labmanagement.safety.service.SafetyTrainingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SafetyTrainingServiceImpl extends ServiceImpl<SafetyTrainingMapper, SafetyTraining>
        implements SafetyTrainingService {

    @Override
    public boolean saveTraining(SafetyTraining training) {
        training.setCreateTime(LocalDateTime.now());
        return save(training);
    }

    @Override
    public boolean updateTraining(Long id, SafetyTraining training) {
        training.setId(id);
        return updateById(training);
    }
}
