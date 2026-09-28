package com.thematrix.labmanagement.resources.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_record")
@ApiModel(value="PurchaseRecord对象", description="采购记录表")
public class PurchaseRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "物资ID")
    private Long resourceId;
    @ApiModelProperty(value = "采购数量")
    private Integer quantity;
    @ApiModelProperty(value = "采购单价")
    private BigDecimal unitPrice;
    @ApiModelProperty(value = "采购人ID")
    private Long purchaserId;
    @ApiModelProperty(value = "供应商")
    private String supplier;
    @ApiModelProperty(value = "采购时间")
    private LocalDateTime purchaseTime;
}
