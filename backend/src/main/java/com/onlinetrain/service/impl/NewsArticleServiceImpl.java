package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.NewsArticle;
import com.onlinetrain.mapper.NewsArticleMapper;
import com.onlinetrain.service.NewsArticleService;
import org.springframework.stereotype.Service;

@Service
public class NewsArticleServiceImpl extends ServiceImpl<NewsArticleMapper, NewsArticle> implements NewsArticleService {
}
