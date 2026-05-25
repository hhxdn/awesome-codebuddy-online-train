package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
@Api(tags = "管理端-学员管理")
public class AdminStudentController {

    @Autowired
    private UserService userService;

    @GetMapping
    @ApiOperation("学员列表")
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));

        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/{id}/detail")
    @ApiOperation("学员详情")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.ok(user);
    }
}
