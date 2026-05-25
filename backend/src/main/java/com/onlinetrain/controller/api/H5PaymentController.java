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

        // 查询订单
        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();

        // 记录支付日志
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setOrderNo(orderNo);
        paymentLog.setTransactionId(transactionId);
        paymentLog.setPayMethod("WECHAT");
        paymentLog.setStatus("SUCCESS");
        paymentLog.setCallbackData(params.toString());
        if (order != null) {
            paymentLog.setOrderId(order.getId());
            paymentLog.setAmount(order.getAmount());
        }
        paymentLogService.save(paymentLog);

        // 更新订单状态
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

        // 查询订单
        Order order = orderService.lambdaQuery().eq(Order::getOrderNo, orderNo).one();

        // 记录支付日志
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

        // 更新订单状态
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderService.updateById(order);
        }

        return Result.ok();
    }
}
