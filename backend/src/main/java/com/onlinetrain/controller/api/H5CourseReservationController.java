package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseReservation;
import com.onlinetrain.service.CourseReservationService;
import com.onlinetrain.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * H5-线下课程预约控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-线下课程预约")
public class H5CourseReservationController {

    @Autowired
    private CourseReservationService courseReservationService;

    @Autowired
    private CourseService courseService;

    /**
     * 获取可预约的线下课程列表
     */
    @GetMapping("/course/reservations/available")
    @ApiOperation("可预约的线下课程列表")
    public Result<List<Map<String, Object>>> available(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 获取所有已上架的线下课程
        List<Course> offlineCourses = courseService.lambdaQuery()
                .eq(Course::getCourseType, "OFFLINE")
                .eq(Course::getStatus, "UP")
                .list();

        // 获取该用户已有预约
        List<CourseReservation> myReservations = courseReservationService.lambdaQuery()
                .eq(CourseReservation::getUserId, userId)
                .list();
        Set<Long> reservedCourseIds = myReservations.stream()
                .map(CourseReservation::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, CourseReservation> reservationMap = myReservations.stream()
                .collect(Collectors.toMap(CourseReservation::getCourseId, r -> r, (a, b) -> a));

        List<Map<String, Object>> result = offlineCourses.stream().map(course -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", course.getId());
            map.put("title", course.getTitle());
            map.put("cover", course.getCover());
            map.put("description", course.getDescription());
            map.put("studentCount", course.getStudentCount());
            map.put("hasReserved", reservedCourseIds.contains(course.getId()));

            CourseReservation myRes = reservationMap.get(course.getId());
            if (myRes != null) {
                map.put("reservationId", myRes.getId());
                map.put("reservationTime", myRes.getReservationTime());
                map.put("reservationStatus", myRes.getStatus());
            }
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 预约线下课程
     * body: { courseId, reservationTime (可选), remark (可选) }
     */
    @PostMapping("/course/reservations")
    @ApiOperation("预约线下课程")
    public Result<Map<String, Object>> reserve(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long courseId = Long.valueOf(params.get("courseId").toString());

        Course course = courseService.getById(courseId);
        if (course == null) return Result.notFound("课程不存在");
        if (!"OFFLINE".equals(course.getCourseType())) {
            return Result.error("该课程为线上课程，无需预约");
        }
        if (!"UP".equals(course.getStatus())) {
            return Result.error("该课程暂未开放预约");
        }

        // 检查是否已预约
        CourseReservation existing = courseReservationService.lambdaQuery()
                .eq(CourseReservation::getUserId, userId)
                .eq(CourseReservation::getCourseId, courseId)
                .one();
        if (existing != null) {
            if ("CANCELLED".equals(existing.getStatus())) {
                existing.setStatus("PENDING");
                existing.setRemark(null);
                if (params.get("reservationTime") != null) {
                    existing.setReservationTime(java.time.LocalDateTime.parse(params.get("reservationTime").toString()));
                }
                if (params.get("remark") != null) {
                    existing.setRemark(params.get("remark").toString());
                }
                courseReservationService.updateById(existing);
                Map<String, Object> result = new HashMap<>();
                result.put("id", existing.getId());
                result.put("status", "PENDING");
                result.put("message", "重新预约成功，等待管理员确认");
                return Result.ok(result);
            }
            return Result.error("您已预约该课程，状态：" + existing.getStatus());
        }

        CourseReservation reservation = new CourseReservation();
        reservation.setUserId(userId);
        reservation.setCourseId(courseId);
        reservation.setStatus("PENDING");
        if (params.get("reservationTime") != null) {
            reservation.setReservationTime(java.time.LocalDateTime.parse(params.get("reservationTime").toString()));
        }
        if (params.get("remark") != null) {
            reservation.setRemark(params.get("remark").toString());
        }
        courseReservationService.save(reservation);

        Map<String, Object> result = new HashMap<>();
        result.put("id", reservation.getId());
        result.put("status", "PENDING");
        result.put("message", "预约成功，等待管理员确认");
        return Result.ok(result);
    }

    /**
     * 取消预约
     */
    @PutMapping("/course/reservations/{id}/cancel")
    @ApiOperation("取消预约")
    public Result<Void> cancelReservation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        CourseReservation reservation = courseReservationService.getById(id);
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            return Result.notFound("预约不存在");
        }
        if (!"PENDING".equals(reservation.getStatus())) {
            return Result.error("当前状态不允许取消");
        }
        reservation.setStatus("CANCELLED");
        courseReservationService.updateById(reservation);
        return Result.ok();
    }

    /**
     * 我的课程预约列表
     */
    @GetMapping("/course/reservations/my")
    @ApiOperation("我的课程预约列表")
    public Result<List<Map<String, Object>>> myReservations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<CourseReservation> reservations = courseReservationService.lambdaQuery()
                .eq(CourseReservation::getUserId, userId)
                .orderByDesc(CourseReservation::getCreateTime)
                .list();

        List<Map<String, Object>> result = reservations.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("courseId", r.getCourseId());
            map.put("status", r.getStatus());
            map.put("reservationTime", r.getReservationTime());
            map.put("remark", r.getRemark());
            map.put("createTime", r.getCreateTime());

            Course course = courseService.getById(r.getCourseId());
            map.put("courseTitle", course != null ? course.getTitle() : "");
            map.put("courseCover", course != null ? course.getCover() : "");
            map.put("courseDescription", course != null ? course.getDescription() : "");
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 查询单个课程的预约状态
     */
    @GetMapping("/course/reservations/status/{courseId}")
    @ApiOperation("查询课程预约状态")
    public Result<Map<String, Object>> status(HttpServletRequest request, @PathVariable Long courseId) {
        Long userId = (Long) request.getAttribute("userId");
        CourseReservation reservation = courseReservationService.lambdaQuery()
                .eq(CourseReservation::getUserId, userId)
                .eq(CourseReservation::getCourseId, courseId)
                .one();

        Map<String, Object> map = new HashMap<>();
        if (reservation != null) {
            map.put("hasReservation", true);
            map.put("reservationId", reservation.getId());
            map.put("status", reservation.getStatus());
            map.put("reservationTime", reservation.getReservationTime());
            map.put("remark", reservation.getRemark());
            map.put("createTime", reservation.getCreateTime());
        } else {
            map.put("hasReservation", false);
        }
        return Result.ok(map);
    }
}
