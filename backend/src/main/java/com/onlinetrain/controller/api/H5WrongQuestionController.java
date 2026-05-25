package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.WrongQuestion;
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
 * H5错题控制器
 */
@RestController
@RequestMapping("/api/wrong-questions")
@Api(tags = "H5-错题接口")
public class H5WrongQuestionController {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private QuestionService questionService;

    /**
     * 我的错题列表
     */
    @GetMapping
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
            Map<String, Object> item = new HashMap<>();
            item.put("id", wq.getId());
            item.put("questionId", wq.getQuestionId());
            item.put("wrongCount", wq.getWrongCount());
            item.put("lastWrongTime", wq.getLastWrongTime());
            item.put("question", question);
            if (question != null) {
                question.setAnswer(null);
                question.setAnalysis(null);
            }
            records.add(item);
        }

        PageResult<Map<String, Object>> result = PageResult.of(
                records, wqPage.getTotal(), wqPage.getCurrent(), wqPage.getSize());

        return Result.ok(result);
    }

    /**
     * 移除单个错题
     */
    @DeleteMapping("/{id}")
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
     * 清空所有错题
     */
    @DeleteMapping("/clear")
    @ApiOperation("清空错题")
    public Result<Void> clearAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        wrongQuestionService.lambdaUpdate()
                .eq(WrongQuestion::getUserId, userId)
                .remove();
        return Result.ok();
    }
}
