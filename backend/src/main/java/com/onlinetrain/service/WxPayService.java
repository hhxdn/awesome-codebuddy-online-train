package com.onlinetrain.service;

import java.util.Map;

/**
 * 微信支付服务接口
 */
public interface WxPayService {

    /**
     * JSAPI下单（获取prepay_id）
     *
     * @param orderNo    商户订单号
     * @param amount     金额（元）
     * @param description 商品描述
     * @param openid     用户openid
     * @return 前端调起支付需要的参数 Map
     */
    Map<String, String> jsapiPrepay(String orderNo, int amount, String description, String openid);

    /**
     * 通过code获取openid（OAuth流程）
     *
     * @param code 微信OAuth返回的code
     * @return openid
     */
    String getOpenidByCode(String code);

    /**
     * 验证微信支付回调签名
     *
     * @param body      回调请求体（JSON字符串）
     * @param timestamp 微信回调header中的时间戳
     * @param nonce     微信回调header中的随机串
     * @param signature 微信回调header中的签名
     * @param serialNo  微信回调header中的证书序列号
     * @return 验证通过返回解密的回调数据，失败返回null
     */
    Map<String, Object> verifyAndDecryptCallback(String body, String timestamp, String nonce,
                                                  String signature, String serialNo);

    /**
     * 查询订单状态
     *
     * @param orderNo 商户订单号
     * @return 订单状态: SUCCESS/REFUND/NOTPAY/CLOSED等
     */
    String queryOrder(String orderNo);
}
