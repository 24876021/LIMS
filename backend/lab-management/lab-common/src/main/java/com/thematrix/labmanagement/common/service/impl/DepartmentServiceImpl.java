package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.entity.Department;
import com.thematrix.labmanagement.common.mapper.DepartmentMapper;
import com.thematrix.labmanagement.common.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDepartment(Department department) {
        Department exist = this.getOne(
                new LambdaQueryWrapper<Department>().eq(Department::getDepartmentName, department.getDepartmentName())
        );
        if (exist != null) {
            throw new RuntimeException("部门名称「" + department.getDepartmentName() + "」已存在，请勿重复添加");
        }
        department.setCreateTime(LocalDateTime.now());
        this.save(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(Integer id, Department department) {
        Department exist = this.getOne(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getDepartmentName, department.getDepartmentName())
                        .ne(Department::getDepartmentId, id)
        );
        if (exist != null) {
            throw new RuntimeException("部门名称「" + department.getDepartmentName() + "」已存在，请勿重复");
        }

        department.setDepartmentId(id);
        boolean success = this.updateById(department);
        if (!success) {
            throw new RuntimeException("更新失败");
        }

        // 同步更新 personnel 表中的部门名称（仅在 updateById 成功后执行）
        if (department.getDepartmentName() != null && jdbcTemplate != null) {
            jdbcTemplate.update("UPDATE personnel SET department = ? WHERE department_id = ?",
                    department.getDepartmentName(), id);
        }
    }
}
