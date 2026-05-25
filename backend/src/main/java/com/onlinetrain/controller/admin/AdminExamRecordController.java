package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.ExamRecord;
import com.onlinetrain.service.ExamRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/exam-records")
@Api(tags = "管理端-考试记录")
public class AdminExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    @GetMapping
    @ApiOperation("考试记录列表")
    public Result<PageResult<ExamRecord>> list(
            @RequestParam(required = false) Long examPaperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ExamRecord> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (examPaperId != null) wrapper.eq(ExamRecord::getExamPaperId, examPaperId);
        if (userId != null) wrapper.eq(ExamRecord::getUserId, userId);
        wrapper.orderByDesc(ExamRecord::getCreateTime);

        return Result.ok(PageResult.of(examRecordService.page(pageParam, wrapper)));
    }
}
