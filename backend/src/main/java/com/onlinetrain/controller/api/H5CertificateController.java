package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Certificate;
import com.onlinetrain.entity.Course;
import com.onlinetrain.service.CertificateService;
import com.onlinetrain.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * H5-结业证书接口
 */
@RestController
@RequestMapping("/api")
@Api(tags = "H5-结业证书")
public class H5CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CourseService courseService;

    /**
     * 我的证书列表
     */
    @GetMapping("/certificates")
    @ApiOperation("我的证书列表")
    public Result<List<Map<String, Object>>> myCertificates(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<Certificate> certificates = certificateService.lambdaQuery()
                .eq(Certificate::getUserId, userId)
                .eq(Certificate::getStatus, 1)
                .orderByDesc(Certificate::getIssueTime)
                .list();

        List<Map<String, Object>> result = certificates.stream().map(cert -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cert.getId());
            map.put("title", cert.getTitle());
            map.put("content", cert.getContent());
            map.put("certNo", cert.getCertNo());
            map.put("certType", cert.getCertType());
            map.put("issueTime", cert.getIssueTime());

            if (cert.getCourseId() != null) {
                Course course = courseService.getById(cert.getCourseId());
                map.put("courseTitle", course != null ? course.getTitle() : "");
            } else {
                map.put("courseTitle", "全部课程");
            }
            return map;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    /**
     * 证书详情
     */
    @GetMapping("/certificates/{id}")
    @ApiOperation("证书详情")
    public Result<Map<String, Object>> certificateDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Certificate cert = certificateService.getById(id);

        if (cert == null || !cert.getUserId().equals(userId)) {
            return Result.notFound("证书不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", cert.getId());
        result.put("title", cert.getTitle());
        result.put("content", cert.getContent());
        result.put("certNo", cert.getCertNo());
        result.put("certType", cert.getCertType());
        result.put("issueTime", cert.getIssueTime());

        if (cert.getCourseId() != null) {
            Course course = courseService.getById(cert.getCourseId());
            result.put("courseTitle", course != null ? course.getTitle() : "");
        } else {
            result.put("courseTitle", "全部课程");
        }

        return Result.ok(result);
    }
}
