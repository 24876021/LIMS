package com.thematrix.labmanagement.personnel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考勤记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attendance_record")
@ApiModel(value="AttendanceRecord对象", description="考勤记录表")
public class AttendanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "人员ID")
    private Long personnelId;

    @ApiModelProperty(value = "活动ID")
    private Long activityId;

    @ApiModelProperty(value = "签到时间")
    private LocalDateTime signTime;

    @ApiModelProperty(value = "状态：normal/late/early_leave/absent")
    private String status;
}
