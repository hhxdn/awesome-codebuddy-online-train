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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CourseService courseService;

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
            item.put("examTitle", paper != null ? paper.getTitle() : "");
            item.put("examType", paper != null ? paper.getExamType() : "ONLINE");
            item.put("userId", r.getUserId());
            item.put("score", r.getScore());
            item.put("totalScore", paper != null ? paper.getTotalScore() : 0);
            item.put("isPass", r.getIsPass());
            item.put("status", r.getStatus());
            item.put("startTime", r.getStartTime());
            item.put("submitTime", r.getSubmitTime());
            item.put("createTime", r.getCreateTime());

            // 检查是否已有证书
            Certificate existingCert = certificateService.lambdaQuery()
                    .eq(Certificate::getExamRecordId, r.getId())
                    .eq(Certificate::getStatus, 1)
                    .one();
            item.put("hasCertificate", existingCert != null);
            item.put("certificateId", existingCert != null ? existingCert.getId() : null);

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
            detail.put("questionContent", q != null ? q.getContent() : "");
            detail.put("userAnswer", a.getUserAnswer());
            detail.put("correctAnswer", q != null ? q.getAnswer() : "");
            detail.put("isCorrect", a.getIsCorrect());
            detail.put("score", a.getScore());
            detail.put("analysis", q != null ? q.getAnalysis() : "");
            answerDetails.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("paper", paper);
        result.put("answers", answerDetails);
        return Result.ok(result);
    }

    /**
     * 管理员录入线下考试成绩
     * body: { userId, examPaperId, score, isPass }
     */
    @PostMapping("/records/offline-score")
    @ApiOperation("录入线下考试成绩")
    public Result<Map<String, Object>> recordOfflineScore(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long examPaperId = Long.valueOf(params.get("examPaperId").toString());
        BigDecimal score = new BigDecimal(params.get("score").toString());
        int isPass = params.get("isPass") != null ? Integer.parseInt(params.get("isPass").toString()) : 0;

        ExamPaper paper = examPaperService.getById(examPaperId);
        if (paper == null) return Result.notFound("试卷不存在");
        if (!"OFFLINE".equals(paper.getExamType())) {
            return Result.error("只能录入线下考试成绩");
        }

        // 检查是否已录入
        ExamRecord existing = examRecordService.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamPaperId, examPaperId)
                .one();
        if (existing != null) {
            return Result.error("该学员已有此考试的记录");
        }

        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamPaperId(examPaperId);
        record.setScore(score);
        record.setIsPass(isPass);
        record.setStartTime(LocalDateTime.now());
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus("SUBMITTED");
        record.setCheatCount(0);
        examRecordService.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("score", score);
        result.put("isPass", isPass);
        return Result.ok("线下考试成绩录入成功", result);
    }

    /**
     * 根据线下考试记录颁发结业证书
     * body: { examRecordId }
     */
    @PostMapping("/records/{recordId}/issue-certificate")
    @ApiOperation("颁发结业证书（线下考试通过后）")
    public Result<Map<String, Object>> issueCertificate(@PathVariable Long recordId) {
        ExamRecord record = examRecordService.getById(recordId);
        if (record == null) return Result.notFound("考试记录不存在");

        ExamPaper paper = examPaperService.getById(record.getExamPaperId());
        if (paper == null) return Result.notFound("试卷不存在");

        // 线下考试且通过才能颁发证书
        if (!"OFFLINE".equals(paper.getExamType())) {
            return Result.error("仅线下考试通过后可颁发证书");
        }
        if (record.getIsPass() == null || record.getIsPass() != 1) {
            return Result.error("考试未通过，无法颁发证书");
        }

        // 检查是否已颁发
        Certificate existingCert = certificateService.lambdaQuery()
                .eq(Certificate::getExamRecordId, recordId)
                .eq(Certificate::getStatus, 1)
                .one();
        if (existingCert != null) {
            return Result.error("该考试记录已颁发过证书，证书编号：" + existingCert.getCertNo());
        }

        User user = userService.getById(record.getUserId());
        if (user == null) return Result.notFound("学员不存在");

        Course course = courseService.getById(paper.getCourseId());
        String courseName = course != null ? course.getTitle() : "课程";

        String title = courseName + " - 结业证书";
        String content = "兹证明 " + (user.getRealName() != null ? user.getRealName() : user.getNickname())
                + "（" + user.getPhone() + "）已完成「" + courseName + "」课程学习并通过线下考试，成绩合格，准予结业。";

        String certNo = "CERT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + record.getUserId();

        Certificate certificate = new Certificate();
        certificate.setUserId(record.getUserId());
        certificate.setCourseId(paper.getCourseId());
        certificate.setExamRecordId(recordId);
        certificate.setCertType("COURSE");
        certificate.setTitle(title);
        certificate.setContent(content);
        certificate.setCertNo(certNo);
        certificate.setIssueTime(LocalDateTime.now());
        certificate.setStatus(1);
        certificateService.save(certificate);

        Map<String, Object> result = new HashMap<>();
        result.put("id", certificate.getId());
        result.put("certNo", certNo);
        result.put("title", title);
        return Result.ok("证书颁发成功", result);
    }
}
