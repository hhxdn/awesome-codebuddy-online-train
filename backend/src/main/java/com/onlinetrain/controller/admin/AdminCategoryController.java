package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-课程分类")
public class AdminCategoryController {

    @Autowired
    private CourseCategoryService categoryService;

    @GetMapping("/categories")
    @ApiOperation("分类列表")
    public Result<List<CourseCategory>> list() {
        List<CourseCategory> list = categoryService.lambdaQuery()
                .orderByAsc(CourseCategory::getSortOrder)
                .list();
        return Result.ok(list);
    }

    @PostMapping("/categories")
    @ApiOperation("创建分类")
    public Result<CourseCategory> create(@RequestBody CourseCategory category) {
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
        categoryService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/categories/{id}/status")
    @ApiOperation("切换分类状态 - body: {status}")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        CourseCategory category = categoryService.getById(id);
        if (category != null && params.get("status") != null) {
            // status: 0=禁用, 1=启用
            Object statusObj = params.get("status");
            category.setSortOrder(Integer.valueOf(statusObj.toString()));
            categoryService.updateById(category);
        }
        return Result.ok();
    }
}
