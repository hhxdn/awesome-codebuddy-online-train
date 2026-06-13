package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.WrongQuestion;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.QuestionService;
import com.onlinetrain.service.WrongQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5错题控制器 - 支持 /wrong-questions 和 /user/wrong-questions 路径
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-错题接口")
public class H5WrongQuestionController {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseService courseService;

    /**
     * 我的错题列表（支持 /wrong-questions 和 /user/wrong-questions）
     */
    @GetMapping({"/wrong-questions", "/user/wrong-questions"})
    @ApiOperation("我的错题列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Page<WrongQuestion> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WrongQuestion> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getLastWrongTime);

        Page<WrongQuestion> wqPage = wrongQuestionService.page(pageParam, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (WrongQuestion wq : wqPage.getRecords()) {
            Question question = questionService.getById(wq.getQuestionId());
            if (question == null) continue; // 跳过已删除的题目
            
            Map<String, Object> item = new HashMap<>();
            item.put("id", wq.getId());
            item.put("questionId", wq.getQuestionId());
            item.put("wrongCount", wq.getWrongCount());
            item.put("lastWrongTime", wq.getLastWrongTime());
            // 前端需要的扁平字段
            item.put("content", question.getContent());
            item.put("chapterId", question.getChapterId());
            item.put("courseId", question.getCourseId());
            item.put("type", question.getType());
            
            // 查询课程名称
            String courseName = "未知课程";
            if (question.getCourseId() != null) {
                Course course = courseService.getById(question.getCourseId());
                if (course != null) {
                    courseName = course.getTitle();
                }
            }
            item.put("courseName", courseName);
            
            records.add(item);
        }

        PageResult<Map<String, Object>> result = PageResult.of(
                records, wqPage.getTotal(), wqPage.getCurrent(), wqPage.getSize());
        return Result.ok(result);
    }

    /**
     * 移除单个错题
     */
    @DeleteMapping("/wrong-questions/{id}")
    @ApiOperation("移除错题")
    public Result<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        WrongQuestion wq = wrongQuestionService.getById(id);
        if (wq != null && wq.getUserId().equals(userId)) {
            wrongQuestionService.removeById(id);
        }
        return Result.ok();
    }

    /**
     * 清空所有错题（支持 /wrong-questions/clear 和 /user/wrong-questions）
     */
    @DeleteMapping({"/wrong-questions/clear", "/user/wrong-questions"})
    @ApiOperation("清空错题")
    public Result<Void> clearAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        wrongQuestionService.lambdaUpdate()
                .eq(WrongQuestion::getUserId, userId)
                .remove();
        return Result.ok();
    }
}
