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
 * H5考试控制器 - 支持 /api/exam 和 /api/exams 两种路径
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-考试接口")
public class H5ExamController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private ExamPaperQuestionService examPaperQuestionService;

    @Autowired
    private QuestionService questionService;

    /**
     * 试卷列表（支持指定课程或分类筛选）
     */
    @GetMapping("/exams")
    @ApiOperation("试卷列表")
    public Result<List<Map<String, Object>>> listExams(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String examType) {
        List<ExamPaper> papers;
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamPaper> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getStatus, "PUBLISHED");
        if (courseId != null) wrapper.eq(ExamPaper::getCourseId, courseId);
        if (examType != null && !examType.isEmpty()) wrapper.eq(ExamPaper::getExamType, examType);
        papers = examPaperService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamPaper paper : papers) {
            long qCount = examPaperQuestionService.lambdaQuery()
                    .eq(ExamPaperQuestion::getExamPaperId, paper.getId())
                    .count();
            Map<String, Object> item = new HashMap<>();
            item.put("id", paper.getId());
            item.put("name", paper.getTitle());
            item.put("duration", paper.getDurationMinutes());
            item.put("totalScore", paper.getTotalScore());
            item.put("passScore", paper.getPassScore());
            item.put("questionCount", qCount);
            item.put("examType", paper.getExamType() != null ? paper.getExamType() : "ONLINE");
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 试卷详情
     */
    @GetMapping("/exams/{paperId}")
    @ApiOperation("试卷详情")
    public Result<Map<String, Object>> examDetail(@PathVariable Long paperId) {
        ExamPaper paper = examPaperService.getById(paperId);
        if (paper == null) return Result.notFound("试卷不存在");

        long qCount = examPaperQuestionService.lambdaQuery()
                .eq(ExamPaperQuestion::getExamPaperId, paperId)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("id", paper.getId());
        result.put("name", paper.getTitle());
        result.put("duration", paper.getDurationMinutes());
        result.put("totalScore", paper.getTotalScore());
        result.put("passScore", paper.getPassScore());
        result.put("questionCount", qCount);
        return Result.ok(result);
    }

    /**
     * 开始考试 - body: {paperId}
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/exam/start")
    @ApiOperation("开始考试")
    public Result<Map<String, Object>> startExam(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long paperId = Long.valueOf(params.get("paperId").toString());

        ExamPaper paper = examPaperService.getById(paperId);
        if (paper == null || !"PUBLISHED".equals(paper.getStatus())) {
            throw new BusinessException("试卷不可用");
        }

        // 线下考试不能在线参加，只能由管理员录入成绩
        if ("OFFLINE".equals(paper.getExamType())) {
            throw new BusinessException("此考试为线下考试，请参加线下考试，成绩将由管理员录入");
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
            List<ExamAnswer> answers = examAnswerService.lambdaQuery()
                    .eq(ExamAnswer::getExamRecordId, doingRecord.getId())
                    .list();

            List<Question> questions = new ArrayList<>();
            for (ExamAnswer answer : answers) {
                Question q = questionService.getById(answer.getQuestionId());
                if (q != null) {
                    q.setAnalysis(null);
                    questions.add(q);
                }
            }
            questionService.enrichForDisplay(questions);
            questions.forEach(q -> q.setAnswer(null));

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
            q.setAnalysis(null);
        });
        // 加载选项并转换答案为索引格式
        questionService.enrichForDisplay(questions);
        // 考试中清除正确答案
        questions.forEach(q -> q.setAnswer(null));

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
     * 提交考试 - body: {recordId, answers: [{questionId, answer}], cheatCount?}
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/exam/submit")
    @ApiOperation("提交考试")
    public Result<Map<String, Object>> submitExam(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long recordId = Long.valueOf(params.get("recordId").toString());
        List<Map<String, Object>> userAnswers = (List<Map<String, Object>>) params.get("answers");

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
            String correctIndex = questionService.toIndexAnswer(question);
            if (userAnswer != null && userAnswer.equals(correctIndex)) {
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
        result.put("paperName", paper.getTitle());
        result.put("score", totalScore);
        result.put("totalScore", paper.getTotalScore());
        result.put("passScore", paper.getPassScore());
        result.put("isPass", record.getIsPass() == 1);
        result.put("rightCount", rightCount);
        result.put("totalCount", answers.size());

        return Result.ok(result);
    }

    /**
     * 获取考试题目的问题列表
     */
    @GetMapping("/exam/records/{recordId}/questions")
    @ApiOperation("获取考试题目")
    public Result<Map<String, Object>> recordQuestions(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        ExamRecord record = examRecordService.getById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("考试记录不存在");
        }

        ExamPaper paper = examPaperService.getById(record.getExamPaperId());
        List<Question> questions = questionService.getExamPaperQuestions(record.getExamPaperId());
        questions.forEach(q -> {
            q.setAnalysis(null);
        });
        questionService.enrichForDisplay(questions);
        questions.forEach(q -> q.setAnswer(null));

        Map<String, Object> result = new HashMap<>();
        result.put("questions", questions);
        result.put("duration", paper != null ? paper.getDurationMinutes() * 60 : 3600);
        return Result.ok(result);
    }

    /**
     * 我的考试记录（同时支持 /exam/records 和 /user/exam-records）
     */
    @GetMapping({"/exam/records", "/user/exam-records"})
    @ApiOperation("我的考试记录")
    public Result<PageResult<Map<String, Object>>> myRecords(
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

        // 转换为前端期望的格式（包含 paperName 等）
        List<Map<String, Object>> records = new ArrayList<>();
        for (ExamRecord r : result.getRecords()) {
            ExamPaper paper = examPaperService.getById(r.getExamPaperId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("paperName", paper != null ? paper.getTitle() : "");
            item.put("score", r.getScore());
            item.put("totalScore", paper != null ? paper.getTotalScore() : 100);
            item.put("passed", r.getIsPass() == 1);
            item.put("duration", 0);
            item.put("createTime", r.getCreateTime() != null ? r.getCreateTime().toString() : "");
            records.add(item);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        return Result.ok(pageResult);
    }

    /**
     * 考试记录详情
     */
    @GetMapping("/exam/records/{id}")
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

        // 为回顾模式加载选项和正确答案（索引格式）
        List<Question> reviewQuestions = new ArrayList<>();
        for (Map<String, Object> d : answerDetails) {
            Question q = (Question) d.get("question");
            if (q != null) reviewQuestions.add(q);
        }
        questionService.enrichForDisplay(reviewQuestions);

        // 试卷总题数（从 exam_paper_question 表统计，即使无答题记录也能获取）
        long questionCount = examPaperQuestionService.lambdaQuery()
                .eq(ExamPaperQuestion::getExamPaperId, paper != null ? paper.getId() : null)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("paper", paper);
        result.put("answers", answerDetails);
        result.put("questionCount", (int) questionCount);

        return Result.ok(result);
    }

    /**
     * 课程下试卷列表（兼容旧路径，供 H5CourseController 调用）
     */
    @GetMapping("/courses/{courseId}/exams")
    @ApiOperation("课程试卷列表")
    public Result<List<Map<String, Object>>> courseExams(@PathVariable Long courseId) {
        return listExams(courseId, null, null);
    }
}
