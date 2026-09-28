package com.thematrix.labmanagement.reports.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("report")
@ApiModel(value="Report对象", description="实验报告表")
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "报告标题")
    private String title;
    @ApiModelProperty(value = "报告内容")
    private String content;
    @ApiModelProperty(value = "提交人ID")
    private Long userId;
    @ApiModelProperty(value = "状态：pending/approved/rejected")
    private String status;
    @ApiModelProperty(value = "评分")
    private Integer score;
    @ApiModelProperty(value = "评语")
    private String comment;
    @ApiModelProperty(value = "审批人ID")
    private Long reviewerId;
    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;
    @ApiModelProperty(value = "审批时间")
    private LocalDateTime reviewTime;
    @ApiModelProperty(value = "附件URL（多个用逗号分隔）")
    private String attachment;
    @ApiModelProperty(value = "课程名称")
    private String course;
}
