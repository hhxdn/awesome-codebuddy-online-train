package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.Certificate;
import com.onlinetrain.mapper.CertificateMapper;
import com.onlinetrain.service.CertificateService;
import org.springframework.stereotype.Service;

@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {
}
