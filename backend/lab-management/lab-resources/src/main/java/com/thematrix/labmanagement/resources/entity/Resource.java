package com.thematrix.labmanagement.resources.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("resource")
@ApiModel(value="Resource对象", description="物资表")
public class Resource implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "物资名称")
    private String name;
    @ApiModelProperty(value = "物资分类")
    private String category;
    @ApiModelProperty(value = "规格型号")
    private String specification;
    @ApiModelProperty(value = "库存数量")
    private Integer quantity;
    @ApiModelProperty(value = "单位")
    private String unit;
    @ApiModelProperty(value = "单价")
    private BigDecimal price;
    @ApiModelProperty(value = "存放位置")
    private String location;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
