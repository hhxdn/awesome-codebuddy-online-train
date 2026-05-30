package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.Order;
import com.onlinetrain.entity.PaymentLog;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.OrderService;
import com.onlinetrain.service.PaymentLogService;
import com.onlinetrain.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * H5支付控制器（微信支付回调）
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@Api(tags = "H5-支付回调接口")
public class H5PaymentController {

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private CourseService courseService;

    /**
     * 微信支付回调（真实验签+解密）
     */
    @PostMapping("/callback/wechat")
    @ApiOperation("微信支付回调")
    public Map<String, String> wechatCallback(HttpServletRequest request) {
        try {
            // 1. 读取回调请求体（原始JSON字符串）
            BufferedReader reader = request.getReader();
            StringBuilder bodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
            String bodyJson = bodyBuilder.toString();

            // 2. 获取微信回调签名头
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");
            String serialNo = request.getHeader("Wechatpay-Serial");

            log.info("微信支付回调: timestamp={}, nonce={}, serialNo={}", timestamp, nonce, serialNo);

            // 3. 验签并解密
            Map<String, Object> decrypted = wxPayService.verifyAndDecryptCallback(
                    bodyJson, timestamp, nonce, signature, serialNo);

            if (decrypted == null) {
                log.error("支付回调验签失败");
                return mapOf("code", "FAIL", "message", "验签失败");
            }

            // 4. 从解密数据获取订单信息
            String orderNo = (String) decrypted.get("out_trade_no");
            String transactionId = (String) decrypted.get("transaction_id");
            String tradeState = (String) decrypted.get("trade_state");
            Object amountObj = decrypted.get("amount");
            String payerOpenid = null;
            if (decrypted.get("payer") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payer = (Map<String, Object>) decrypted.get("payer");
                payerOpenid = (String) payer.get("openid");
            }

            log.info("支付成功: orderNo={}, transactionId={}, tradeState={}", orderNo, transactionId, tradeState);

            // 5. 查询订单并更新状态
            Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();
            if (order == null) {
                log.error("订单不存在: {}", orderNo);
                return mapOf("code", "FAIL", "message", "订单不存在");
            }

            if ("PAID".equals(order.getStatus())) {
                // 重复通知，直接返回成功
                log.info("订单已支付，忽略重复通知: {}", orderNo);
                return mapOf("code", "SUCCESS", "message", "OK");
            }

            // 记录支付日志
            PaymentLog paymentLog = new PaymentLog();
            paymentLog.setOrderId(order.getId());
            paymentLog.setOrderNo(orderNo);
            paymentLog.setTransactionId(transactionId);
            paymentLog.setPayMethod("WECHAT");
            if (amountObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> amt = (Map<String, Object>) amountObj;
                Object total = amt.get("total");
                if (total != null) {
                    paymentLog.setAmount(new java.math.BigDecimal(total.toString()).divide(new java.math.BigDecimal("100")));
                }
            }
            paymentLog.setStatus("SUCCESS");
            paymentLog.setCallbackData(bodyJson);
            paymentLogService.save(paymentLog);

            // 更新订单状态
            order.setStatus("PAID");
            order.setPayTime(LocalDateTime.now());
            orderService.updateById(order);

            // 如果用户已购买该课程，无需重复处理学员-课程关系
            // （实际项目可根据需要自动开通课程权限）

            return mapOf("code", "SUCCESS", "message", "OK");

        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return mapOf("code", "FAIL", "message", e.getMessage());
        }
    }

    /**
     * 支付宝支付回调（暂未实现真实接口，保留原有模拟逻辑）
     */
    @PostMapping("/callback/alipay")
    @ApiOperation("支付宝支付回调")
    public Result<Void> alipayCallback(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : "";
        String transactionId = params.get("transactionId") != null ? params.get("transactionId").toString() : "";

        log.info("支付宝支付回调: orderNo={}, transactionId={}", orderNo, transactionId);

        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();

        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setOrderNo(orderNo);
        paymentLog.setTransactionId(transactionId);
        paymentLog.setPayMethod("ALIPAY");
        paymentLog.setStatus("SUCCESS");
        paymentLog.setCallbackData(params.toString());
        if (order != null) {
            paymentLog.setOrderId(order.getId());
            paymentLog.setAmount(order.getAmount());
        }
        paymentLogService.save(paymentLog);

        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderService.updateById(order);
        }

        return Result.ok();
    }

    private Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}
