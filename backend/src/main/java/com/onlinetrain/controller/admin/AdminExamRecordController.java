package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamRecord;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.entity.ExamAnswer;
import com.onlinetrain.entity.Question;
import com.onlinetrain.service.ExamPaperService;
import com.onlinetrain.service.ExamRecordService;
import com.onlinetrain.service.ExamAnswerService;
import com.onlinetrain.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理端-考试记录控制器（路径匹配前端 /admin/exams/records）
 */
@RestController
@RequestMapping("/api/admin/exams")
@Api(tags = "管理端-考试记录")
public class AdminExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/records")
    @ApiOperation("考试记录列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ExamRecord> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (examId != null) wrapper.eq(ExamRecord::getExamPaperId, examId);
        if (userId != null) wrapper.eq(ExamRecord::getUserId, userId);
        wrapper.orderByDesc(ExamRecord::getCreateTime);

        Page<ExamRecord> result = examRecordService.page(pageParam, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (ExamRecord r : result.getRecords()) {
            ExamPaper paper = examPaperService.getById(r.getExamPaperId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("examPaperId", r.getExamPaperId());
            item.put("paperName", paper != null ? paper.getTitle() : "");
            item.put("userId", r.getUserId());
            item.put("score", r.getScore());
            item.put("isPass", r.getIsPass());
            item.put("status", r.getStatus());
            item.put("startTime", r.getStartTime());
            item.put("submitTime", r.getSubmitTime());
            item.put("createTime", r.getCreateTime());
            records.add(item);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/records/{id}")
    @ApiOperation("考试记录详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ExamRecord record = examRecordService.getById(id);
        if (record == null) return Result.notFound("记录不存在");

        ExamPaper paper = examPaperService.getById(record.getExamPaperId());
        List<ExamAnswer> answers = examAnswerService.lambdaQuery()
                .eq(ExamAnswer::getExamRecordId, record.getId())
                .list();

        List<Map<String, Object>> answerDetails = new ArrayList<>();
        for (ExamAnswer a : answers) {
            Question q = questionService.getById(a.getQuestionId());
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", a.getId());
            detail.put("question", q);
            detail.put("userAnswer", a.getUserAnswer());
            detail.put("isCorrect", a.getIsCorrect());
            detail.put("score", a.getScore());
            answerDetails.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("paper", paper);
        result.put("answers", answerDetails);
        return Result.ok(result);
    }
}
