package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long courseId;

    /** 购买类型: COURSE / CATEGORY */
    private String productType;

    /** 产品ID（课程ID或分类ID） */
    private Long productId;

    private BigDecimal amount;
    private String payMethod;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime payTime;
    private LocalDateTime expireTime;

    @TableLogic
    private Integer deleted;
}
