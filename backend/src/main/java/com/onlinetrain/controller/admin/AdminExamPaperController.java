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

import java.util.List;
import java.util.Map;

/**
 * 管理端-试卷管理控制器
 */
@RestController
@RequestMapping("/api/admin/exam-papers")
@Api(tags = "管理端-试卷管理")
public class AdminExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 试卷列表
     */
    @GetMapping
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
     * 创建试卷
     */
    @PostMapping
    @ApiOperation("创建试卷")
    public Result<ExamPaper> create(@RequestBody Map<String, Object> params) {
        ExamPaper paper = new ExamPaper();
        paper.setCourseId(params.get("courseId") != null ? Long.valueOf(params.get("courseId").toString()) : null);
        paper.setTitle(params.get("title") != null ? params.get("title").toString() : "");
        paper.setDurationMinutes(params.get("durationMinutes") != null ? Integer.parseInt(params.get("durationMinutes").toString()) : 60);
        paper.setTotalScore(params.get("totalScore") != null ? Integer.parseInt(params.get("totalScore").toString()) : 100);
        paper.setPassScore(params.get("passScore") != null ? Integer.parseInt(params.get("passScore").toString()) : 60);
        paper.setMaxAttempts(params.get("maxAttempts") != null ? Integer.parseInt(params.get("maxAttempts").toString()) : 1);
        paper.setStatus(params.get("status") != null ? params.get("status").toString() : "DRAFT");
        examPaperService.save(paper);

        @SuppressWarnings("unchecked")
        List<Integer> questionIds = (List<Integer>) params.get("questionIds");
        if (questionIds != null && !questionIds.isEmpty()) {
            examPaperService.savePaperQuestions(paper.getId(), questionIds);
        }

        return Result.ok(paper);
    }

    /**
     * 更新试卷
     */
    @PutMapping("/{id}")
    @ApiOperation("更新试卷")
    public Result<ExamPaper> update(@PathVariable Long id, @RequestBody ExamPaper paper) {
        paper.setId(id);
        examPaperService.updateById(paper);
        return Result.ok(paper);
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除试卷")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperService.removeById(id);
        return Result.ok();
    }

    /**
     * 修改试卷状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("修改状态")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper != null) {
            paper.setStatus(status);
            examPaperService.updateById(paper);
        }
        return Result.ok();
    }
}
