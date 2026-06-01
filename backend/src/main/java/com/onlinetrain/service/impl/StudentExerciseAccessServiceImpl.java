package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.StudentExerciseAccess;
import com.onlinetrain.mapper.StudentExerciseAccessMapper;
import com.onlinetrain.service.StudentExerciseAccessService;
import org.springframework.stereotype.Service;

@Service
public class StudentExerciseAccessServiceImpl extends ServiceImpl<StudentExerciseAccessMapper, StudentExerciseAccess> implements StudentExerciseAccessService {
}
