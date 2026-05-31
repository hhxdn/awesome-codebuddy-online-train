package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamPaper;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.QuestionOption;
import com.onlinetrain.service.ExamPaperService;
import com.onlinetrain.service.QuestionOptionService;
import com.onlinetrain.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 管理端-试卷管理控制器（路径匹配前端 /admin/exams）
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-试卷管理")
public class AdminExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionOptionService questionOptionService;

    /**
     * 试卷列表
     */
    @GetMapping("/exams")
    @ApiOperation("试卷列表")
    public Result<PageResult<ExamPaper>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String examType) {

        Page<ExamPaper> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamPaper> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (examType != null && !examType.isEmpty()) {
            wrapper.eq(ExamPaper::getExamType, examType);
        }
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
        result.put("examType", paper.getExamType() != null ? paper.getExamType() : "ONLINE");
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
        paper.setExamType(params.get("examType") != null ? params.get("examType").toString() : "ONLINE");
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

    /**
     * 试卷预览 - 返回试卷信息及完整题目详情（含选项和答案）
     */
    @GetMapping("/exams/{id}/preview")
    @ApiOperation("试卷预览")
    public Result<Map<String, Object>> preview(@PathVariable Long id) {
        ExamPaper paper = examPaperService.getById(id);
        if (paper == null) return Result.notFound("试卷不存在");

        List<Question> questions = questionService.getExamPaperQuestions(id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", paper.getId());
        result.put("title", paper.getTitle());
        result.put("courseId", paper.getCourseId());
        result.put("durationMinutes", paper.getDurationMinutes());
        result.put("totalScore", paper.getTotalScore());
        result.put("passScore", paper.getPassScore());
        result.put("maxAttempts", paper.getMaxAttempts());
        result.put("status", paper.getStatus());
        result.put("examType", paper.getExamType() != null ? paper.getExamType() : "ONLINE");
        result.put("questions", buildQuestionPreviewList(questions));
        result.put("questionCount", questions != null ? questions.size() : 0);
        return Result.ok(result);
    }

    private List<Map<String, Object>> buildQuestionPreviewList(List<Question> questions) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (questions == null) return list;
        for (Question q : questions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("type", q.getType());
            item.put("content", q.getContent());
            item.put("score", q.getScore());
            item.put("answer", q.getAnswer());
            item.put("analysis", q.getAnalysis());
            if ("SINGLE".equals(q.getType()) || "MULTIPLE".equals(q.getType())) {
                List<QuestionOption> opts = questionOptionService.lambdaQuery()
                        .eq(QuestionOption::getQuestionId, q.getId())
                        .orderByAsc(QuestionOption::getOptionLabel)
                        .list();
                item.put("options", opts.stream().map(o -> {
                    Map<String, Object> om = new HashMap<>();
                    om.put("label", o.getOptionLabel());
                    om.put("content", o.getContent());
                    om.put("isCorrect", o.getIsCorrect());
                    return om;
                }).collect(Collectors.toList()));
            }
            list.add(item);
        }
        return list;
    }

    /**
     * 随机组卷 - 从题库随机抽取指定数量的题目生成试卷
     * body: { courseId, title, singleCount(默认60), multipleCount(默认20), judgeCount(默认20),
     *         durationMinutes, passScore, maxAttempts, examType }
     */
    @PostMapping("/exams/random")
    @ApiOperation("随机组卷")
    public Result<Map<String, Object>> randomGenerate(@RequestBody Map<String, Object> params) {
        Long courseId = Long.valueOf(params.get("courseId").toString());
        int singleCount = params.get("singleCount") != null ? Integer.parseInt(params.get("singleCount").toString()) : 60;
        int multipleCount = params.get("multipleCount") != null ? Integer.parseInt(params.get("multipleCount").toString()) : 20;
        int judgeCount = params.get("judgeCount") != null ? Integer.parseInt(params.get("judgeCount").toString()) : 20;

        // 随机抽取单选题
        List<Question> singleQuestions = questionService.lambdaQuery()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getType, "SINGLE")
                .eq(Question::getStatus, 1)
                .list();
        Collections.shuffle(singleQuestions);
        List<Question> selectedSingle = singleQuestions.stream().limit(singleCount).collect(Collectors.toList());

        // 随机抽取多选题
        List<Question> multipleQuestions = questionService.lambdaQuery()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getType, "MULTIPLE")
                .eq(Question::getStatus, 1)
                .list();
        Collections.shuffle(multipleQuestions);
        List<Question> selectedMultiple = multipleQuestions.stream().limit(multipleCount).collect(Collectors.toList());

        // 随机抽取判断题
        List<Question> judgeQuestions = questionService.lambdaQuery()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getType, "JUDGE")
                .eq(Question::getStatus, 1)
                .list();
        Collections.shuffle(judgeQuestions);
        List<Question> selectedJudge = judgeQuestions.stream().limit(judgeCount).collect(Collectors.toList());

        // 计算总分
        int totalScore = 0;
        for (Question q : selectedSingle) totalScore += q.getScore() != null ? q.getScore() : 1;
        for (Question q : selectedMultiple) totalScore += q.getScore() != null ? q.getScore() : 1;
        for (Question q : selectedJudge) totalScore += q.getScore() != null ? q.getScore() : 1;

        // 创建试卷
        String title = params.get("title") != null ? params.get("title").toString()
                : "随机组卷 - 单选" + selectedSingle.size() + " 多选" + selectedMultiple.size() + " 判断" + selectedJudge.size();

        ExamPaper paper = new ExamPaper();
        paper.setCourseId(courseId);
        paper.setTitle(title);
        paper.setDurationMinutes(params.get("durationMinutes") != null ? Integer.parseInt(params.get("durationMinutes").toString()) : 120);
        paper.setTotalScore(totalScore);
        paper.setPassScore(params.get("passScore") != null ? Integer.parseInt(params.get("passScore").toString()) : 60);
        paper.setMaxAttempts(params.get("maxAttempts") != null ? Integer.parseInt(params.get("maxAttempts").toString()) : 1);
        paper.setStatus("PUBLISHED");
        paper.setExamType(params.get("examType") != null ? params.get("examType").toString() : "ONLINE");
        examPaperService.save(paper);

        // 组合所有题目并保存关联
        List<Long> allQuestionIds = new ArrayList<>();
        selectedSingle.forEach(q -> allQuestionIds.add(q.getId()));
        selectedMultiple.forEach(q -> allQuestionIds.add(q.getId()));
        selectedJudge.forEach(q -> allQuestionIds.add(q.getId()));
        examPaperService.savePaperQuestions(paper.getId(), allQuestionIds);

        Map<String, Object> result = new HashMap<>();
        result.put("paperId", paper.getId());
        result.put("title", paper.getTitle());
        result.put("totalScore", totalScore);
        result.put("singleCount", selectedSingle.size());
        result.put("multipleCount", selectedMultiple.size());
        result.put("judgeCount", selectedJudge.size());
        result.put("totalQuestions", allQuestionIds.size());
        result.put("message", "随机组卷成功！单选" + selectedSingle.size() + "道、多选" + selectedMultiple.size() + "道、判断" + selectedJudge.size() + "道，共" + allQuestionIds.size() + "题，总分" + totalScore + "分");
        return Result.ok(result);
    }
}
