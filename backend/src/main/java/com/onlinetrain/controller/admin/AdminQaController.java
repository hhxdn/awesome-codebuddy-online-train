package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.QaQuestion;
import com.onlinetrain.service.QaQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-答疑解惑")
public class AdminQaController {

    @Autowired
    private QaQuestionService qaQuestionService;

    @GetMapping("/qa-questions")
    @ApiOperation("答疑列表")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<QaQuestion> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(QaQuestion::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(QaQuestion::getContent, keyword)
                    .or().like(QaQuestion::getPhone, keyword));
        }
        wrapper.orderByDesc(QaQuestion::getCreateTime);
        Page<QaQuestion> result = qaQuestionService.page(new Page<>(page, pageSize), wrapper);
        return Result.ok(PageResult.of(result));
    }

    @PutMapping("/qa-questions/{id}/process")
    @ApiOperation("标记已处理+回复")
    public Result<?> process(@PathVariable Long id, @RequestBody Map<String, String> body) {
        QaQuestion qa = new QaQuestion();
        qa.setId(id);
        qa.setStatus("PROCESSED");
        if (body.containsKey("reply")) {
            qa.setReply(body.get("reply"));
        }
        qa.setUpdateTime(LocalDateTime.now());
        qaQuestionService.updateById(qa);
        return Result.ok("已处理", null);
    }
}
