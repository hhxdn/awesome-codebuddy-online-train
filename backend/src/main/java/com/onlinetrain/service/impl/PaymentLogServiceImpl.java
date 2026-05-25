package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.PaymentLog;
import com.onlinetrain.mapper.PaymentLogMapper;
import com.onlinetrain.service.PaymentLogService;
import org.springframework.stereotype.Service;

@Service
public class PaymentLogServiceImpl extends ServiceImpl<PaymentLogMapper, PaymentLog> implements PaymentLogService {
}
