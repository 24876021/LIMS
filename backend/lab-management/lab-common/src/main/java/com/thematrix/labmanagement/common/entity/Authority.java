package com.thematrix.labmanagement.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统权限表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Authority对象", description="系统权限表")
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "权限ID")
    @TableId(value = "authority_id", type = IdType.AUTO)
    private Long authorityId;

    @ApiModelProperty(value = "权限标识")
    private String authorityName;

    @ApiModelProperty(value = "权限描述")
    private String description;

}
