package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.mapper.CourseCategoryMapper;
import com.onlinetrain.service.CourseCategoryService;
import org.springframework.stereotype.Service;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
}
