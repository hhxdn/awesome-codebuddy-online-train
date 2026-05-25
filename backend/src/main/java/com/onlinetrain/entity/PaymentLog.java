package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_log")
public class PaymentLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String transactionId;
    private String payMethod;
    private BigDecimal amount;
    private String status;
    private String callbackData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
