package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.ExamPaperQuestion;
import com.onlinetrain.mapper.ExamPaperQuestionMapper;
import com.onlinetrain.service.ExamPaperQuestionService;
import org.springframework.stereotype.Service;

@Service
public class ExamPaperQuestionServiceImpl extends ServiceImpl<ExamPaperQuestionMapper, ExamPaperQuestion> implements ExamPaperQuestionService {
}
