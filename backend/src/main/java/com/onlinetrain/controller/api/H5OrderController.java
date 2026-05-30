package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.BusinessException;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.entity.Order;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.CourseCategoryService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.OrderService;
import com.onlinetrain.service.UserService;
import com.onlinetrain.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * H5订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Api(tags = "H5-订单接口")
public class H5OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseCategoryService categoryService;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private UserService userService;

    /**
     * 创建订单（支持购买课程或分类）
     */
    @PostMapping
    @ApiOperation("创建订单")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String productType = params.get("productType") != null ? params.get("productType").toString() : "COURSE";

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        order.setUserId(userId);
        order.setPayMethod(params.get("payMethod") != null ? params.get("payMethod").toString() : "WECHAT");
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));

        if ("CATEGORY".equals(productType)) {
            // 购买分类
            Long categoryId = Long.valueOf(params.get("categoryId").toString());
            CourseCategory category = categoryService.getById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
            order.setProductType("CATEGORY");
            order.setProductId(categoryId);
            order.setCourseId(categoryId); // 兼容老字段
            order.setAmount(category.getPrice() != null ? category.getPrice() : BigDecimal.ZERO);

            // 检查是否已有未支付订单
            Order existOrder = orderService.lambdaQuery()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getProductType, "CATEGORY")
                    .eq(Order::getProductId, categoryId)
                    .eq(Order::getStatus, "PENDING")
                    .one();
            if (existOrder != null) {
                return Result.ok(existOrder);
            }
        } else {
            // 购买课程
            Long courseId = Long.valueOf(params.get("courseId").toString());
            Course course = courseService.getById(courseId);
            if (course == null) {
                throw new BusinessException("课程不存在");
            }
            order.setProductType("COURSE");
            order.setProductId(courseId);
            order.setCourseId(courseId);
            order.setAmount(course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO);

            // 检查是否已有未支付订单
            Order existOrder = orderService.lambdaQuery()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getProductType, "COURSE")
                    .eq(Order::getProductId, courseId)
                    .eq(Order::getStatus, "PENDING")
                    .one();
            if (existOrder != null) {
                return Result.ok(existOrder);
            }
        }

        orderService.save(order);
        return Result.ok(order);
    }

    /**
     * 我的订单列表
     */
    @GetMapping
    @ApiOperation("我的订单列表")
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Page<Order> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);

        Page<Order> result = orderService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许取消");
        }

        order.setStatus("CANCELLED");
        orderService.updateById(order);

        return Result.ok();
    }

    /**
     * 发起支付（自测模式：发起订单即成功）
     */
    @PostMapping("/{id}/pay")
    @ApiOperation("发起支付")
    public Result<Map<String, Object>> pay(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }

        // 自测模式：直接标记为已支付
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderService.updateById(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getAmount());
        result.put("payMethod", order.getPayMethod());
        result.put("status", "PAID");

        return Result.ok(result);
    }
}
