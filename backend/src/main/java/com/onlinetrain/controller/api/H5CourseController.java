package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.Order;
import com.onlinetrain.entity.StudentExerciseAccess;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.CourseCategoryService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.QuestionService;
import com.onlinetrain.service.OrderService;
import com.onlinetrain.service.StudentExerciseAccessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private CourseCategoryService categoryService;

    @Autowired
    private StudentExerciseAccessService exerciseAccessService;

    /**
     * 课程列表（带筛选）
     */
    @GetMapping
    @ApiOperation("课程列表")
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String courseType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Course> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, "UP");

        if (categoryId != null) {
            // 查询该分类及其所有子孙分类的ID，确保上级分类"全部"也能查到子分类下的课程
            Set<Long> categoryIds = collectDescendantIds(categoryId);
            if (!categoryIds.isEmpty()) {
                wrapper.in(Course::getCategoryId, categoryIds);
            } else {
                wrapper.eq(Course::getCategoryId, categoryId);
            }
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        if (courseType != null && !courseType.isEmpty()) {
            wrapper.eq(Course::getCourseType, courseType);
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
     * 章节列表（含各章节题目数量）
     */
    @GetMapping("/{id}/chapters")
    @ApiOperation("章节列表")
    public Result<List<Map<String, Object>>> chapters(@PathVariable Long id) {
        List<Chapter> chapters = chapterService.lambdaQuery()
                .eq(Chapter::getCourseId, id)
                .orderByAsc(Chapter::getSortOrder)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Chapter ch : chapters) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ch.getId());
            item.put("courseId", ch.getCourseId());
            item.put("title", ch.getTitle());
            item.put("videoUrl", ch.getVideoUrl());
            item.put("duration", ch.getVideoDuration() != null && ch.getVideoDuration() > 0 ? formatDuration(ch.getVideoDuration()) : "视频");
            item.put("sortOrder", ch.getSortOrder());
            item.put("createTime", ch.getCreateTime());
            // 统计该章节的题目数
            long questionCount = questionService.lambdaQuery()
                    .eq(Question::getChapterId, ch.getId())
                    .eq(Question::getStatus, 1)
                    .count();
            item.put("questionCount", questionCount);
            result.add(item);
        }
        return Result.ok(result);
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
     * 检查课程访问权限（支持分类购买模式）
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

        if (userId != null) {
            // 方式1：直接购买了该课程
            long paidCount = orderService.lambdaQuery()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getCourseId, id)
                    .eq(Order::getStatus, "PAID")
                    .count();
            if (paidCount > 0) {
                result.put("accessible", true);
                result.put("reason", "已购买课程");
                return Result.ok(result);
            }

            // 方式2：购买了课程所属的末级分类（或其祖先分类）
            if (course.getCategoryId() != null) {
                // 收集该分类及其所有祖先分类ID
                Set<Long> categoryIds = new HashSet<>();
                Long catId = course.getCategoryId();
                CourseCategory cat = categoryService.getById(catId);
                while (cat != null) {
                    categoryIds.add(cat.getId());
                    cat = cat.getParentId() != null ? categoryService.getById(cat.getParentId()) : null;
                }

                long categoryPaidCount = orderService.lambdaQuery()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getProductType, "CATEGORY")
                        .in(Order::getProductId, categoryIds)
                        .eq(Order::getStatus, "PAID")
                        .count();
                if (categoryPaidCount > 0) {
                    result.put("accessible", true);
                    result.put("reason", "已购买分类");
                    return Result.ok(result);
                }
            }
        }

        result.put("accessible", false);
        result.put("reason", "需要购买课程或对应分类");
        return Result.ok(result);
    }

    /**
     * 检查学员是否有课程习题练习权限
     */
    @GetMapping("/{id}/exercise-access")
    @ApiOperation("检查习题练习权限")
    public Result<Map<String, Object>> checkExerciseAccess(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("courseId", id);

        StudentExerciseAccess access = exerciseAccessService.lambdaQuery()
                .eq(StudentExerciseAccess::getUserId, userId)
                .eq(StudentExerciseAccess::getCourseId, id)
                .one();
        result.put("hasExerciseAccess", access != null);
        return Result.ok(result);
    }

    /**
     * 获取所有有练习题的课程（含章节和题目统计），不做权限过滤
     */
    @GetMapping("/with-exercises")
    @ApiOperation("获取所有有练习题的课程列表")
    public Result<List<Map<String, Object>>> coursesWithExercises() {
        // 查询所有有题目（status=1）的课程ID，去重
        List<Object> courseIdObjs = questionService.listObjs(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question>()
                        .select(Question::getCourseId)
                        .eq(Question::getStatus, 1)
                        .groupBy(Question::getCourseId)
        );

        if (courseIdObjs == null || courseIdObjs.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> courseIds = courseIdObjs.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        // 查询上架状态的课程
        List<Course> courses = courseService.lambdaQuery()
                .in(Course::getId, courseIds)
                .eq(Course::getStatus, "UP")
                .orderByDesc(Course::getSortOrder)
                .list();

        if (courses.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // 批量预取章节数和题目数，避免N+1查询
        Map<Long, Long> chapterCountMap = new HashMap<>();
        Map<Long, Long> questionCountMap = new HashMap<>();
        for (Long cid : courseIds) {
            long cc = chapterService.lambdaQuery().eq(Chapter::getCourseId, cid).count();
            long qc = questionService.lambdaQuery()
                    .eq(Question::getCourseId, cid)
                    .eq(Question::getStatus, 1)
                    .count();
            chapterCountMap.put(cid, cc);
            questionCountMap.put(cid, qc);
        }

        List<Map<String, Object>> result = courses.stream().map(course -> {
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", course.getId());
            item.put("courseTitle", course.getTitle());
            item.put("courseCover", course.getCover());
            item.put("price", course.getPrice());
            item.put("chapterCount", chapterCountMap.getOrDefault(course.getId(), 0L));
            item.put("questionCount", questionCountMap.getOrDefault(course.getId(), 0L));
            return item;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 收集指定分类及其所有子孙分类的ID集合
     */
    private Set<Long> collectDescendantIds(Long categoryId) {
        Set<Long> ids = new HashSet<>();
        ids.add(categoryId);

        List<CourseCategory> allCategories = categoryService.list();
        // 构建 parentId -> children 映射
        Map<Long, List<CourseCategory>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CourseCategory::getParentId));

        // 递归收集
        collectChildren(categoryId, childrenMap, ids);
        return ids;
    }

    private void collectChildren(Long parentId, Map<Long, List<CourseCategory>> childrenMap, Set<Long> ids) {
        List<CourseCategory> children = childrenMap.get(parentId);
        if (children == null) return;
        for (CourseCategory child : children) {
            ids.add(child.getId());
            collectChildren(child.getId(), childrenMap, ids);
        }
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0) return "视频";
        if (seconds >= 3600) {
            return (seconds / 3600) + "小时" + ((seconds % 3600) / 60) + "分钟";
        } else if (seconds >= 60) {
            return (seconds / 60) + "分钟" + (seconds % 60) + "秒";
        }
        return seconds + "秒";
    }
}
