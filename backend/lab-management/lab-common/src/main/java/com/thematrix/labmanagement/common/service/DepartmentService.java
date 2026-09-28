package com.thematrix.labmanagement.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.common.entity.Department;

public interface DepartmentService extends IService<Department> {

    /**
     * 新增部门（含名称唯一性检查）
     */
    void addDepartment(Department department);

    /**
     * 更新部门（含名称唯一性检查 + personnel表同步）
     */
    void updateDepartment(Integer id, Department department);
}
