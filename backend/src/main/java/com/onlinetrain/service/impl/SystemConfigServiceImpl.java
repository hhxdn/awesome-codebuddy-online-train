package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.mapper.SystemConfigMapper;
import com.onlinetrain.service.SystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
}
