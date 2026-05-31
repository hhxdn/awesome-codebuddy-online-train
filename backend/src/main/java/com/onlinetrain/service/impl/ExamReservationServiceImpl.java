package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.ExamReservation;
import com.onlinetrain.mapper.ExamReservationMapper;
import com.onlinetrain.service.ExamReservationService;
import org.springframework.stereotype.Service;

@Service
public class ExamReservationServiceImpl extends ServiceImpl<ExamReservationMapper, ExamReservation> implements ExamReservationService {
}
