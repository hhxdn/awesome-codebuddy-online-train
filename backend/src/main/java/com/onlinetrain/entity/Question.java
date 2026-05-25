package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long chapterId;
    private String type;
    private String content;
    private Integer score;
    private String answer;
    private String analysis;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    /**
     * 选项列表（非数据库字段，仅用于前端展示）
     * SINGLE/MULTIPLE: 选项内容字符串列表
     * JUDGE: 前端硬编码['正确','错误']，无需从DB加载
     */
    @TableField(exist = false)
    private List<String> options;
}
