package com.thematrix.labmanagement.common.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="RoleAuthority对象", description="角色-权限关联表")
public class RoleAuthority implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @ApiModelProperty(value = "权限ID")
    private Long authorityId;

}
