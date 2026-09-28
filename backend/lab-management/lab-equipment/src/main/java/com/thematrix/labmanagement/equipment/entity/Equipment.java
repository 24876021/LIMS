package com.thematrix.labmanagement.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("equipment")
@ApiModel(value="Equipment对象", description="设备表")
public class Equipment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "设备名称")
    private String name;

    @ApiModelProperty(value = "设备编号")
    private String code;

    @ApiModelProperty(value = "设备分类")
    private String category;

    @ApiModelProperty(value = "设备位置")
    private String location;

    @ApiModelProperty(value = "设备状态：normal/borrowed/maintenance/scrapped")
    private String status;

    @ApiModelProperty(value = "购买日期")
    private LocalDateTime purchaseDate;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
