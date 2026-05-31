package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.CourseReservation;
import com.onlinetrain.mapper.CourseReservationMapper;
import com.onlinetrain.service.CourseReservationService;
import org.springframework.stereotype.Service;

@Service
public class CourseReservationServiceImpl extends ServiceImpl<CourseReservationMapper, CourseReservation> implements CourseReservationService {
}
