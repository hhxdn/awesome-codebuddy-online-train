package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线下课程预约实体
 */
@Data
@TableName("course_reservation")
public class CourseReservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 线下课程ID */
    private Long courseId;

    /** 学员ID */
    private Long userId;

    /** 预约课程时间 */
    private LocalDateTime reservationTime;

    /** 预约状态: PENDING待确认 / CONFIRMED已确认 / CANCELLED已取消 / COMPLETED已完成 */
    private String status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
