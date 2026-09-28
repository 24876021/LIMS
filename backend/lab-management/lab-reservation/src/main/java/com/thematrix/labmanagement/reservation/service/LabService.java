package com.thematrix.labmanagement.reservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.reservation.entity.Lab;

/**
 * 实验室 服务类
 */
public interface LabService extends IService<Lab> {

    boolean saveLab(Lab lab);

    boolean updateLab(Long id, Lab lab);
}
