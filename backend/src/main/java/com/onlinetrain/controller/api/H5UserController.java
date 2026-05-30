package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.*;
import com.onlinetrain.service.*;
import com.onlinetrain.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private OrderService orderService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 手机号+密码登录
     */
    @PostMapping("/login")
    @ApiOperation("手机号密码登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");

        if (phone == null || phone.isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        User user = userService.lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            return Result.error("用户不存在，请先注册");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtils.createToken(user.getId(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        result.put("approvalStatus", user.getApprovalStatus() != null ? user.getApprovalStatus() : "APPROVED");
        return Result.ok(result);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        String confirmPassword = params.get("confirmPassword");

        if (phone == null || phone.isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (password.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        if (!password.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }

        User existUser = userService.lambdaQuery().eq(User::getPhone, phone).one();
        if (existUser != null) {
            return Result.error("该手机号已注册，请直接登录");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname("用户" + phone.substring(Math.max(0, phone.length() - 4)));
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setApprovalStatus("PENDING");
        user.setRegisterTime(LocalDateTime.now());
        userService.save(user);

        String token = jwtUtils.createToken(user.getId(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        result.put("approvalStatus", "PENDING");
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
        if (updateUser.getNickname() != null) user.setNickname(updateUser.getNickname());
        if (updateUser.getAvatar() != null) user.setAvatar(updateUser.getAvatar());
        userService.updateById(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 学习数据统计（同时支持 /study-data 和 /stats）
     */
    @GetMapping({"/study-data", "/stats"})
    @ApiOperation("学习数据统计")
    public Result<Map<String, Object>> studyData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);

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

    /**
     * 我的课程列表（已购买或免费的）
     */
    @GetMapping("/courses")
    @ApiOperation("我的课程")
    public Result<List<Map<String, Object>>> myCourses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 查询已支付的订单
        List<Order> paidOrders = orderService.lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, "PAID")
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> addedCourseIds = new HashSet<>();

        for (Order order : paidOrders) {
            if (addedCourseIds.contains(order.getCourseId())) continue;
            Course course = courseService.getById(order.getCourseId());
            if (course != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", course.getId());
                item.put("title", course.getTitle());
                item.put("coverUrl", course.getCover());
                item.put("price", course.getPrice());

                // 学习进度
                long finishedChapters = learningRecordService.lambdaQuery()
                        .eq(LearningRecord::getUserId, userId)
                        .eq(LearningRecord::getCourseId, course.getId())
                        .eq(LearningRecord::getIsFinished, 1)
                        .count();
                item.put("finishedChapters", finishedChapters);

                result.add(item);
                addedCourseIds.add(order.getCourseId());
            }
        }

        // 也包含免费课程
        List<Course> freeCourses = courseService.lambdaQuery()
                .and(w -> w.isNull(Course::getPrice).or().eq(Course::getPrice, java.math.BigDecimal.ZERO))
                .eq(Course::getStatus, "UP")
                .list();
        for (Course course : freeCourses) {
            if (addedCourseIds.contains(course.getId())) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", course.getId());
            item.put("title", course.getTitle());
            item.put("coverUrl", course.getCover());
            item.put("price", course.getPrice());
            item.put("finishedChapters", 0);
            result.add(item);
            addedCourseIds.add(course.getId());
        }

        return Result.ok(result);
    }

    /**
     * 提交用户资料（注册后的信息补充）
     */
    @PostMapping("/submit-profile")
    @ApiOperation("提交用户资料")
    public Result<String> submitProfile(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }

        String realName = params.get("realName");
        String gender = params.get("gender");
        String ageStr = params.get("age");
        String education = params.get("education");
        String major = params.get("major");
        String contactPhone = params.get("contactPhone") != null ? params.get("contactPhone") : params.get("phone");

        if (realName == null || realName.isEmpty()) {
            return Result.error("姓名不能为空");
        }
        if (gender == null || gender.isEmpty()) {
            return Result.error("性别不能为空");
        }

        user.setRealName(realName);
        user.setGender(gender);
        user.setEducation(education);
        user.setMajor(major);
        user.setPhone(contactPhone);
        if (ageStr != null && !ageStr.isEmpty()) {
            try { user.setAge(Integer.parseInt(ageStr)); } catch (NumberFormatException ignored) {}
        }
        user.setApprovalStatus("PENDING");
        userService.updateById(user);

        return Result.ok("提交成功");
    }

    /**
     * 检查用户审核状态
     */
    @GetMapping("/check-status")
    @ApiOperation("检查审核状态")
    public Result<Map<String, Object>> checkStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("approvalStatus", user.getApprovalStatus() != null ? user.getApprovalStatus() : "APPROVED");
        data.put("hasProfile", user.getRealName() != null && !user.getRealName().isEmpty());
        return Result.ok(data);
    }
}
