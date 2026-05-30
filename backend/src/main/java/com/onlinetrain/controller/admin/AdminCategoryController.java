package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-课程分类")
public class AdminCategoryController {

    @Autowired
    private CourseCategoryService categoryService;

    /**
     * 树形分类列表
     */
    @GetMapping("/categories/tree")
    @ApiOperation("分类树")
    public Result<List<CourseCategory>> tree() {
        List<CourseCategory> all = categoryService.lambdaQuery()
                .orderByAsc(CourseCategory::getSortOrder)
                .list();

        // 构建树形结构
        Map<Long, List<CourseCategory>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CourseCategory::getParentId));

        List<CourseCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .peek(c -> c.setChildren(childrenMap.getOrDefault(c.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return Result.ok(roots);
    }

    @GetMapping("/categories")
    @ApiOperation("分类列表（扁平，兼容旧接口）")
    public Result<List<CourseCategory>> list() {
        List<CourseCategory> list = categoryService.lambdaQuery()
                .orderByAsc(CourseCategory::getLevel, CourseCategory::getSortOrder)
                .list();
        return Result.ok(list);
    }

    @PostMapping("/categories")
    @ApiOperation("创建分类")
    public Result<CourseCategory> create(@RequestBody CourseCategory category) {
        // 自动设置level
        if (category.getParentId() != null && category.getParentId() > 0) {
            CourseCategory parent = categoryService.getById(category.getParentId());
            category.setLevel(parent != null ? parent.getLevel() + 1 : 2);
        } else {
            category.setLevel(1);
        }
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getIsFree() == null) category.setIsFree(0);
        categoryService.save(category);
        return Result.ok(category);
    }

    @PutMapping("/categories/{id}")
    @ApiOperation("更新分类")
    public Result<CourseCategory> update(@PathVariable Long id, @RequestBody CourseCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.ok(category);
    }

    @DeleteMapping("/categories/{id}")
    @ApiOperation("删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        // 检查是否有子分类
        long childCount = categoryService.lambdaQuery()
                .eq(CourseCategory::getParentId, id).count();
        if (childCount > 0) {
            return Result.error("请先删除子分类");
        }
        categoryService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/categories/{id}/status")
    @ApiOperation("切换分类状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        CourseCategory category = categoryService.getById(id);
        if (category != null && params.get("status") != null) {
            Object statusObj = params.get("status");
            category.setStatus(Integer.valueOf(statusObj.toString()));
            categoryService.updateById(category);
        }
        return Result.ok();
    }
}
