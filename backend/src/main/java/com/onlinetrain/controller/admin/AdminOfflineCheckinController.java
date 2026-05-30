package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.OfflineCheckin;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.OfflineCheckinService;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端-线下课程打卡管理
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-线下课程打卡管理")
public class AdminOfflineCheckinController {

    @Autowired
    private OfflineCheckinService offlineCheckinService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    /**
     * 打卡记录列表
     */
    @GetMapping("/checkins")
    @ApiOperation("打卡记录列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String checkinType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<OfflineCheckin> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OfflineCheckin> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (courseId != null) wrapper.eq(OfflineCheckin::getCourseId, courseId);
        if (checkinType != null && !checkinType.isEmpty()) wrapper.eq(OfflineCheckin::getCheckinType, checkinType);
        wrapper.orderByDesc(OfflineCheckin::getCreateTime);

        Page<OfflineCheckin> result = offlineCheckinService.page(pageParam, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(checkin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", checkin.getId());
            map.put("userId", checkin.getUserId());
            map.put("courseId", checkin.getCourseId());
            map.put("checkinLongitude", checkin.getCheckinLongitude());
            map.put("checkinLatitude", checkin.getCheckinLatitude());
            map.put("distance", checkin.getDistance());
            map.put("checkinType", checkin.getCheckinType());
            map.put("checkinTime", checkin.getCreateTime());

            User user = userService.getById(checkin.getUserId());
            map.put("userName", user != null ? (user.getRealName() != null ? user.getRealName() : user.getNickname()) : "");
            map.put("userPhone", user != null ? user.getPhone() : "");

            Course course = courseService.getById(checkin.getCourseId());
            map.put("courseTitle", course != null ? course.getTitle() : "");
            return map;
        }).collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());

        return Result.ok(pageResult);
    }

    /**
     * 后台替学员打卡（绕过前置课程限制和距离校验）
     */
    @PostMapping("/checkins/admin-checkin")
    @ApiOperation("后台替学员打卡")
    public Result<Map<String, Object>> adminCheckin(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.notFound("课程不存在");
        }
        if (!"OFFLINE".equals(course.getCourseType())) {
            return Result.error("该课程不是线下课程");
        }

        // 检查是否已打卡
        long existingCount = offlineCheckinService.lambdaQuery()
                .eq(OfflineCheckin::getUserId, userId)
                .eq(OfflineCheckin::getCourseId, courseId)
                .count();
        if (existingCount > 0) {
            return Result.error("该学员已完成打卡");
        }

        OfflineCheckin checkin = new OfflineCheckin();
        checkin.setUserId(userId);
        checkin.setCourseId(courseId);
        checkin.setCheckinLongitude(course.getLongitude());
        checkin.setCheckinLatitude(course.getLatitude());
        checkin.setDistance(0);
        checkin.setCheckinType("ADMIN");
        checkin.setOperatorId(1L); // 管理员ID
        checkin.setStatus(1);
        offlineCheckinService.save(checkin);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已替学员完成打卡");
        return Result.ok(result);
    }

    /**
     * 删除打卡记录
     */
    @DeleteMapping("/checkins/{id}")
    @ApiOperation("删除打卡记录")
    public Result<Void> deleteCheckin(@PathVariable Long id) {
        offlineCheckinService.removeById(id);
        return Result.ok();
    }
}
