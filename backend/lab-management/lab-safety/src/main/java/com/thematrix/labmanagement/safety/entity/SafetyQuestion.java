package com.thematrix.labmanagement.safety.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("safety_question")
@ApiModel(value="SafetyQuestion对象", description="安全考试题目表")
public class SafetyQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "题目内容")
    private String question;
    @ApiModelProperty(value = "选项A")
    private String optionA;
    @ApiModelProperty(value = "选项B")
    private String optionB;
    @ApiModelProperty(value = "选项C")
    private String optionC;
    @ApiModelProperty(value = "选项D")
    private String optionD;
    @ApiModelProperty(value = "正确答案")
    private String answer;
    @ApiModelProperty(value = "题目类型")
    private String type;
    @ApiModelProperty(value = "关联培训ID")
    private Long trainingId;
}
