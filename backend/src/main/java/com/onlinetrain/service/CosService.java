package com.onlinetrain.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 腾讯云 COS 对象存储服务
 */
public interface CosService {

    /**
     * 上传图片到 COS
     * @param file 图片文件
     * @return 可访问的图片 URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 上传视频到 COS（作为 VOD PullUpload 的源）
     * @param file 视频文件
     * @return COS URL
     */
    String uploadVideo(MultipartFile file);

    /**
     * 删除 COS 上的文件
     * @param key COS 对象 Key
     */
    void delete(String key);
}
