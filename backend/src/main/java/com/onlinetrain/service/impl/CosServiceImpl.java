package com.onlinetrain.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.onlinetrain.config.TencentCloudConfig;
import com.onlinetrain.service.CosService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;

@Slf4j
@Service
public class CosServiceImpl implements CosService {

    private final TencentCloudConfig config;
    private COSClient cosClient;
    private String baseUrl;

    public CosServiceImpl(TencentCloudConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        COSCredentials cred = new BasicCOSCredentials(
                config.getCloud().getSecretId(),
                config.getCloud().getSecretKey());
        Region region = new Region(config.getCos().getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);

        // CDN 域名优先，否则用 COS 默认域名
        String cdnDomain = config.getCos().getCdnDomain();
        if (cdnDomain != null && !cdnDomain.isEmpty()) {
            baseUrl = cdnDomain.replaceAll("/+$", "");
        } else {
            baseUrl = String.format("https://%s.cos.%s.myqcloud.com",
                    config.getCos().getBucket(), config.getCos().getRegion());
        }
        log.info("COS client initialized, baseUrl={}", baseUrl);
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return doUpload(file, "images");
    }

    @Override
    public String uploadVideo(MultipartFile file) {
        return doUpload(file, "videos");
    }

    @Override
    public void delete(String key) {
        try {
            cosClient.deleteObject(config.getCos().getBucket(), key);
        } catch (Exception e) {
            log.error("COS delete failed: key={}", key, e);
        }
    }

    private String doUpload(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String datePath = DateUtil.today().replace("-", "/");
        String key = folder + "/" + datePath + "/" + IdUtil.fastSimpleUUID() + ext;

        try (InputStream is = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            String contentType = file.getContentType();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            PutObjectRequest putRequest = new PutObjectRequest(
                    config.getCos().getBucket(), key, is, metadata);
            cosClient.putObject(putRequest);

            log.info("COS upload success: key={}, size={}", key, file.getSize());
            return baseUrl + "/" + key;
        } catch (Exception e) {
            log.error("COS upload failed: {}", originalFilename, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
}
