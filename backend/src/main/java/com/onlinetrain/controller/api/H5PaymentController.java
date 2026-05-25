package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Order;
import com.onlinetrain.entity.PaymentLog;
import com.onlinetrain.service.OrderService;
import com.onlinetrain.service.PaymentLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * H5支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/h5/payment")
@Api(tags = "H5-支付接口")
public class H5PaymentController {

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付回调（模拟）
     */
    @PostMapping("/callback/wechat")
    @ApiOperation("微信支付回调")
    public Result<Void> wechatCallback(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : "";
        String transactionId = params.get("transactionId") != null ? params.get("transactionId").toString() : "";

        log.info("微信支付回调: orderNo={}, transactionId={}", orderNo, transactionId);

        // 记录支付日志
        PaymentLog log = new PaymentLog();
        log.setOrderNo(orderNo);
        log.setTransactionId(transactionId);
        log.setPayMethod("WECHAT");
        log.setStatus("SUCCESS");
        log.setCallbackData(params.toString());
        paymentLogService.save(log);

        // 更新订单状态
        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderService.updateById(order);
        }

        return Result.ok();
    }

    /**
     * 支付宝支付回调（模拟）
     */
    @PostMapping("/callback/alipay")
    @ApiOperation("支付宝支付回调")
    public Result<Void> alipayCallback(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : "";
        String transactionId = params.get("transactionId") != null ? params.get("transactionId").toString() : "";

        log.info("支付宝支付回调: orderNo={}, transactionId={}", orderNo, transactionId);

        // 记录支付日志
        PaymentLog log = new PaymentLog();
        log.setOrderNo(orderNo);
        log.setTransactionId(transactionId);
        log.setPayMethod("ALIPAY");
        log.setStatus("SUCCESS");
        log.setCallbackData(params.toString());
        paymentLogService.save(log);

        // 更新订单状态
        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderService.updateById(order);
        }

        return Result.ok();
    }
}
