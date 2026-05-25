package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.Order;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.CourseCategoryService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.QuestionService;
import com.onlinetrain.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5课程控制器
 */
@RestController
@RequestMapping("/api/courses")
@Api(tags = "H5-课程接口")
public class H5CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private OrderService orderService;

    /**
     * 课程列表（带筛选）
     */
    @GetMapping
    @ApiOperation("课程列表")
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Course> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, "UP");

        if (categoryId != null) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        wrapper.orderByDesc(Course::getSortOrder).orderByDesc(Course::getCreateTime);

        Page<Course> result = courseService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 课程详情
     */
    @GetMapping("/{id}")
    @ApiOperation("课程详情")
    public Result<Course> detail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.notFound("课程不存在");
        }
        return Result.ok(course);
    }

    /**
     * 章节列表
     */
    @GetMapping("/{id}/chapters")
    @ApiOperation("章节列表")
    public Result<List<Chapter>> chapters(@PathVariable Long id) {
        List<Chapter> chapters = chapterService.lambdaQuery()
                .eq(Chapter::getCourseId, id)
                .orderByAsc(Chapter::getSortOrder)
                .list();
        return Result.ok(chapters);
    }

    /**
     * 章节题目列表
     */
    @GetMapping("/{id}/chapters/{chapterId}/questions")
    @ApiOperation("章节题目")
    public Result<List<Question>> chapterQuestions(@PathVariable Long id, @PathVariable Long chapterId) {
        List<Question> questions = questionService.lambdaQuery()
                .eq(Question::getChapterId, chapterId)
                .eq(Question::getStatus, 1)
                .list();
        // 加载选项
        questionService.enrichForDisplay(questions);
        // 清除答案信息（练习模式下不暴露答案）
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });
        return Result.ok(questions);
    }

    /**
     * 检查课程访问权限
     */
    @GetMapping("/{id}/access")
    @ApiOperation("检查课程访问权限")
    public Result<Map<String, Object>> checkAccess(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.notFound("课程不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", id);

        // 免费课程直接可访问
        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            result.put("accessible", true);
            result.put("reason", "免费课程");
            return Result.ok(result);
        }

        // 检查是否已购买
        if (userId != null) {
            long paidCount = orderService.lambdaQuery()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getCourseId, id)
                    .eq(Order::getStatus, "PAID")
                    .count();
            if (paidCount > 0) {
                result.put("accessible", true);
                result.put("reason", "已购买");
                return Result.ok(result);
            }
        }

        result.put("accessible", false);
        result.put("reason", "需要购买");
        return Result.ok(result);
    }
}
