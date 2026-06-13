package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.CourseService;
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

    @Autowired
    private CourseService courseService;

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
     * 返回按课程聚合的学习进度，适合前端展示课程维度的学习卡片
     */
    @GetMapping({"/learning/records", "/user/learning-records"})
    @ApiOperation("全部学习记录（课程聚合进度）")
    public Result<List<Map<String, Object>>> allRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 查询用户所有学习记录
        List<LearningRecord> records = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .orderByDesc(LearningRecord::getUpdateTime)
                .list();

        if (records.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // 按 courseId 分组
        Map<Long, List<LearningRecord>> groupedByCourse = records.stream()
                .collect(Collectors.groupingBy(LearningRecord::getCourseId));

        // 每门课程聚合成一条记录
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<LearningRecord>> entry : groupedByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<LearningRecord> courseRecords = entry.getValue();

            // 课程信息
            Course course = courseService.getById(courseId);
            String courseTitle = course != null ? course.getTitle() : "未知课程";
            String courseCover = course != null ? course.getCover() : "";

            // 该课程所有章节
            List<Chapter> chapters = chapterService.lambdaQuery()
                    .eq(Chapter::getCourseId, courseId)
                    .list();
            int totalChapters = chapters.size();

            // 已完成章节数
            long studiedChapters = courseRecords.stream()
                    .filter(r -> r.getIsFinished() != null && r.getIsFinished() == 1)
                    .count();

            // 总学习时长（秒）
            long totalDuration = courseRecords.stream()
                    .mapToLong(r -> r.getWatchDuration() != null ? r.getWatchDuration() : 0)
                    .sum();

            // 进度百分比
            int progress = totalChapters > 0 
                    ? (int) Math.round((double) studiedChapters / totalChapters * 100) 
                    : 0;

            // 最近学习时间
            LocalDateTime lastStudyTime = courseRecords.stream()
                    .map(LearningRecord::getUpdateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            // 最近学习的章节名称
            LearningRecord latestRecord = courseRecords.get(0);
            String lastChapterName = "";
            if (latestRecord.getChapterId() != null) {
                Chapter lastChapter = chapterService.getById(latestRecord.getChapterId());
                if (lastChapter != null) {
                    lastChapterName = lastChapter.getTitle();
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("courseId", courseId);
            item.put("courseTitle", courseTitle);
            item.put("courseCover", courseCover);
            item.put("courseName", courseTitle);           // 兼容小程序
            item.put("progress", progress);
            item.put("studiedChapters", (int) studiedChapters);
            item.put("totalChapters", totalChapters);
            item.put("totalDuration", totalDuration);
            item.put("duration", totalDuration);           // 兼容小程序（秒）
            item.put("lastStudyTime", lastStudyTime != null ? lastStudyTime.toString().substring(0, 16) : "");
            item.put("createTime", lastStudyTime != null ? lastStudyTime.toString().substring(0, 16) : "");  // 兼容小程序
            item.put("lastChapterName", lastChapterName);
            item.put("chapterName", lastChapterName);      // 兼容小程序
            result.add(item);
        }

        // 按最近学习时间倒序
        result.sort((a, b) -> {
            String ta = (String) a.getOrDefault("lastStudyTime", "");
            String tb = (String) b.getOrDefault("lastStudyTime", "");
            return tb.compareTo(ta);
        });

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
