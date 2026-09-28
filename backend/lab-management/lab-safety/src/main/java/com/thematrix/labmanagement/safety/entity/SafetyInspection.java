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
@TableName("safety_inspection")
@ApiModel(value="SafetyInspection对象", description="安全检查表")
public class SafetyInspection implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "检查标题")
    private String title;
    @ApiModelProperty(value = "检查内容")
    private String content;
    @ApiModelProperty(value = "检查人ID")
    private Long inspectorId;
    @ApiModelProperty(value = "检查结果")
    private String result;
    @ApiModelProperty(value = "检查时间")
    private LocalDateTime inspectionTime;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
