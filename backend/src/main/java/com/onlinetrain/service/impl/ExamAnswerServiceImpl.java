package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.ExamAnswer;
import com.onlinetrain.mapper.ExamAnswerMapper;
import com.onlinetrain.service.ExamAnswerService;
import org.springframework.stereotype.Service;

@Service
public class ExamAnswerServiceImpl extends ServiceImpl<ExamAnswerMapper, ExamAnswer> implements ExamAnswerService {
}
