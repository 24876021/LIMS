package com.thematrix.labmanagement.resources.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("usage_record")
@ApiModel(value="UsageRecord对象", description="物资领用记录表")
public class UsageRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "物资ID")
    private Long resourceId;
    @ApiModelProperty(value = "领用人ID")
    private Long userId;
    @ApiModelProperty(value = "领用数量")
    private Integer quantity;
    @ApiModelProperty(value = "用途")
    private String purpose;
    @ApiModelProperty(value = "领用时间")
    private LocalDateTime usageTime;
}
