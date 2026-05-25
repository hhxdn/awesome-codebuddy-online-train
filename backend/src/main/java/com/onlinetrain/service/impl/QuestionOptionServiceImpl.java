package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.QuestionOption;
import com.onlinetrain.mapper.QuestionOptionMapper;
import com.onlinetrain.service.QuestionOptionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionOptionServiceImpl extends ServiceImpl<QuestionOptionMapper, QuestionOption> implements QuestionOptionService {
}
