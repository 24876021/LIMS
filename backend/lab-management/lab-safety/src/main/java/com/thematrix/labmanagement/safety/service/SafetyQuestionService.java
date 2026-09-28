package com.thematrix.labmanagement.safety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.safety.entity.SafetyQuestion;

import java.util.List;

/**
 * 安全题目 服务类
 */
public interface SafetyQuestionService extends IService<SafetyQuestion> {

    List<SafetyQuestion> getByTrainingId(Long trainingId);
}
