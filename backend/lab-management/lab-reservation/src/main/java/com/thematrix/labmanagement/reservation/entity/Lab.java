package com.thematrix.labmanagement.reservation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lab")
@ApiModel(value="Lab对象", description="实验室表")
public class Lab implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "实验室名称")
    private String name;
    @ApiModelProperty(value = "实验室位置")
    private String location;
    @ApiModelProperty(value = "容量")
    private Integer capacity;
    @ApiModelProperty(value = "设备配置")
    private String equipment;
    @ApiModelProperty(value = "状态：available/reserved/maintenance")
    private String status;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
