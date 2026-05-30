package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.entity.OfflineCheckin;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.LearningRecordService;
import com.onlinetrain.service.OfflineCheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * H5-线下课程打卡控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-线下课程打卡")
public class H5OfflineCheckinController {

    @Autowired
    private OfflineCheckinService offlineCheckinService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private LearningRecordService learningRecordService;

    /**
     * 学员自主打卡
     */
    @PostMapping("/checkin")
    @ApiOperation("线下课程打卡")
    public Result<Map<String, Object>> checkin(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long courseId = Long.valueOf(params.get("courseId").toString());
        BigDecimal userLng = new BigDecimal(params.get("longitude").toString());
        BigDecimal userLat = new BigDecimal(params.get("latitude").toString());

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.notFound("课程不存在");
        }
        if (!"OFFLINE".equals(course.getCourseType())) {
            return Result.error("该课程不是线下课程，无需打卡");
        }
        if (course.getLatitude() == null || course.getLongitude() == null) {
            return Result.error("该课程未设置打卡位置");
        }

        // 检查是否已打卡
        long existingCount = offlineCheckinService.lambdaQuery()
                .eq(OfflineCheckin::getUserId, userId)
                .eq(OfflineCheckin::getCourseId, courseId)
                .count();
        if (existingCount > 0) {
            return Result.error("您已经完成打卡");
        }

        // 检查前置线上课程是否学完
        if (course.getPrerequisiteCourseId() != null) {
            boolean prerequisiteFinished = checkPrerequisiteCourse(userId, course.getPrerequisiteCourseId());
            if (!prerequisiteFinished) {
                Course preCourse = courseService.getById(course.getPrerequisiteCourseId());
                String preName = preCourse != null ? preCourse.getTitle() : "前置线上课程";
                return Result.error("请先学完「" + preName + "」后才能打卡");
            }
        }

        // 计算距离
        int distance = calculateDistance(userLat.doubleValue(), userLng.doubleValue(),
                course.getLatitude().doubleValue(), course.getLongitude().doubleValue());
        int radius = course.getCheckinRadius() != null ? course.getCheckinRadius() : 3000;

        if (distance > radius) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("distance", distance);
            result.put("maxDistance", radius);
            result.put("message", "您距离打卡位置" + distance + "米，需在" + radius + "米范围内");
            return Result.ok(result);
        }

        // 保存打卡记录
        OfflineCheckin checkin = new OfflineCheckin();
        checkin.setUserId(userId);
        checkin.setCourseId(courseId);
        checkin.setCheckinLongitude(userLng);
        checkin.setCheckinLatitude(userLat);
        checkin.setDistance(distance);
        checkin.setCheckinType("SELF");
        checkin.setStatus(1);
        offlineCheckinService.save(checkin);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("distance", distance);
        result.put("message", "打卡成功！您已完成线下课程");
        return Result.ok(result);
    }

    /**
     * 获取我的打卡记录列表
     */
    @GetMapping("/checkin/list")
    @ApiOperation("我的打卡记录")
    public Result<List<Map<String, Object>>> myCheckins(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<OfflineCheckin> checkins = offlineCheckinService.lambdaQuery()
                .eq(OfflineCheckin::getUserId, userId)
                .orderByDesc(OfflineCheckin::getCreateTime)
                .list();

        List<Map<String, Object>> result = checkins.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("courseId", c.getCourseId());
            map.put("checkinTime", c.getCreateTime());
            map.put("distance", c.getDistance());
            map.put("checkinType", c.getCheckinType());

            Course course = courseService.getById(c.getCourseId());
            map.put("courseTitle", course != null ? course.getTitle() : "");
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 检查课程打卡状态
     */
    @GetMapping("/checkin/status/{courseId}")
    @ApiOperation("检查打卡状态")
    public Result<Map<String, Object>> checkinStatus(HttpServletRequest request, @PathVariable Long courseId) {
        Long userId = (Long) request.getAttribute("userId");
        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.notFound("课程不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("courseType", course.getCourseType());

        if (!"OFFLINE".equals(course.getCourseType())) {
            result.put("needCheckin", false);
            result.put("checkedIn", true);
            return Result.ok(result);
        }

        result.put("needCheckin", true);
        result.put("latitude", course.getLatitude());
        result.put("longitude", course.getLongitude());
        result.put("checkinRadius", course.getCheckinRadius() != null ? course.getCheckinRadius() : 3000);
        result.put("prerequisiteCourseId", course.getPrerequisiteCourseId());

        // 检查是否已打卡
        long count = offlineCheckinService.lambdaQuery()
                .eq(OfflineCheckin::getUserId, userId)
                .eq(OfflineCheckin::getCourseId, courseId)
                .count();
        result.put("checkedIn", count > 0);

        // 检查前置条件
        if (course.getPrerequisiteCourseId() != null) {
            boolean preFinished = checkPrerequisiteCourse(userId, course.getPrerequisiteCourseId());
            result.put("prerequisiteFinished", preFinished);
            Course preCourse = courseService.getById(course.getPrerequisiteCourseId());
            result.put("prerequisiteTitle", preCourse != null ? preCourse.getTitle() : "");
        } else {
            result.put("prerequisiteFinished", true);
        }

        return Result.ok(result);
    }

    /**
     * 检查前置线上课程是否已学完
     */
    private boolean checkPrerequisiteCourse(Long userId, Long prerequisiteCourseId) {
        // 获取前置课程的所有章节
        List<Long> chapterIds = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getCourseId, prerequisiteCourseId)
                .eq(LearningRecord::getUserId, userId)
                .list()
                .stream()
                .map(LearningRecord::getChapterId)
                .collect(Collectors.toList());

        if (chapterIds.isEmpty()) return false;

        // 获取该课程的总章节数
        long totalChapters = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getCourseId, prerequisiteCourseId)
                .eq(LearningRecord::getUserId, userId)
                .count();

        // 检查是否所有章节都已完成
        long finishedChapters = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getCourseId, prerequisiteCourseId)
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getIsFinished, 1)
                .count();

        return totalChapters > 0 && finishedChapters >= totalChapters;
    }

    /**
     * 计算两点距离（Haversine公式，返回米）
     */
    private int calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 米
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (earthRadius * c);
    }
}
