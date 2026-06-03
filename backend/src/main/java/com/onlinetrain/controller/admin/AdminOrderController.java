package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.Order;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.OrderService;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@Api(tags = "管理端-订单管理")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    @ApiOperation("订单列表")
    public Result<PageResult<Map<String, Object>>> list(
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

        Page<Order> orderPage = orderService.page(pageParam, wrapper);

        // 填充学员名称和课程名称
        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("userId", order.getUserId());
            item.put("courseId", order.getCourseId());
            item.put("amount", order.getAmount());
            item.put("payMethod", order.getPayMethod());
            item.put("status", order.getStatus());
            item.put("createTime", order.getCreateTime());
            item.put("payTime", order.getPayTime());
            item.put("expireTime", order.getExpireTime());

            // 学员名称
            User user = userService.getById(order.getUserId());
            item.put("studentName", user != null ? (user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getNickname()) : "未知学员");

            // 课程名称
            Course course = courseService.getById(order.getCourseId());
            item.put("courseName", course != null ? course.getTitle() : "未知课程");

            enrichedRecords.add(item);
        }

        return Result.ok(PageResult.of(enrichedRecords, orderPage.getTotal(), orderPage.getCurrent(), orderPage.getSize()));
    }
}
