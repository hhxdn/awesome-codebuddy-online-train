package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.LearningRecord;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.LearningRecordService;
import com.onlinetrain.service.UserService;
import com.onlinetrain.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * H5用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "H5-用户接口")
public class H5UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 手机号+验证码登录
     */
    @PostMapping("/login")
    @ApiOperation("手机号验证码登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");

        if (phone == null || phone.isEmpty()) {
            return Result.error("手机号不能为空");
        }
        // 模拟验证码 123456
        if (!"123456".equals(code)) {
            return Result.error("验证码错误");
        }

        // 查找或创建用户
        User user = userService.lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(Math.max(0, phone.length() - 4)));
            user.setRole("STUDENT");
            user.setStatus(1);
            user.setRegisterTime(LocalDateTime.now());
            userService.save(user);
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtils.createToken(user.getId(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    /**
     * 微信登录（模拟）
     */
    @PostMapping("/wx-login")
    @ApiOperation("微信登录")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> params) {
        String openid = params.getOrDefault("openid", "mock_openid_" + System.currentTimeMillis());
        String nickname = params.getOrDefault("nickname", "微信用户");
        String avatar = params.getOrDefault("avatar", "");

        User user = userService.lambdaQuery().eq(User::getOpenid, openid).one();
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setRole("STUDENT");
            user.setStatus(1);
            user.setRegisterTime(LocalDateTime.now());
            userService.save(user);
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtils.createToken(user.getId(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    @ApiOperation("更新个人资料")
    public Result<User> updateProfile(@RequestBody User updateUser, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
        }
        userService.updateById(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 学习数据统计
     */
    @GetMapping("/study-data")
    @ApiOperation("学习数据统计")
    public Result<Map<String, Object>> studyData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);

        // 统计学习记录数
        long totalRecords = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .count();
        long finishedRecords = learningRecordService.lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getIsFinished, 1)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudyDuration", user != null ? user.getTotalStudyDuration() : 0);
        data.put("totalRecords", totalRecords);
        data.put("finishedRecords", finishedRecords);

        return Result.ok(data);
    }
}
