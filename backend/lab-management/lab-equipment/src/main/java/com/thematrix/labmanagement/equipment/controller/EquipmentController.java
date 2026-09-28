package com.thematrix.labmanagement.equipment.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.equipment.entity.Equipment;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;
import com.thematrix.labmanagement.equipment.service.EquipmentMaintenanceRecordService;
import com.thematrix.labmanagement.equipment.service.EquipmentService;
import com.thematrix.labmanagement.equipment.service.EquipmentUsageRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 设备管理控制器（重构版：业务逻辑已下沉到 Service 层）
 */
@RestController
@Api(tags = "设备管理相关接口")
@RequestMapping("/api")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentUsageRecordService usageRecordService;

    @Autowired
    private EquipmentMaintenanceRecordService maintenanceRecordService;

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:get')")
    @GetMapping("/equipment")
    @ApiOperation("获取设备列表")
    public Result getEquipmentList() {
        return Result.success(equipmentService.list());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:set')")
    @PostMapping("/equipment")
    @ApiOperation("新增设备")
    public Result addEquipment(@RequestBody Equipment equipment) {
        equipmentService.addEquipment(equipment);
        return Result.success("新增设备成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:set')")
    @PutMapping("/equipment/{id}")
    @ApiOperation("更新设备")
    public Result updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        equipmentService.updateEquipment(id, equipment);
        return Result.success("更新设备成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:remove')")
    @DeleteMapping("/equipment/{id}")
    @ApiOperation("删除设备")
    public Result deleteEquipment(@PathVariable Long id) {
        return equipmentService.removeById(id) ? Result.success("删除设备成功") : Result.error("删除设备失败");
    }

    @PostMapping("/equipment/{id}/borrow")
    @ApiOperation("借用设备")
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:get')")
    public Result borrowEquipment(@PathVariable Long id,
                                  @RequestParam Long userId,
                                  @RequestParam(required = false) String purpose) {
        equipmentService.borrowEquipment(id, userId, purpose);
        return Result.success("借用成功");
    }

    @PostMapping("/equipment/{id}/return")
    @ApiOperation("归还设备")
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:get')")
    public Result returnEquipment(@PathVariable Long id, @RequestParam Long userId) {
        equipmentService.returnEquipment(id, userId);
        return Result.success("归还成功");
    }

    @PostMapping("/equipment/{id}/maintenance")
    @ApiOperation("提交维修")
    public Result submitMaintenance(@PathVariable Long id, @RequestBody EquipmentMaintenanceRecord maintenanceRecord) {
        equipmentService.submitMaintenance(id, maintenanceRecord);
        return Result.success("提交维修成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:set')")
    @PostMapping("/equipment/{id}/maintenance/complete")
    @ApiOperation("完成维修")
    public Result completeMaintenance(@PathVariable Long id, @RequestBody EquipmentMaintenanceRecord maintenanceRecord) {
        equipmentService.completeMaintenance(maintenanceRecord.getId(), maintenanceRecord);
        return Result.success("维修完成");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:get')")
    @GetMapping("/equipment/{id}/maintenance-records")
    @ApiOperation("获取维修记录")
    public Result getMaintenanceRecords(@PathVariable Long id) {
        return Result.success(maintenanceRecordService.getByEquipmentId(id));
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('equipment:get')")
    @GetMapping("/equipment/{id}/usage-records")
    @ApiOperation("获取使用记录")
    public Result getUsageRecords(@PathVariable Long id) {
        return Result.success(usageRecordService.getByEquipmentId(id));
    }
}
