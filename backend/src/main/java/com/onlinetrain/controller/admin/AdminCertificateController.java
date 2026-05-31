package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Certificate;
import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.CertificateService;
import com.onlinetrain.service.CosService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端-结业证书管理
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-结业证书管理")
public class AdminCertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CosService cosService;

    /**
     * 证书列表
     */
    @GetMapping("/certificates")
    @ApiOperation("证书列表")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String certType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Certificate> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Certificate> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (userId != null) wrapper.eq(Certificate::getUserId, userId);
        if (courseId != null) wrapper.eq(Certificate::getCourseId, courseId);
        if (certType != null && !certType.isEmpty()) wrapper.eq(Certificate::getCertType, certType);
        wrapper.orderByDesc(Certificate::getCreateTime);

        // 按学员姓名/手机号搜索
        if (keyword != null && !keyword.isEmpty()) {
            List<User> matchedUsers = userService.lambdaQuery()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .list();
            if (!matchedUsers.isEmpty()) {
                List<Long> userIds = matchedUsers.stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(Certificate::getUserId, userIds);
            } else {
                wrapper.eq(Certificate::getUserId, -1L); // 没有匹配的用户，返回空
            }
        }

        Page<Certificate> result = certificateService.page(pageParam, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(cert -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cert.getId());
            map.put("userId", cert.getUserId());
            map.put("courseId", cert.getCourseId());
            map.put("certType", cert.getCertType());
            map.put("title", cert.getTitle());
            map.put("content", cert.getContent());
            map.put("attachmentUrl", cert.getAttachmentUrl());
            map.put("certNo", cert.getCertNo());
            map.put("issueTime", cert.getIssueTime());
            map.put("status", cert.getStatus());

            User user = userService.getById(cert.getUserId());
            map.put("userName", user != null ? (user.getRealName() != null ? user.getRealName() : user.getNickname()) : "");
            map.put("userPhone", user != null ? user.getPhone() : "");

            if (cert.getCourseId() != null) {
                Course course = courseService.getById(cert.getCourseId());
                map.put("courseTitle", course != null ? course.getTitle() : "");
            } else {
                map.put("courseTitle", "全部课程");
            }
            return map;
        }).collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());

        return Result.ok(pageResult);
    }

    /**
     * 颁发结业证书
     */
    @PostMapping("/certificates")
    @ApiOperation("颁发结业证书")
    public Result<Map<String, Object>> issue(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String certType = params.get("certType") != null ? params.get("certType").toString() : "COURSE";
        Long courseId = params.containsKey("courseId") && params.get("courseId") != null
                ? Long.valueOf(params.get("courseId").toString()) : null;

        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("学员不存在");
        }

        String title;
        String content;
        if ("ALL".equals(certType)) {
            title = "全部课程结业证书";
            content = "兹证明 " + user.getRealName() + "（" + user.getPhone() + "）已完成全部课程学习，成绩合格，准予结业。";
        } else {
            Course course = courseService.getById(courseId);
            String courseName = course != null ? course.getTitle() : "课程";
            title = courseName + " - 结业证书";
            content = "兹证明 " + user.getRealName() + "（" + user.getPhone() + "）已完成「" + courseName + "」课程学习，成绩合格，准予结业。";
        }

        // 生成证书编号
        String certNo = "CERT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + userId;

        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setCertType(certType);
        certificate.setTitle(title);
        certificate.setContent(content);
        certificate.setCertNo(certNo);
        certificate.setIssueTime(LocalDateTime.now());
        certificate.setStatus(1);
        certificateService.save(certificate);

        Map<String, Object> result = new HashMap<>();
        result.put("id", certificate.getId());
        result.put("certNo", certNo);
        result.put("title", title);
        return Result.ok("证书颁发成功", result);
    }

    /**
     * 撤销证书
     */
    @PutMapping("/certificates/{id}/revoke")
    @ApiOperation("撤销证书")
    public Result<Void> revoke(@PathVariable Long id) {
        Certificate cert = certificateService.getById(id);
        if (cert == null) {
            return Result.notFound("证书不存在");
        }
        cert.setStatus(0);
        certificateService.updateById(cert);
        return Result.ok();
    }

    /**
     * 删除证书
     */
    @DeleteMapping("/certificates/{id}")
    @ApiOperation("删除证书")
    public Result<Void> delete(@PathVariable Long id) {
        certificateService.removeById(id);
        return Result.ok();
    }

    /**
     * 上传证书附件（Word/PDF/图片）
     */
    @PostMapping("/certificates/{id}/attachment")
    @ApiOperation("上传证书附件")
    public Result<Map<String, String>> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Certificate cert = certificateService.getById(id);
        if (cert == null) {
            return Result.notFound("证书不存在");
        }
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return Result.error("无法识别文件类型");
        }
        String lowerName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean valid = contentType.startsWith("image/")
                || contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || lowerName.endsWith(".doc")
                || lowerName.endsWith(".docx")
                || lowerName.endsWith(".pdf");
        if (!valid) {
            return Result.error("仅支持上传图片、Word、PDF格式文件");
        }
        try {
            String url = cosService.uploadImage(file);
            cert.setAttachmentUrl(url);
            certificateService.updateById(cert);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.ok("附件上传成功", data);
        } catch (Exception e) {
            return Result.error("附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除证书附件
     */
    @DeleteMapping("/certificates/{id}/attachment")
    @ApiOperation("删除证书附件")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        Certificate cert = certificateService.getById(id);
        if (cert == null) {
            return Result.notFound("证书不存在");
        }
        cert.setAttachmentUrl(null);
        certificateService.updateById(cert);
        return Result.ok("附件已删除");
    }
}
