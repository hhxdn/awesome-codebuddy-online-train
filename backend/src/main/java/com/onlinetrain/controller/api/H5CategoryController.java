package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * H5分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@Api(tags = "H5-分类接口")
public class H5CategoryController {

    @Autowired
    private CourseCategoryService categoryService;

    /**
     * 树形分类列表（含多级结构）
     */
    @GetMapping("/tree")
    @ApiOperation("分类树")
    public Result<List<CourseCategory>> tree() {
        List<CourseCategory> all = categoryService.lambdaQuery()
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getSortOrder)
                .list();

        Map<Long, List<CourseCategory>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CourseCategory::getParentId));

        // 递归设置每个节点的children
        for (CourseCategory category : all) {
            List<CourseCategory> children = childrenMap.get(category.getId());
            if (children != null) {
                category.setChildren(children);
            }
        }

        List<CourseCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        return Result.ok(roots);
    }

    @GetMapping
    @ApiOperation("分类列表（扁平，兼容旧接口）")
    public Result<List<CourseCategory>> list() {
        List<CourseCategory> list = categoryService.lambdaQuery()
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getLevel, CourseCategory::getSortOrder)
                .list();
        return Result.ok(list);
    }

    /**
     * 分类详情（用于购买页展示）
     */
    @GetMapping("/{id}")
    @ApiOperation("分类详情")
    public Result<CourseCategory> detail(@PathVariable Long id) {
        CourseCategory category = categoryService.getById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.ok(category);
    }
}
