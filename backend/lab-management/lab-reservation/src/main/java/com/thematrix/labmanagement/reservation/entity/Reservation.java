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
@TableName("reservation")
@ApiModel(value="Reservation对象", description="预约表")
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "实验室ID")
    private Long labId;
    @ApiModelProperty(value = "预约人ID")
    private Long userId;
    @ApiModelProperty(value = "预约开始时间")
    private LocalDateTime startTime;
    @ApiModelProperty(value = "预约结束时间")
    private LocalDateTime endTime;
    @ApiModelProperty(value = "预约目的")
    private String purpose;
    @ApiModelProperty(value = "状态：pending/approved/rejected/cancelled")
    private String status;
    @ApiModelProperty(value = "审批人ID")
    private Long approverId;
    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approveTime;
    @ApiModelProperty(value = "审批备注")
    private String approveRemark;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
