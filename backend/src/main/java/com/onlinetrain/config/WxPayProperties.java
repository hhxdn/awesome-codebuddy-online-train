package com.onlinetrain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayProperties {
    /** 微信公众号AppID */
    private String appId;
    /** 微信公众号AppSecret */
    private String appSecret;
    /** 微信商户号 */
    private String mchId;
    /** API v3密钥（32位，在商户平台设置） */
    private String apiV3Key;
    /** 商户私钥证书路径（classpath: 或绝对路径） */
    private String privateKeyPath;
    /** 商户证书序列号（在商户平台查看） */
    private String serialNo;
    /** 支付结果通知地址（必须是公网可访问的URL） */
    private String notifyUrl;
}
