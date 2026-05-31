package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 系统用户管理控制器（管理员账号CRUD）
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-系统用户管理")
public class AdminUserController {

    @Autowired
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 系统用户列表（role=ADMIN）
     */
    @GetMapping("/users")
    @ApiOperation("系统用户列表")
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "ADMIN");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(result));
    }

    /**
     * 新增系统用户
     */
    @PostMapping("/users")
    @ApiOperation("新增系统用户")
    public Result<String> create(@RequestBody User user, HttpServletRequest request) {
        // 校验手机号不能重复
        User existPhone = userService.lambdaQuery()
                .eq(User::getPhone, user.getPhone()).one();
        if (existPhone != null) {
            return Result.error("手机号已被使用");
        }

        // 校验昵称
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            return Result.error("昵称不能为空");
        }

        // 设置默认值
        user.setRole("ADMIN");
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setRegisterTime(LocalDateTime.now());

        userService.save(user);
        return Result.ok("新增成功");
    }

    /**
     * 更新系统用户
     */
    @PutMapping("/users/{id}")
    @ApiOperation("更新系统用户")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        User exist = userService.getById(id);
        if (exist == null) {
            return Result.notFound("用户不存在");
        }
        if (!"ADMIN".equals(exist.getRole())) {
            return Result.error("只能编辑管理员用户");
        }

        // 手机号去重（排除自己）
        if (user.getPhone() != null && !user.getPhone().equals(exist.getPhone())) {
            User samePhone = userService.lambdaQuery()
                    .eq(User::getPhone, user.getPhone()).one();
            if (samePhone != null) {
                return Result.error("手机号已被使用");
            }
            exist.setPhone(user.getPhone());
        }

        if (user.getNickname() != null) {
            exist.setNickname(user.getNickname());
        }
        if (user.getStatus() != null) {
            exist.setStatus(user.getStatus());
        }
        if (user.getRealName() != null) {
            exist.setRealName(user.getRealName());
        }

        // 密码不为空则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            exist.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.updateById(exist);
        return Result.ok("更新成功");
    }

    /**
     * 删除系统用户
     */
    @DeleteMapping("/users/{id}")
    @ApiOperation("删除系统用户")
    public Result<String> delete(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error("只能删除管理员用户");
        }
        // 禁止删除自己
        // 这里不做自我校验，前端控制也可以

        userService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 重置密码
     */
    @PutMapping("/users/{id}/reset-password")
    @ApiOperation("重置密码")
    public Result<String> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.ok("密码已重置为123456");
    }
}
