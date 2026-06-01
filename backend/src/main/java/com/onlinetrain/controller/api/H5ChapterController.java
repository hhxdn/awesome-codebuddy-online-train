package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.entity.Question;
import com.onlinetrain.entity.StudentExerciseAccess;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.LearningRecordService;
import com.onlinetrain.service.QuestionService;
import com.onlinetrain.service.StudentExerciseAccessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5章节控制器
 */
@RestController
@RequestMapping("/api/chapters")
@Api(tags = "H5-章节接口")
public class H5ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private StudentExerciseAccessService exerciseAccessService;

    /**
     * 章节详情
     */
    @GetMapping("/{chapterId}")
    @ApiOperation("章节详情")
    public Result<Chapter> detail(@PathVariable Long chapterId) {
        Chapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            return Result.notFound("章节不存在");
        }
        return Result.ok(chapter);
    }

    /**
     * 章节题目列表（练习用，隐藏答案）
     * 需要管理员开通习题权限后才能访问
     */
    @GetMapping("/{chapterId}/questions")
    @ApiOperation("章节题目列表")
    public Result<List<Question>> questions(@PathVariable Long chapterId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Chapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            return Result.notFound("章节不存在");
        }

        // 检查习题权限：管理员开通后学员才能看到习题
        Long courseId = chapter.getCourseId();
        StudentExerciseAccess access = exerciseAccessService.lambdaQuery()
                .eq(StudentExerciseAccess::getUserId, userId)
                .eq(StudentExerciseAccess::getCourseId, courseId)
                .one();
        if (access == null) {
            return Result.error(403, "习题练习权限未开通，请联系管理员");
        }

        List<Question> questions = questionService.lambdaQuery()
                .eq(Question::getChapterId, chapterId)
                .eq(Question::getStatus, 1)
                .list();
        questionService.enrichForDisplay(questions);
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setAnalysis(null);
        });
        return Result.ok(questions);
    }

    /**
     * 标记章节学习完成
     */
    @PostMapping("/{chapterId}/finish")
    @ApiOperation("章节学习完成")
    public Result<Void> finishChapter(@PathVariable Long chapterId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Chapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            return Result.notFound("章节不存在");
        }

        // 查找或创建学习记录
        LearningRecord existing = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getChapterId, chapterId)
                .one();

        if (existing != null) {
            existing.setIsFinished(1);
            existing.setWatchPercent(BigDecimal.valueOf(100));
            existing.setWatchDuration(chapter.getVideoDuration() != null ? chapter.getVideoDuration().longValue() : 0L);
            existing.setUpdateTime(LocalDateTime.now());
            learningRecordService.updateById(existing);
        } else {
            LearningRecord record = new LearningRecord();
            record.setUserId(userId);
            record.setCourseId(chapter.getCourseId());
            record.setChapterId(chapterId);
            record.setIsFinished(1);
            record.setWatchPercent(BigDecimal.valueOf(100));
            record.setWatchDuration(chapter.getVideoDuration() != null ? chapter.getVideoDuration().longValue() : 0L);
            record.setLastPosition(0L);
            record.setCreateTime(LocalDateTime.now());
            learningRecordService.save(record);
        }
        return Result.ok();
    }

    /**
     * 章节练习统计
     */
    @GetMapping("/{chapterId}/practice/stats")
    @ApiOperation("章节练习统计")
    public Result<Map<String, Object>> practiceStats(@PathVariable Long chapterId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<Question> questions = questionService.lambdaQuery()
                .eq(Question::getChapterId, chapterId)
                .eq(Question::getStatus, 1)
                .list();

        LearningRecord record = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getChapterId, chapterId)
                .one();

        Map<String, Object> result = new HashMap<>();
        result.put("totalQuestions", questions.size());
        result.put("finished", record != null && record.getIsFinished() == 1);
        if (record != null) {
            result.put("watchPercent", record.getWatchPercent());
        }
        return Result.ok(result);
    }
}
