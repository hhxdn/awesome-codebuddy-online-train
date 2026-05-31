package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.Banner;
import com.onlinetrain.mapper.BannerMapper;
import com.onlinetrain.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
