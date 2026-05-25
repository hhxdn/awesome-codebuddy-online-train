package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.mapper.LearningRecordMapper;
import com.onlinetrain.service.LearningRecordService;
import org.springframework.stereotype.Service;

@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements LearningRecordService {
}
