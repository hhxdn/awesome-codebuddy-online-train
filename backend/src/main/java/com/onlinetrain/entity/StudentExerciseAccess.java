package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_exercise_access")
public class StudentExerciseAccess {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
