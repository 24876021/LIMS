package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.entity.Department;
import com.thematrix.labmanagement.common.service.DepartmentService;
import com.thematrix.labmanagement.common.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "部门管理相关接口")
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:get') || hasAuthority('personnel:all')")
    @GetMapping
    @ApiOperation("获取部门列表")
    public Result getDepartments() {
        List<Department> list = departmentService.list();
        return Result.success(list);
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set') || hasAuthority('personnel:all')")
    @PostMapping
    @ApiOperation("新增部门")
    public Result addDepartment(@RequestBody Department department) {
        try {
            departmentService.addDepartment(department);
            return Result.success("新增成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:set') || hasAuthority('personnel:all')")
    @PutMapping("/{id}")
    @ApiOperation("更新部门")
    public Result updateDepartment(@PathVariable Integer id, @RequestBody Department department) {
        try {
            departmentService.updateDepartment(id, department);
            return Result.success("更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('personnel:remove') || hasAuthority('personnel:all')")
    @DeleteMapping("/{id}")
    @ApiOperation("删除部门")
    public Result deleteDepartment(@PathVariable Integer id) {
        return departmentService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
