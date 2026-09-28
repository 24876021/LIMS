package com.thematrix.labmanagement.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备维修记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("equipment_maintenance_record")
@ApiModel(value="EquipmentMaintenanceRecord对象", description="设备维修记录表")
public class EquipmentMaintenanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "设备ID")
    private Long equipmentId;

    @ApiModelProperty(value = "报修人ID")
    private Long reporterId;

    @ApiModelProperty(value = "故障描述")
    private String faultDescription;

    @ApiModelProperty(value = "维修状态：pending/in_progress/completed")
    private String status;

    @ApiModelProperty(value = "维修人")
    private String repairPerson;

    @ApiModelProperty(value = "维修费用")
    private String repairCost;

    @ApiModelProperty(value = "报修时间")
    private LocalDateTime reportTime;

    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completeTime;

    @ApiModelProperty(value = "维修结果")
    private String repairResult;
}
