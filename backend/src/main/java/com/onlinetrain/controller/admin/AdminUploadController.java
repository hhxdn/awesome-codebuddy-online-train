package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.service.CosService;
import com.onlinetrain.service.VodService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/upload")
@Api(tags = "管理后台-文件上传")
public class AdminUploadController {

    private final CosService cosService;
    private final VodService vodService;

    public AdminUploadController(CosService cosService, VodService vodService) {
        this.cosService = cosService;
        this.vodService = vodService;
    }

    @PostMapping("/image")
    @ApiOperation("上传图片到腾讯云COS")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
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
            log.info("Image uploaded: {} -> {}", file.getOriginalFilename(), url);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("Image upload failed", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/video")
    @ApiOperation("上传视频到腾讯云VOD")
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return Result.error("仅支持上传视频文件");
        }
        try {
            // 直接上传到 VOD（ApplyUpload → PUT → CommitUpload）
            Map<String, String> vodResult = vodService.directUpload(file);

            Map<String, String> data = new HashMap<>();
            data.put("fileId", vodResult.getOrDefault("fileId", ""));
            data.put("playbackUrl", vodResult.getOrDefault("playbackUrl", ""));
            String coverUrl = vodResult.get("coverUrl");
            if (coverUrl != null && !coverUrl.isEmpty()) {
                data.put("coverUrl", coverUrl);
            }
            log.info("Video uploaded to VOD: {} -> fileId={}, coverUrl={}", file.getOriginalFilename(), data.get("fileId"), coverUrl);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("Video upload failed", e);
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }
}
