package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.OfflineCheckin;
import com.onlinetrain.mapper.OfflineCheckinMapper;
import com.onlinetrain.service.OfflineCheckinService;
import org.springframework.stereotype.Service;

@Service
public class OfflineCheckinServiceImpl extends ServiceImpl<OfflineCheckinMapper, OfflineCheckin> implements OfflineCheckinService {
}
