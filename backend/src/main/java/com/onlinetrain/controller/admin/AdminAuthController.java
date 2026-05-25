package com.onlinetrain.controller.admin;

import com.onlinetrain.common.BusinessException;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.UserService;
import com.onlinetrain.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
@Api(tags = "管理端-认证接口")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String password = params.get("password");

        if (account == null || password == null) {
            return Result.error("账号和密码不能为空");
        }

        User admin = userService.lambdaQuery()
                .eq(User::getPhone, account)
                .eq(User::getRole, "ADMIN")
                .one();

        if (admin == null) {
            throw new BusinessException("账号不存在");
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtils.createToken(admin.getId(), admin.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        admin.setPassword(null);
        result.put("user", admin);

        return Result.ok(result);
    }

    /**
     * 管理员信息
     */
    @GetMapping("/info")
    @ApiOperation("管理员信息")
    public Result<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User admin = userService.getById(userId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            throw new BusinessException(403, "无权限");
        }
        admin.setPassword(null);
        return Result.ok(admin);
    }
}
