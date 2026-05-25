package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.LearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * H5学习记录控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-学习记录接口")
public class H5LearningController {

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private ChapterService chapterService;

    /**
     * 保存学习进度（完整格式）
     */
    @PostMapping("/learning/record")
    @ApiOperation("保存学习进度")
    public Result<LearningRecord> saveProgress(@RequestBody LearningRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        record.setUserId(userId);

        LearningRecord existing = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getChapterId, record.getChapterId())
                .one();

        if (existing != null) {
            if (record.getWatchDuration() != null) existing.setWatchDuration(record.getWatchDuration());
            if (record.getWatchPercent() != null) existing.setWatchPercent(record.getWatchPercent());
            if (record.getLastPosition() != null) existing.setLastPosition(record.getLastPosition());
            existing.setIsFinished(record.getIsFinished() != null ? record.getIsFinished() : 0);
            existing.setUpdateTime(LocalDateTime.now());
            learningRecordService.updateById(existing);
            return Result.ok(existing);
        } else {
            record.setCreateTime(LocalDateTime.now());
            record.setIsFinished(record.getIsFinished() != null ? record.getIsFinished() : 0);
            if (record.getWatchPercent() == null) record.setWatchPercent(BigDecimal.ZERO);
            if (record.getWatchDuration() == null) record.setWatchDuration(0L);
            if (record.getLastPosition() == null) record.setLastPosition(0L);
            learningRecordService.save(record);
            return Result.ok(record);
        }
    }

    /**
     * 保存学习进度（简化格式）- frontend VideoPlayer: {chapterId, position}
     */
    @PostMapping("/learning/progress")
    @ApiOperation("保存学习进度(简化)")
    public Result<Void> saveProgressSimple(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long chapterId = Long.valueOf(params.get("chapterId").toString());
        Long position = params.get("position") != null ? Long.valueOf(params.get("position").toString()) : 0L;

        Chapter chapter = chapterService.getById(chapterId);
        Long courseId = chapter != null ? chapter.getCourseId() : null;

        LearningRecord existing = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getChapterId, chapterId)
                .one();

        if (existing != null) {
            existing.setLastPosition(position);
            existing.setWatchDuration(existing.getWatchDuration() != null ? existing.getWatchDuration() + 1 : 1L);
            existing.setUpdateTime(LocalDateTime.now());
            learningRecordService.updateById(existing);
        } else {
            LearningRecord record = new LearningRecord();
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setChapterId(chapterId);
            record.setLastPosition(position);
            record.setWatchDuration(1L);
            record.setWatchPercent(BigDecimal.ZERO);
            record.setIsFinished(0);
            record.setCreateTime(LocalDateTime.now());
            learningRecordService.save(record);
        }
        return Result.ok();
    }

    /**
     * 课程学习记录
     */
    @GetMapping("/learning/records/{courseId}")
    @ApiOperation("课程学习记录")
    public Result<List<LearningRecord>> courseRecords(@PathVariable Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LearningRecord> records = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getCourseId, courseId)
                .list();
        return Result.ok(records);
    }

    /**
     * 全部学习记录（支持 /learning/records 和 /user/learning-records）
     */
    @GetMapping({"/learning/records", "/user/learning-records"})
    @ApiOperation("全部学习记录")
    public Result<List<Map<String, Object>>> allRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LearningRecord> records = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .orderByDesc(LearningRecord::getUpdateTime)
                .list();

        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("courseId", r.getCourseId());
            item.put("chapterId", r.getChapterId());
            item.put("watchDuration", r.getWatchDuration());
            item.put("watchPercent", r.getWatchPercent());
            item.put("lastPosition", r.getLastPosition());
            item.put("isFinished", r.getIsFinished());
            item.put("updateTime", r.getUpdateTime() != null ? r.getUpdateTime().toString() : "");
            return item;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 课程整体学习进度
     */
    @GetMapping("/learning/progress/{courseId}")
    @ApiOperation("课程学习进度")
    public Result<Map<String, Object>> courseProgress(@PathVariable Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<Chapter> chapters = chapterService.lambdaQuery()
                .eq(Chapter::getCourseId, courseId)
                .list();

        int totalChapters = chapters.size();
        if (totalChapters == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("progress", 0);
            result.put("finishedCount", 0);
            result.put("totalCount", 0);
            return Result.ok(result);
        }

        long finishedCount = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getCourseId, courseId)
                .eq(LearningRecord::getIsFinished, 1)
                .count();

        BigDecimal progress = BigDecimal.valueOf(finishedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalChapters), 2, RoundingMode.HALF_UP);

        Long totalDuration = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getCourseId, courseId)
                .list()
                .stream()
                .mapToLong(r -> r.getWatchDuration() != null ? r.getWatchDuration() : 0)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("progress", progress.doubleValue());
        result.put("finishedCount", finishedCount);
        result.put("totalCount", totalChapters);
        result.put("totalDuration", totalDuration);
        return Result.ok(result);
    }
}
