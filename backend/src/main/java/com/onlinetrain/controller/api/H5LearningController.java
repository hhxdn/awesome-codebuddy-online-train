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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H5学习记录控制器
 */
@RestController
@RequestMapping("/api/h5/learning")
@Api(tags = "H5-学习记录接口")
public class H5LearningController {

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private ChapterService chapterService;

    /**
     * 保存学习进度
     */
    @PostMapping("/record")
    @ApiOperation("保存学习进度")
    public Result<LearningRecord> saveProgress(@RequestBody LearningRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        record.setUserId(userId);

        // 查找已存在的记录
        LearningRecord existing = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getChapterId, record.getChapterId())
                .one();

        if (existing != null) {
            existing.setWatchDuration(record.getWatchDuration());
            existing.setWatchPercent(record.getWatchPercent());
            existing.setLastPosition(record.getLastPosition());
            existing.setIsFinished(record.getIsFinished() != null ? record.getIsFinished() : 0);
            existing.setUpdateTime(LocalDateTime.now());
            learningRecordService.updateById(existing);
            return Result.ok(existing);
        } else {
            record.setCreateTime(LocalDateTime.now());
            record.setIsFinished(record.getIsFinished() != null ? record.getIsFinished() : 0);
            if (record.getWatchPercent() == null) {
                record.setWatchPercent(BigDecimal.ZERO);
            }
            if (record.getWatchDuration() == null) {
                record.setWatchDuration(0L);
            }
            if (record.getLastPosition() == null) {
                record.setLastPosition(0L);
            }
            learningRecordService.save(record);
            return Result.ok(record);
        }
    }

    /**
     * 课程学习记录
     */
    @GetMapping("/records/{courseId}")
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
     * 课程整体学习进度
     */
    @GetMapping("/progress/{courseId}")
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

        // 计算总学习时长
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
