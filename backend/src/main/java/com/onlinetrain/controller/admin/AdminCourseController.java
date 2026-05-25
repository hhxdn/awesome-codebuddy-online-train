package com.onlinetrain.controller.admin;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/courses")
@Api(tags = "管理端-课程管理")
public class AdminCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @GetMapping
    @ApiOperation("课程列表")
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Course> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (categoryId != null) wrapper.eq(Course::getCategoryId, categoryId);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Course::getTitle, keyword);
        if (status != null && !status.isEmpty()) wrapper.eq(Course::getStatus, status);
        wrapper.orderByDesc(Course::getCreateTime);

        return Result.ok(PageResult.of(courseService.page(pageParam, wrapper)));
    }

    @PostMapping
    @ApiOperation("创建课程")
    public Result<Course> create(@RequestBody Course course) {
        course.setStudentCount(0);
        course.setStatus("UP");
        courseService.save(course);
        return Result.ok(course);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新课程")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.ok(course);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除课程")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("切换课程状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam String status) {
        Course course = courseService.getById(id);
        if (course != null) {
            course.setStatus(status);
            courseService.updateById(course);
        }
        return Result.ok();
    }

    @PostMapping("/{id}/chapters")
    @ApiOperation("添加章节")
    public Result<Chapter> addChapter(@PathVariable Long id, @RequestBody Chapter chapter) {
        chapter.setCourseId(id);
        chapterService.save(chapter);
        return Result.ok(chapter);
    }

    @PutMapping("/{id}/chapters/{chapterId}")
    @ApiOperation("更新章节")
    public Result<Chapter> updateChapter(@PathVariable Long id, @PathVariable Long chapterId, @RequestBody Chapter chapter) {
        chapter.setId(chapterId);
        chapter.setCourseId(id);
        chapterService.updateById(chapter);
        return Result.ok(chapter);
    }

    @DeleteMapping("/{id}/chapters/{chapterId}")
    @ApiOperation("删除章节")
    public Result<Void> deleteChapter(@PathVariable Long id, @PathVariable Long chapterId) {
        chapterService.removeById(chapterId);
        return Result.ok();
    }
}
