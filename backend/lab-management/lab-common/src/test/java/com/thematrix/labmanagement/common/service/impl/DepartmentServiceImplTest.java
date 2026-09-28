package com.thematrix.labmanagement.common.service.impl;

import com.thematrix.labmanagement.common.entity.Department;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DepartmentServiceImpl 单元测试
 * 聚焦测试：addDepartment() 重复名称校验、updateDepartment() 重复校验和返回值检查
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Spy
    @InjectMocks
    private DepartmentServiceImpl departmentService;

    // ==================== addDepartment() ====================

    /**
     * 部门名称已存在时，addDepartment 应抛出 RuntimeException
     */
    @Test
    void addDepartment_duplicateName_throwsException() {
        Department existing = new Department();
        existing.setDepartmentName("实验室");
        doReturn(existing).when(departmentService).getOne(any());

        Department newDept = new Department();
        newDept.setDepartmentName("实验室");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> departmentService.addDepartment(newDept));
        assertEquals("部门名称「实验室」已存在，请勿重复添加", ex.getMessage());

        // 确认没有执行保存
        verify(departmentService, never()).save(any(Department.class));
    }

    // ==================== updateDepartment() ====================

    /**
     * 部门名称与其他部门重复时，updateDepartment 应抛出 RuntimeException
     */
    @Test
    void updateDepartment_duplicateName_throwsException() {
        Integer id = 1;
        Department existing = new Department();
        existing.setDepartmentName("实验室");
        doReturn(existing).when(departmentService).getOne(any());

        Department dept = new Department();
        dept.setDepartmentName("实验室");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> departmentService.updateDepartment(id, dept));
        assertEquals("部门名称「实验室」已存在，请勿重复", ex.getMessage());

        // 确认没有执行更新
        verify(departmentService, never()).updateById(any(Department.class));
    }

    /**
     * updateById 返回 false 时，updateDepartment 应抛出 RuntimeException
     */
    @Test
    void updateDepartment_updateFails_throwsException() {
        Integer id = 1;
        doReturn(null).when(departmentService).getOne(any());
        doReturn(false).when(departmentService).updateById(any(Department.class));

        Department dept = new Department();
        dept.setDepartmentName("新部门");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> departmentService.updateDepartment(id, dept));
        assertEquals("更新失败", ex.getMessage());

        // 确认没有同步 personnel 表（因为 updateById 失败了）
        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }
}
