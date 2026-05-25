package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.WrongQuestion;
import com.onlinetrain.mapper.WrongQuestionMapper;
import com.onlinetrain.service.WrongQuestionService;
import org.springframework.stereotype.Service;

@Service
public class WrongQuestionServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionService {
}
