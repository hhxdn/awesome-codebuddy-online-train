package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.QaQuestion;
import com.onlinetrain.mapper.QaQuestionMapper;
import com.onlinetrain.service.QaQuestionService;
import org.springframework.stereotype.Service;

@Service
public class QaQuestionServiceImpl extends ServiceImpl<QaQuestionMapper, QaQuestion> implements QaQuestionService {
}
