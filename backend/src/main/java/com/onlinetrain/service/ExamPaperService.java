package com.onlinetrain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlinetrain.entity.ExamPaper;

import java.util.List;

public interface ExamPaperService extends IService<ExamPaper> {

    /**
     * 保存试卷关联的题目
     */
    void savePaperQuestions(Long paperId, List<Integer> questionIds);
}
