package com.onlinetrain.controller;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.QaQuestion;
import com.onlinetrain.service.CosService;
import com.onlinetrain.service.QaQuestionService;
import com.onlinetrain.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qa")
@Api(tags = "学员端-答疑解惑")
public class QaController {

    @Autowired
    private QaQuestionService qaQuestionService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CosService cosService;

    @PostMapping("/upload-image")
    @ApiOperation("上传答疑图片")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持上传图片文件");
        }
        try {
            String url = cosService.uploadImage(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    @ApiOperation("提交答疑")
    public Result<?> submit(@RequestBody QaQuestion qa, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                Long userId = jwtUtils.getUserId(token.substring(7));
                qa.setUserId(userId);
            } catch (Exception ignored) {
            }
        }
        qa.setStatus("PENDING");
        qaQuestionService.save(qa);
        return Result.ok("提交成功，我们将尽快回复您", null);
    }
}
