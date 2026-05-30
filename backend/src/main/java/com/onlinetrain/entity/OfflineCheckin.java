package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线下课程打卡记录实体
 */
@Data
@TableName("offline_checkin")
public class OfflineCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 线下课程ID */
    private Long courseId;

    /** 打卡时经度 */
    private BigDecimal checkinLongitude;

    /** 打卡时纬度 */
    private BigDecimal checkinLatitude;

    /** 打卡距离(米) */
    private Integer distance;

    /** 打卡方式: SELF自主打卡 / ADMIN后台代打卡 */
    private String checkinType;

    /** 操作人ID(后台代打卡时记录) */
    private Long operatorId;

    /** 状态 1有效 */
    private Integer status;

    /** 打卡时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
