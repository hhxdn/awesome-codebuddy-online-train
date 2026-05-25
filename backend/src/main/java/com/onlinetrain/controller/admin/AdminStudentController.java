package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.*;
import com.onlinetrain.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-学员管理")
public class AdminStudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private LearningRecordService learningRecordService;

    @GetMapping("/students")
    @ApiOperation("学员列表")
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/students/{id}")
    @ApiOperation("学员详情")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.ok(user);
    }

    @GetMapping("/students/{id}/courses")
    @ApiOperation("学员课程")
    public Result<List<Map<String, Object>>> studentCourses(@PathVariable Long id) {
        List<Order> paidOrders = orderService.lambdaQuery()
                .eq(Order::getUserId, id)
                .eq(Order::getStatus, "PAID")
                .list();

        Set<Long> courseIds = paidOrders.stream().map(Order::getCourseId).collect(Collectors.toSet());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long cid : courseIds) {
            Course course = courseService.getById(cid);
            if (course == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", course.getId());
            item.put("title", course.getTitle());
            item.put("price", course.getPrice());

            long finishedCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getUserId, id)
                    .eq(LearningRecord::getCourseId, cid)
                    .eq(LearningRecord::getIsFinished, 1)
                    .count();
            item.put("finishedChapters", finishedCount);
            result.add(item);
        }
        return Result.ok(result);
    }

    @GetMapping("/students/{id}/exams")
    @ApiOperation("学员考试记录")
    public Result<List<Map<String, Object>>> studentExams(@PathVariable Long id) {
        List<ExamRecord> records = examRecordService.lambdaQuery()
                .eq(ExamRecord::getUserId, id)
                .orderByDesc(ExamRecord::getCreateTime)
                .list();

        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("examPaperId", r.getExamPaperId());
            item.put("score", r.getScore());
            item.put("isPass", r.getIsPass());
            item.put("status", r.getStatus());
            item.put("createTime", r.getCreateTime());
            return item;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/students/{id}/orders")
    @ApiOperation("学员订单")
    public Result<List<Order>> studentOrders(@PathVariable Long id) {
        List<Order> orders = orderService.lambdaQuery()
                .eq(Order::getUserId, id)
                .orderByDesc(Order::getCreateTime)
                .list();
        return Result.ok(orders);
    }
}
