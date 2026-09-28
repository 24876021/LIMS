package com.thematrix.labmanagement.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.safety.entity.SafetyQuestion;
import com.thematrix.labmanagement.safety.mapper.SafetyQuestionMapper;
import com.thematrix.labmanagement.safety.service.SafetyQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafetyQuestionServiceImpl extends ServiceImpl<SafetyQuestionMapper, SafetyQuestion>
        implements SafetyQuestionService {

    @Override
    public List<SafetyQuestion> getByTrainingId(Long trainingId) {
        return list(new LambdaQueryWrapper<SafetyQuestion>().eq(SafetyQuestion::getTrainingId, trainingId));
    }
}
