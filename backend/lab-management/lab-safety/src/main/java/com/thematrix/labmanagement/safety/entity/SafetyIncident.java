package com.thematrix.labmanagement.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("safety_incident")
@ApiModel(value="SafetyIncident对象", description="安全事故表")
public class SafetyIncident implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "事故标题")
    private String title;
    @ApiModelProperty(value = "事故描述")
    private String description;
    @ApiModelProperty(value = "事故等级")
    private String level;
    @ApiModelProperty(value = "处理状态")
    private String status;
    @ApiModelProperty(value = "报告人ID")
    private Long reporterId;
    @ApiModelProperty(value = "发生时间")
    private LocalDateTime incidentTime;
    @ApiModelProperty(value = "发生地点")
    private String location;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
