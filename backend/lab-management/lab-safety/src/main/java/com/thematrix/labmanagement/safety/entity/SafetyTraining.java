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
@TableName("safety_training")
@ApiModel(value="SafetyTraining对象", description="安全培训表")
public class SafetyTraining implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "培训标题")
    private String title;
    @ApiModelProperty(value = "培训内容")
    private String content;
    @ApiModelProperty(value = "培训时间")
    private LocalDateTime trainingTime;
    @ApiModelProperty(value = "培训地点")
    private String location;
    @ApiModelProperty(value = "创建人ID")
    private Long createdBy;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
