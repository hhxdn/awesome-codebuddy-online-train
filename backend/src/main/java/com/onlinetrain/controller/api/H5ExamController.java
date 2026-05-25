package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.BusinessException;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.*;
import com.onlinetrain.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * H5考试控制器
 */
@RestController
@RequestMapping("/api/h5/exam")
@Api(tags = "H5-考试接口")
public class H5ExamController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    /**
     * 课程可用的试卷列表
     */
    @GetMapping("/papers/{courseId}")
    @ApiOperation("试卷列表")
    public Result<List<ExamPaper>> papers(@PathVariable Long courseId) {
        List<ExamPaper> papers = examPaperService.lambdaQuery()
                .eq(ExamPaper::getCourseId, courseId)
                .eq(ExamPaper::getStatus, "PUBLISHED")
                .list();
        return Result.ok(papers);
    }

    /**
     * 开始考试
     */
    @PostMapping("/start/{paperId}")
    @ApiOperation("开始考试")
    public Result<Map<String, Object>> startExam(@PathVariable Long paperId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        ExamPaper paper = examPaperService.getById(paperId);
        if (paper == null || !"PUBLISHED".equals(paper.getStatus())) {
            throw new BusinessException("试卷不可用");
        }

        // 检查是否超过最大考试次数
        long attemptCount = examRecordService.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamPaperId, paperId)
                .count();
        if (attemptCount >= paper.getMaxAttempts()) {
            throw new BusinessException("已达到最大考试次数");
        }

        // 检查是否有进行中的考试
        ExamRecord doingRecord = examRecordService.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamPaperId, paperId)
                .eq(ExamRecord::getStatus, "DOING")
                .one();
        if (doingRecord != null) {
            // 返回进行中的考试
            List<ExamAnswer> answers = examAnswerService.lambdaQuery()
                    .eq(ExamAnswer::getExamRecordId, doingRecord.getId())
                    .list();

            List<Question> questions = new ArrayList<>();
            for (ExamAnswer answer : answers) {
                Question q = questionService.getById(answer.getQuestionId());
                if (q != null) {
                    q.setAnswer(null);
                    q.setAnalysis(null);
                    questions.add(q);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("recordId", doingRecord.getId());
            result.put("paper", paper);
            result.put("questions", questions);
            result.put("answers", answers);
            return Result.ok(result);
        }

        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamPaperId(paperId);
        record.setScore(BigDecimal.ZERO);
        record.setIsPass(0);
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(LocalDateTime.now().plusMinutes(paper.getDurationMinutes()));
        record.setStatus("DOING");
        record.setCheatCount(0);
        examRecordService.save(record);

        // 获取试卷题目
        List<Question> questions = questionService.getExamPaperQuestions(paperId);
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });

        // 创建答题记录
        List<ExamAnswer> answers = new ArrayList<>();
        for (Question q : questions) {
            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(record.getId());
            answer.setQuestionId(q.getId());
            answer.setScore(0);
            answer.setIsCorrect(0);
            answers.add(answer);
        }
        examAnswerService.saveBatch(answers);

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("paper", paper);
        result.put("questions", questions);
        result.put("answers", answers);
        return Result.ok(result);
    }

    /**
     * 提交考试
     */
    @PostMapping("/submit/{recordId}")
    @ApiOperation("提交考试")
    public Result<Map<String, Object>> submitExam(
            @PathVariable Long recordId,
            @RequestBody List<Map<String, Object>> userAnswers,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        ExamRecord record = examRecordService.getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("考试记录不存在");
        }
        if ("SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException("考试已提交");
        }

        ExamPaper paper = examPaperService.getById(record.getExamPaperId());

        List<ExamAnswer> answers = examAnswerService.lambdaQuery()
                .eq(ExamAnswer::getExamRecordId, record.getId())
                .list();

        int totalScore = 0;
        int rightCount = 0;

        for (ExamAnswer answer : answers) {
            Question question = questionService.getById(answer.getQuestionId());
            if (question == null) continue;

            String userAnswer = null;
            for (Map<String, Object> ua : userAnswers) {
                Object qidObj = ua.get("questionId");
                if (qidObj != null && Long.valueOf(qidObj.toString()).equals(question.getId())) {
                    Object ansObj = ua.get("answer");
                    userAnswer = ansObj != null ? ansObj.toString() : "";
                    break;
                }
            }

            answer.setUserAnswer(userAnswer);
            if (userAnswer != null && userAnswer.equals(question.getAnswer())) {
                answer.setIsCorrect(1);
                answer.setScore(question.getScore() != null ? question.getScore() : 1);
                rightCount++;
                totalScore += (question.getScore() != null ? question.getScore() : 1);
            } else {
                answer.setIsCorrect(0);
                answer.setScore(0);
            }
        }
        examAnswerService.updateBatchById(answers);

        record.setScore(BigDecimal.valueOf(totalScore));
        record.setIsPass(totalScore >= paper.getPassScore() ? 1 : 0);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus("SUBMITTED");
        examRecordService.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("score", totalScore);
        result.put("totalScore", paper.getTotalScore());
        result.put("isPass", record.getIsPass() == 1);
        result.put("rightCount", rightCount);
        result.put("totalCount", answers.size());

        return Result.ok(result);
    }

    /**
     * 我的考试记录
     */
    @GetMapping("/records")
    @ApiOperation("我的考试记录")
    public Result<PageResult<ExamRecord>> myRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Page<ExamRecord> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime);

        Page<ExamRecord> result = examRecordService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 考试记录详情
     */
    @GetMapping("/records/{id}")
    @ApiOperation("考试记录详情")
    public Result<Map<String, Object>> recordDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        ExamRecord record = examRecordService.getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("考试记录不存在");
        }

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
