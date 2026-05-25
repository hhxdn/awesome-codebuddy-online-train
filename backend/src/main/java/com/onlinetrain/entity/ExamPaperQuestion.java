package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examPaperId;
    private Long questionId;
    private Integer sortOrder;
}
