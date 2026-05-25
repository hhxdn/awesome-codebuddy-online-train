package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * H5分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@Api(tags = "H5-分类接口")
public class H5CategoryController {

    @Autowired
    private CourseCategoryService categoryService;

    @GetMapping
    @ApiOperation("分类列表")
    public Result<List<CourseCategory>> list() {
        List<CourseCategory> list = categoryService.lambdaQuery()
                .eq(CourseCategory::getStatus, 1)
                .orderByAsc(CourseCategory::getSortOrder)
                .list();
        return Result.ok(list);
    }
}
