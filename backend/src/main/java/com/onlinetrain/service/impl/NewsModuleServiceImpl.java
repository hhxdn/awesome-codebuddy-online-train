package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.NewsModule;
import com.onlinetrain.mapper.NewsModuleMapper;
import com.onlinetrain.service.NewsModuleService;
import org.springframework.stereotype.Service;

@Service
public class NewsModuleServiceImpl extends ServiceImpl<NewsModuleMapper, NewsModule> implements NewsModuleService {
}
