package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线下考试预约实体
 */
@Data
@TableName("exam_reservation")
public class ExamReservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 线下考试试卷ID */
    private Long examPaperId;

    /** 学员ID */
    private Long userId;

    /** 预约考试时间 */
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
