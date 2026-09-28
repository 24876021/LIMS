package com.thematrix.labmanagement.resources.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.resources.entity.PurchaseRecord;
import com.thematrix.labmanagement.resources.entity.Resource;
import com.thematrix.labmanagement.resources.entity.UsageRecord;
import com.thematrix.labmanagement.resources.service.ResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Api(tags = "资源管理相关接口")
@RequestMapping("/api")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:get')")
    @GetMapping("/resources")
    @ApiOperation("物资列表")
    public Result getResources() {
        return Result.success(resourceService.list());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:set')")
    @PostMapping("/resources")
    @ApiOperation("新增物资")
    public Result addResource(@RequestBody Resource resource) {
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());
        return resourceService.save(resource) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:set')")
    @PutMapping("/resources/{id}")
    @ApiOperation("更新物资")
    public Result updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        resource.setId(id);
        resource.setUpdateTime(LocalDateTime.now());
        return resourceService.updateById(resource) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:remove')")
    @DeleteMapping("/resources/{id}")
    @ApiOperation("删除物资")
    public Result deleteResource(@PathVariable Long id) {
        return resourceService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:set')")
    @PostMapping("/resources/{id}/purchase")
    @ApiOperation("采购入库")
    public Result purchase(@PathVariable Long id, @RequestBody PurchaseRecord record) {
        resourceService.purchase(id, record);
        return Result.success("入库成功");
    }

    @PostMapping("/resources/{id}/usage")
    @ApiOperation("物资领用")
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:get')")
    public Result usage(@PathVariable Long id, @RequestBody UsageRecord record) {
        resourceService.consume(id, record);
        return Result.success("领用成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:get')")
    @GetMapping("/resources/{id}/records")
    @ApiOperation("获取记录")
    public Result getRecords(@PathVariable Long id) {
        return Result.success(resourceService.getRecords(id));
    }
}
