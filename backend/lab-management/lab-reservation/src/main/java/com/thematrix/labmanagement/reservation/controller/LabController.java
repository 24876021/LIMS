package com.thematrix.labmanagement.reservation.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.reservation.entity.Lab;
import com.thematrix.labmanagement.reservation.service.LabService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 实验室管理控制器（重构版：业务逻辑已下沉到 Service 层）
 */
@RestController
@Api(tags = "实验室管理相关接口")
@RequestMapping("/api")
public class LabController {

    @Autowired
    private LabService labService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:get')")
    @GetMapping("/labs")
    @ApiOperation("实验室列表")
    public Result getLabs() {
        return Result.success(labService.list());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:set')")
    @PostMapping("/labs")
    @ApiOperation("新增实验室")
    public Result addLab(@RequestBody Lab lab) {
        return labService.saveLab(lab) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:set')")
    @PutMapping("/labs/{id}")
    @ApiOperation("更新实验室")
    public Result updateLab(@PathVariable Long id, @RequestBody Lab lab) {
        return labService.updateLab(id, lab) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('reservation:remove')")
    @DeleteMapping("/labs/{id}")
    @ApiOperation("删除实验室")
    public Result deleteLab(@PathVariable Long id) {
        return labService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
