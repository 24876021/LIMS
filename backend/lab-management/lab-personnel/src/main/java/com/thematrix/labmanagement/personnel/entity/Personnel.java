package com.thematrix.labmanagement.personnel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人员信息表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("personnel")
@ApiModel(value="Personnel对象", description="人员信息表")
public class Personnel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "人员ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联系统用户ID")
    private Long userId;

    @ApiModelProperty(value = "系统用户名（非持久化）")
    @TableField(exist = false)
    private String userName;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "工号")
    private String employeeNo;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "关联部门ID")
    private Integer departmentId;

    @ApiModelProperty(value = "职位/角色")
    private String position;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
