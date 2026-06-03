package com.onlinetrain.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.entity.Course;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-课程管理")
public class AdminCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @GetMapping("/courses")
    @ApiOperation("课程列表")
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Course> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (categoryId != null) wrapper.eq(Course::getCategoryId, categoryId);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Course::getTitle, keyword);
        if (status != null && !status.isEmpty()) wrapper.eq(Course::getStatus, status);
        if (courseType != null && !courseType.isEmpty()) wrapper.eq(Course::getCourseType, courseType);
        wrapper.orderByDesc(Course::getCreateTime);

        return Result.ok(PageResult.of(courseService.page(pageParam, wrapper)));
    }

    @GetMapping("/courses/{id}")
    @ApiOperation("课程详情")
    public Result<JSONObject> detail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) return Result.notFound("课程不存在");

        // 查询章节列表
        List<Chapter> chapters = chapterService.list(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getCourseId, id)
                        .orderByAsc(Chapter::getSortOrder)
        );

        JSONObject result = (JSONObject) JSON.toJSON(course);
        result.put("coverUrl", course.getCover());  // 前端用 coverUrl
        result.put("chapters", chapters);
        return Result.ok(result);
    }

    @PostMapping("/courses")
    @ApiOperation("创建课程")
    @Transactional
    public Result<JSONObject> create(@RequestBody Map<String, Object> params) {
        // 提取并移除 chapters，避免反序列化到 Course 时报错
        Object chaptersRaw = params.remove("chapters");
        // 兼容前端 coverUrl -> 后端 cover
        if (params.containsKey("coverUrl")) {
            params.put("cover", params.remove("coverUrl"));
        }

        Course course = JSON.parseObject(JSON.toJSONString(params), Course.class);
        course.setStudentCount(0);
        if (course.getStatus() == null) course.setStatus("UP");
        courseService.save(course);

        // 保存章节
        List<Chapter> savedChapters = new ArrayList<>();
        if (chaptersRaw instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chaptersList = (List<Map<String, Object>>) chaptersRaw;
            for (int i = 0; i < chaptersList.size(); i++) {
                Map<String, Object> ch = chaptersList.get(i);
                Chapter chapter = new Chapter();
                chapter.setCourseId(course.getId());
                chapter.setTitle((String) ch.get("title"));
                chapter.setVideoUrl((String) ch.get("videoUrl"));
                chapter.setSortOrder(ch.get("sortOrder") != null
                        ? Integer.valueOf(ch.get("sortOrder").toString()) : i + 1);
                // VOD 字段：如果前端传了 fileId，说明是通过 VOD 上传的，记录 fileId 和即时播放地址
                String fileId = (String) ch.get("fileId");
                String playbackUrl = (String) ch.get("playbackUrl");
                if (fileId != null && !fileId.isEmpty()) {
                    chapter.setVodFileId(fileId);
                    chapter.setVodPlaybackUrl(playbackUrl != null ? playbackUrl : chapter.getVideoUrl());
                    chapter.setVodTranscodeStatus("PENDING");
                }
                chapterService.save(chapter);
                savedChapters.add(chapter);
            }
        }

        JSONObject result = (JSONObject) JSON.toJSON(course);
        result.put("coverUrl", course.getCover());
        result.put("chapters", savedChapters);
        return Result.ok(result);
    }

    @PutMapping("/courses/{id}")
    @ApiOperation("更新课程")
    @Transactional
    public Result<JSONObject> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        // 提取并移除 chapters
        Object chaptersRaw = params.remove("chapters");
        // 兼容前端 coverUrl -> 后端 cover
        if (params.containsKey("coverUrl")) {
            params.put("cover", params.remove("coverUrl"));
        }

        params.put("id", id);
        Course course = JSON.parseObject(JSON.toJSONString(params), Course.class);
        courseService.updateById(course);

        // 先删除旧章节，再重新保存（保留旧章节的 VOD 信息用于新记录）
        List<Chapter> oldChapters = chapterService.list(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, id));

        chapterService.remove(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, id));

        List<Chapter> savedChapters = new ArrayList<>();
        if (chaptersRaw instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chaptersList = (List<Map<String, Object>>) chaptersRaw;
            for (int i = 0; i < chaptersList.size(); i++) {
                Map<String, Object> ch = chaptersList.get(i);
                Chapter chapter = new Chapter();
                chapter.setCourseId(id);
                chapter.setTitle((String) ch.get("title"));
                chapter.setVideoUrl((String) ch.get("videoUrl"));
                chapter.setSortOrder(ch.get("sortOrder") != null
                        ? Integer.valueOf(ch.get("sortOrder").toString()) : i + 1);
                // VOD 字段处理：
                // 1. 如果前端传了 fileId（新上传），使用新的 VOD 信息
                // 2. 如果前端传了 vodFileId（编辑已有章节），保留原有 VOD 信息
                String fileId = (String) ch.get("fileId");
                String vodFileId = (String) ch.get("vodFileId");
                String playbackUrl = (String) ch.get("playbackUrl");
                String vodPlaybackUrl = (String) ch.get("vodPlaybackUrl");
                String vodTranscodeStatus = (String) ch.get("vodTranscodeStatus");

                if (fileId != null && !fileId.isEmpty()) {
                    // 新上传的视频
                    chapter.setVodFileId(fileId);
                    chapter.setVodPlaybackUrl(playbackUrl != null ? playbackUrl : chapter.getVideoUrl());
                    chapter.setVodTranscodeStatus("PENDING");
                } else if (vodFileId != null && !vodFileId.isEmpty()) {
                    // 编辑已有章节，保留 VOD 信息
                    chapter.setVodFileId(vodFileId);
                    chapter.setVodPlaybackUrl(vodPlaybackUrl);
                    chapter.setVodTranscodeStatus(vodTranscodeStatus != null ? vodTranscodeStatus : "DONE");
                }
                chapterService.save(chapter);
                savedChapters.add(chapter);
            }
        }

        JSONObject result = (JSONObject) JSON.toJSON(course);
        result.put("coverUrl", course.getCover());
        result.put("chapters", savedChapters);
        return Result.ok(result);
    }

    @DeleteMapping("/courses/{id}")
    @ApiOperation("删除课程")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/courses/{id}/status")
    @ApiOperation("切换课程状态 - body: {status}")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Course course = courseService.getById(id);
        if (course != null && params.get("status") != null) {
            course.setStatus(params.get("status").toString());
            courseService.updateById(course);
        }
        return Result.ok();
    }

    @PostMapping("/courses/{id}/chapters")
    @ApiOperation("添加章节")
    public Result<Chapter> addChapter(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Chapter chapter = new Chapter();
        chapter.setCourseId(id);
        chapter.setTitle((String) params.get("title"));
        chapter.setVideoUrl((String) params.get("videoUrl"));
        chapter.setSortOrder(params.get("sortOrder") != null
                ? Integer.valueOf(params.get("sortOrder").toString()) : 0);
        // VOD 字段
        String fileId = (String) params.get("fileId");
        String playbackUrl = (String) params.get("playbackUrl");
        String vodFileId = (String) params.get("vodFileId");
        if (fileId != null && !fileId.isEmpty()) {
            chapter.setVodFileId(fileId);
            chapter.setVodPlaybackUrl(playbackUrl != null ? playbackUrl : chapter.getVideoUrl());
            chapter.setVodTranscodeStatus("PENDING");
        } else if (vodFileId != null && !vodFileId.isEmpty()) {
            chapter.setVodFileId(vodFileId);
            chapter.setVodPlaybackUrl((String) params.get("vodPlaybackUrl"));
            chapter.setVodTranscodeStatus(
                    params.get("vodTranscodeStatus") != null
                            ? params.get("vodTranscodeStatus").toString() : "DONE");
        }
        chapterService.save(chapter);
        return Result.ok(chapter);
    }

    @PutMapping("/courses/{id}/chapters/{chapterId}")
    @ApiOperation("更新章节")
    public Result<Chapter> updateChapter(@PathVariable Long id, @PathVariable Long chapterId,
                                         @RequestBody Map<String, Object> params) {
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setCourseId(id);
        chapter.setTitle((String) params.get("title"));
        chapter.setVideoUrl((String) params.get("videoUrl"));
        chapter.setSortOrder(params.get("sortOrder") != null
                ? Integer.valueOf(params.get("sortOrder").toString()) : 0);
        // VOD 字段
        String fileId = (String) params.get("fileId");
        String playbackUrl = (String) params.get("playbackUrl");
        String vodFileId = (String) params.get("vodFileId");
        if (fileId != null && !fileId.isEmpty()) {
            chapter.setVodFileId(fileId);
            chapter.setVodPlaybackUrl(playbackUrl != null ? playbackUrl : chapter.getVideoUrl());
            chapter.setVodTranscodeStatus("PENDING");
        } else if (vodFileId != null && !vodFileId.isEmpty()) {
            chapter.setVodFileId(vodFileId);
            chapter.setVodPlaybackUrl((String) params.get("vodPlaybackUrl"));
            chapter.setVodTranscodeStatus(
                    params.get("vodTranscodeStatus") != null
                            ? params.get("vodTranscodeStatus").toString() : "DONE");
        }
        chapterService.updateById(chapter);
        return Result.ok(chapter);
    }

    @DeleteMapping("/courses/{id}/chapters/{chapterId}")
    @ApiOperation("删除章节")
    public Result<Void> deleteChapter(@PathVariable Long id, @PathVariable Long chapterId) {
        chapterService.removeById(chapterId);
        return Result.ok();
    }
}
