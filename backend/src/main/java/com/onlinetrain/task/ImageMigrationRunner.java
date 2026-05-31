package com.onlinetrain.task;

import com.onlinetrain.entity.Course;
import com.onlinetrain.entity.CourseCategory;
import com.onlinetrain.service.CosService;
import com.onlinetrain.service.CourseService;
import com.onlinetrain.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 一次性任务：将课程和分类的封面图片从外部URL迁移到腾讯云COS
 * 解决国内服务器无法访问 Unsplash 等外网图片的问题
 * 执行完成后可删除此文件
 */
@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "image.migration.enabled", havingValue = "true")
public class ImageMigrationRunner implements ApplicationRunner {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseCategoryService categoryService;

    @Autowired
    private CosService cosService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== 开始图片迁移任务 =====");
        int courseMigrated = 0;
        int categoryMigrated = 0;

        // 1. 迁移课程封面
        List<Course> courses = courseService.list();
        for (Course course : courses) {
            String cover = course.getCover();
            if (cover == null || cover.isEmpty()) {
                continue;
            }
            // 只处理外部URL（非COS域名）
            if (cover.contains("myqcloud.com") || cover.contains("cos.")) {
                log.info("课程[{}]封面已是COS链接，跳过: {}", course.getId(), cover);
                continue;
            }
            try {
                String newUrl = downloadAndUpload(cover, "course_cover");
                course.setCover(newUrl);
                courseService.updateById(course);
                courseMigrated++;
                log.info("课程[{}]封面迁移成功: {} -> {}", course.getId(), cover, newUrl);
            } catch (Exception e) {
                log.warn("课程[{}]封面迁移失败，保留原链接: {} - {}", course.getId(), cover, e.getMessage());
            }
        }

        // 2. 迁移分类封面
        List<CourseCategory> categories = categoryService.list();
        for (CourseCategory cat : categories) {
            String cover = cat.getCover();
            if (cover == null || cover.isEmpty()) {
                continue;
            }
            if (cover.contains("myqcloud.com") || cover.contains("cos.")) {
                log.info("分类[{}]封面已是COS链接，跳过: {}", cat.getId(), cover);
                continue;
            }
            try {
                String newUrl = downloadAndUpload(cover, "category_cover");
                cat.setCover(newUrl);
                categoryService.updateById(cat);
                categoryMigrated++;
                log.info("分类[{}]封面迁移成功: {} -> {}", cat.getId(), cover, newUrl);
            } catch (Exception e) {
                log.warn("分类[{}]封面迁移失败，保留原链接: {} - {}", cat.getId(), cover, e.getMessage());
            }
        }

        log.info("===== 图片迁移任务完成: 课程={} 分类={} =====", courseMigrated, categoryMigrated);
    }

    private String downloadAndUpload(String imageUrl, String prefix) throws Exception {
        // 下载图片
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = conn.getInputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }

        // 构建文件名
        String contentType = conn.getContentType();
        String ext = ".jpg";
        if (contentType != null) {
            if (contentType.contains("png")) ext = ".png";
            else if (contentType.contains("webp")) ext = ".webp";
            else if (contentType.contains("gif")) ext = ".gif";
            else if (contentType.contains("svg")) ext = ".svg";
        }

        byte[] bytes = baos.toByteArray();
        String filename = prefix + "_" + System.currentTimeMillis() + ext;
        String mimeType = contentType != null ? contentType : "image/jpeg";
        MultipartFile file = new InMemoryMultipartFile("file", filename, mimeType, bytes);

        // 上传到COS
        String cosUrl = cosService.uploadImage(file);
        log.info("上传COS成功: {}", cosUrl);
        return cosUrl;
    }

    /**
     * 内存中的 MultipartFile 实现，避免引入 spring-test 依赖
     */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return originalFilename; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(File dest) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
