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
 * 设备使用记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("equipment_usage_record")
@ApiModel(value="EquipmentUsageRecord对象", description="设备使用记录表")
public class EquipmentUsageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "设备ID")
    private Long equipmentId;

    @ApiModelProperty(value = "使用人ID")
    private Long userId;

    @ApiModelProperty(value = "借出时间")
    private LocalDateTime borrowTime;

    @ApiModelProperty(value = "归还时间")
    private LocalDateTime returnTime;

    @ApiModelProperty(value = "用途")
    private String purpose;

    @ApiModelProperty(value = "状态：borrowing/returned")
    private String status;
}
