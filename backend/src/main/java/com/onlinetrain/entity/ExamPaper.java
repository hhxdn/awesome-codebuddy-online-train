package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_paper")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private Integer durationMinutes;
    private Integer totalScore;
    private Integer passScore;
    private Integer maxAttempts;
    private String status;
    /** 考试类型: ONLINE线上考试 / OFFLINE线下考试 */
    private String examType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
