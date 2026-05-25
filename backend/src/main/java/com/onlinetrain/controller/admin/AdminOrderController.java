package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Order;
import com.onlinetrain.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@Api(tags = "管理端-订单管理")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @ApiOperation("订单列表")
    public Result<PageResult<Order>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Order> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(Order::getOrderNo, orderNo);
        if (courseId != null) wrapper.eq(Order::getCourseId, courseId);
        if (startDate != null && !startDate.isEmpty()) wrapper.ge(Order::getCreateTime, startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) wrapper.le(Order::getCreateTime, endDate + " 23:59:59");
        wrapper.orderByDesc(Order::getCreateTime);

        return Result.ok(PageResult.of(orderService.page(pageParam, wrapper)));
    }
}
