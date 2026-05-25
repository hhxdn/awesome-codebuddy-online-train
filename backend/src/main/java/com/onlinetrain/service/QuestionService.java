package com.onlinetrain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlinetrain.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {

    /**
     * 获取试卷关联的题目列表
     */
    List<Question> getExamPaperQuestions(Long paperId);

    /**
     * 为题目列表加载选项（从question_option表），并转换答案为前端索引格式
     * 适用于展示/答题场景（返回给前端的数据）
     */
    void enrichForDisplay(List<Question> questions);

    /**
     * 将题目的正确答案从字母格式转换为前端索引格式（用于评分比对）
     * SINGLE: A->0, B->1; MULTIPLE: AB->"0,1"; JUDGE: T->0, F->1
     */
    String toIndexAnswer(Question question);
}
