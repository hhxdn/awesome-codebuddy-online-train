package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.service.ExamPaperService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * 管理端-试卷管理控制器（路径匹配前端 /admin/exams）
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-试卷管理")
public class AdminExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 试卷列表
     */
    @GetMapping("/exams")
    @ApiOperation("试卷列表")
    public Result<PageResult<ExamPaper>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ExamPaper> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamPaper> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamPaper::getCreateTime);
        return Result.ok(PageResult.of(examPaperService.page(pageParam, wrapper)));
    }

    /**
     * 试卷详情
     */
    @GetMapping("/exams/{id}")
    @ApiOperation("试卷详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper == null) return Result.notFound("试卷不存在");
        
        // 获取关联的题目ID列表
        List<Long> questionIds = examPaperService.getPaperQuestionIds(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", paper.getId());
        result.put("title", paper.getTitle());
        result.put("courseId", paper.getCourseId());
        result.put("durationMinutes", paper.getDurationMinutes());
        result.put("totalScore", paper.getTotalScore());
        result.put("passScore", paper.getPassScore());
        result.put("maxAttempts", paper.getMaxAttempts());
        result.put("status", paper.getStatus());
        result.put("questionIds", questionIds != null ? questionIds : Collections.emptyList());
        return Result.ok(result);
    }

    /**
     * 创建试卷
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/exams")
    @ApiOperation("创建试卷")
    public Result<ExamPaper> create(@RequestBody Map<String, Object> params) {
        ExamPaper paper = new ExamPaper();
        if (params.get("courseId") != null) paper.setCourseId(Long.valueOf(params.get("courseId").toString()));
        if (params.get("title") != null) paper.setTitle(params.get("title").toString());
        if (params.get("durationMinutes") != null) paper.setDurationMinutes(Integer.parseInt(params.get("durationMinutes").toString()));
        if (params.get("totalScore") != null) paper.setTotalScore(Integer.parseInt(params.get("totalScore").toString()));
        if (params.get("passScore") != null) paper.setPassScore(Integer.parseInt(params.get("passScore").toString()));
        if (params.get("maxAttempts") != null) paper.setMaxAttempts(Integer.parseInt(params.get("maxAttempts").toString()));
        paper.setStatus(params.get("status") != null ? params.get("status").toString() : "DRAFT");
        examPaperService.save(paper);

        List<Long> questionIds = (List<Long>) params.get("questionIds");
        if (questionIds != null && !questionIds.isEmpty()) {
            examPaperService.savePaperQuestions(paper.getId(), questionIds);
        }
        return Result.ok(paper);
    }

    /**
     * 更新试卷
     */
    @PutMapping("/exams/{id}")
    @ApiOperation("更新试卷")
    public Result<ExamPaper> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper == null) return Result.notFound("试卷不存在");

        if (params.get("title") != null) paper.setTitle(params.get("title").toString());
        if (params.get("courseId") != null) paper.setCourseId(Long.valueOf(params.get("courseId").toString()));
        if (params.get("durationMinutes") != null) paper.setDurationMinutes(Integer.parseInt(params.get("durationMinutes").toString()));
        if (params.get("totalScore") != null) paper.setTotalScore(Integer.parseInt(params.get("totalScore").toString()));
        if (params.get("passScore") != null) paper.setPassScore(Integer.parseInt(params.get("passScore").toString()));
        if (params.get("maxAttempts") != null) paper.setMaxAttempts(Integer.parseInt(params.get("maxAttempts").toString()));
        examPaperService.updateById(paper);

        @SuppressWarnings("unchecked")
        List<Long> questionIds = (List<Long>) params.get("questionIds");
        if (questionIds != null) {
            examPaperService.savePaperQuestions(id, questionIds);
        }
        return Result.ok(paper);
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/exams/{id}")
    @ApiOperation("删除试卷")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperService.removeById(id);
        return Result.ok();
    }

    /**
     * 修改试卷状态 - body: {status: "PUBLISHED"/"DRAFT"/...}
     */
    @PutMapping("/exams/{id}/status")
    @ApiOperation("修改试卷状态")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper != null && params.get("status") != null) {
            paper.setStatus(params.get("status").toString());
            examPaperService.updateById(paper);
        }
        return Result.ok();
    }

    /**
     * 发布试卷
     */
    @PutMapping("/exams/{id}/publish")
    @ApiOperation("发布试卷")
    public Result<Void> publish(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper != null) {
            paper.setStatus("PUBLISHED");
            examPaperService.updateById(paper);
        }
        return Result.ok();
    }

    /**
     * 结束试卷
     */
    @PutMapping("/exams/{id}/end")
    @ApiOperation("结束试卷")
    public Result<Void> end(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper != null) {
            paper.setStatus("ENDED");
            examPaperService.updateById(paper);
        }
        return Result.ok();
    }
}
