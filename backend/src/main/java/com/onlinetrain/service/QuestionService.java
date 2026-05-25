package com.onlinetrain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlinetrain.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {

    /**
     * 获取试卷关联的题目列表
     */
    List<Question> getExamPaperQuestions(Long paperId);
}
