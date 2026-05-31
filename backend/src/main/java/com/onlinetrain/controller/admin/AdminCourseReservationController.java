package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseReservation;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.CourseReservationService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端-线下课程预约管理
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-线下课程预约管理")
public class AdminCourseReservationController {

    @Autowired
    private CourseReservationService courseReservationService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    /**
     * 预约列表
     */
    @GetMapping("/course-reservations")
    @ApiOperation("课程预约列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<CourseReservation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseReservation> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) wrapper.eq(CourseReservation::getCourseId, courseId);
        if (status != null && !status.isEmpty()) wrapper.eq(CourseReservation::getStatus, status);
        wrapper.orderByDesc(CourseReservation::getCreateTime);

        Page<CourseReservation> result = courseReservationService.page(pageParam, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("courseId", r.getCourseId());
            map.put("userId", r.getUserId());
            map.put("reservationTime", r.getReservationTime());
            map.put("status", r.getStatus());
            map.put("remark", r.getRemark());
            map.put("createTime", r.getCreateTime());

            Course course = courseService.getById(r.getCourseId());
            map.put("courseTitle", course != null ? course.getTitle() : "");

            User user = userService.getById(r.getUserId());
            map.put("userName", user != null ? (user.getRealName() != null ? user.getRealName() : user.getNickname()) : "");
            map.put("userPhone", user != null ? user.getPhone() : "");
            return map;
        }).collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
        return Result.ok(pageResult);
    }

    /**
     * 确认预约
     */
    @PutMapping("/course-reservations/{id}/confirm")
    @ApiOperation("确认预约")
    public Result<Void> confirm(@PathVariable Long id) {
        CourseReservation reservation = courseReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("当前状态不允许确认");
        }
        reservation.setStatus("CONFIRMED");
        courseReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 取消预约（管理员操作）
     */
    @PutMapping("/course-reservations/{id}/cancel")
    @ApiOperation("取消预约")
    public Result<Void> cancel(@PathVariable Long id) {
        CourseReservation reservation = courseReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        reservation.setStatus("CANCELLED");
        courseReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 标记为已完成
     */
    @PutMapping("/course-reservations/{id}/complete")
    @ApiOperation("标记已完成")
    public Result<Void> complete(@PathVariable Long id) {
        CourseReservation reservation = courseReservationService.getById(id);
        if (reservation == null) return Result.notFound("预约不存在");
        reservation.setStatus("COMPLETED");
        courseReservationService.updateById(reservation);
        return Result.ok();
    }
}
