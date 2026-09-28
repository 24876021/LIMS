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
@TableName("safety_exam_record")
@ApiModel(value="SafetyExamRecord对象", description="安全考试记录表")
public class SafetyExamRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "培训ID")
    private Long trainingId;
    @ApiModelProperty(value = "得分")
    private Integer score;
    @ApiModelProperty(value = "总分")
    private Integer totalScore;
    @ApiModelProperty(value = "是否通过")
    private Boolean passed;
    @ApiModelProperty(value = "状态：0=待完成，1=已完成")
    private Integer status;
    @ApiModelProperty(value = "考试时间")
    private LocalDateTime examTime;
}
