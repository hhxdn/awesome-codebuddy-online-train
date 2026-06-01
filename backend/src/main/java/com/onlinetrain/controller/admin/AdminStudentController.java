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

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private StudentExerciseAccessService exerciseAccessService;

    @GetMapping("/students")
    @ApiOperation("学员列表")
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            wrapper.eq(User::getApprovalStatus, approvalStatus);
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

            long totalChapters = chapterService.lambdaQuery()
                    .eq(Chapter::getCourseId, cid).count();
            long finishedCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getUserId, id)
                    .eq(LearningRecord::getCourseId, cid)
                    .eq(LearningRecord::getIsFinished, 1)
                    .count();
            item.put("finishedChapters", finishedCount);
            item.put("totalChapters", totalChapters);
            item.put("progress", totalChapters > 0 ? (int)((double)finishedCount / totalChapters * 100) : 0);
            item.put("finished", finishedCount >= totalChapters && totalChapters > 0);
            result.add(item);
        }
        return Result.ok(result);
    }

    @GetMapping("/students/{id}/learning")
    @ApiOperation("学员学习记录(前端兼容)")
    public Result<List<Map<String, Object>>> studentLearning(@PathVariable Long id) {
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
            item.put("courseName", course.getTitle());
            item.put("courseId", cid);

            long totalChapters = chapterService.lambdaQuery()
                    .eq(Chapter::getCourseId, cid).count();
            long finishedCount = learningRecordService.lambdaQuery()
                    .eq(LearningRecord::getUserId, id)
                    .eq(LearningRecord::getCourseId, cid)
                    .eq(LearningRecord::getIsFinished, 1)
                    .count();
            item.put("progress", totalChapters > 0 ? (int)((double)finishedCount / totalChapters * 100) : 0);
            item.put("duration", finishedCount * 600L); // 估算学习时长，每章约10分钟
            item.put("finished", finishedCount >= totalChapters && totalChapters > 0);
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

    @PutMapping("/students/{id}/approve")
    @ApiOperation("审核通过学员")
    public Result<String> approve(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("学员不存在");
        }
        user.setApprovalStatus("APPROVED");
        userService.updateById(user);
        return Result.ok("审核通过");
    }

    @PutMapping("/students/{id}/reject")
    @ApiOperation("拒绝学员")
    public Result<String> reject(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("学员不存在");
        }
        user.setApprovalStatus("REJECTED");
        userService.updateById(user);
        return Result.ok("已拒绝");
    }

    /**
     * 获取学员习题访问权限列表
     */
    @GetMapping("/students/{id}/exercise-access")
    @ApiOperation("学员习题访问权限列表")
    public Result<List<Map<String, Object>>> exerciseAccessList(@PathVariable Long id) {
        // 获取学员已购买课程
        List<Order> paidOrders = orderService.lambdaQuery()
                .eq(Order::getUserId, id)
                .eq(Order::getStatus, "PAID")
                .list();
        Set<Long> courseIds = paidOrders.stream().map(Order::getCourseId).collect(Collectors.toSet());

        // 获取已开通习题权限的课程
        List<StudentExerciseAccess> accessList = exerciseAccessService.lambdaQuery()
                .eq(StudentExerciseAccess::getUserId, id)
                .list();
        Set<Long> accessCourseIds = accessList.stream()
                .map(StudentExerciseAccess::getCourseId).collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long cid : courseIds) {
            Course course = courseService.getById(cid);
            if (course == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", cid);
            item.put("courseTitle", course.getTitle());
            item.put("hasAccess", accessCourseIds.contains(cid));
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 开通学员某课程习题权限
     */
    @PostMapping("/students/{id}/exercise-access/{courseId}")
    @ApiOperation("开通学员习题权限")
    public Result<String> grantExerciseAccess(@PathVariable Long id, @PathVariable Long courseId) {
        StudentExerciseAccess existing = exerciseAccessService.lambdaQuery()
                .eq(StudentExerciseAccess::getUserId, id)
                .eq(StudentExerciseAccess::getCourseId, courseId)
                .one();
        if (existing == null) {
            StudentExerciseAccess access = new StudentExerciseAccess();
            access.setUserId(id);
            access.setCourseId(courseId);
            exerciseAccessService.save(access);
        }
        return Result.ok("已开通");
    }

    /**
     * 撤销学员某课程习题权限
     */
    @DeleteMapping("/students/{id}/exercise-access/{courseId}")
    @ApiOperation("撤销学员习题权限")
    public Result<String> revokeExerciseAccess(@PathVariable Long id, @PathVariable Long courseId) {
        exerciseAccessService.lambdaUpdate()
                .eq(StudentExerciseAccess::getUserId, id)
                .eq(StudentExerciseAccess::getCourseId, courseId)
                .remove();
        return Result.ok("已撤销");
    }

    /**
     * 单独获取待审核学员列表，包含资料信息
     */
    @GetMapping("/students/pending")
    @ApiOperation("待审核学员列表")
    public Result<PageResult<User>> pendingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT")
               .eq(User::getApprovalStatus, "PENDING")
               .orderByDesc(User::getRegisterTime);
        Page<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(result));
    }
}
