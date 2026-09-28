package com.thematrix.labmanagement.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("departments")
@ApiModel(value="Department对象", description="部门表")
public class Department implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "department_id", type = IdType.AUTO)
    private Integer departmentId;

    @ApiModelProperty(value = "部门名称")
    private String departmentName;

    @ApiModelProperty(value = "上级部门ID")
    private Integer parentId;

    @ApiModelProperty(value = "部门负责人ID")
    private Integer leaderUserId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
