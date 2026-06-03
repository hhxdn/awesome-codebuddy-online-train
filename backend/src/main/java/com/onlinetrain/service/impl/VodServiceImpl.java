package com.onlinetrain.service.impl;

import com.onlinetrain.config.TencentCloudConfig;
import com.onlinetrain.service.VodService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class VodServiceImpl implements VodService {

    private final TencentCloudConfig config;
    private final VodClient vodClient;

    public VodServiceImpl(TencentCloudConfig config) {
        this.config = config;
        // 使用官方 SDK 创建 VodClient，SDK 自动处理 TC3 签名
        Credential cred = new Credential(config.getCloud().getSecretId(), config.getCloud().getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vod.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        this.vodClient = new VodClient(cred, config.getVod().getRegion(), clientProfile);
    }

    @Override
    public Map<String, String> pullUpload(String cosUrl, String mediaName) {
        try {
            PullUploadRequest req = new PullUploadRequest();
            req.setMediaUrl(cosUrl);
            req.setMediaName(mediaName);
            if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
                req.setSubAppId(config.getVod().getSubAppId().longValue());
            }

            PullUploadResponse resp = vodClient.PullUpload(req);
            String taskId = resp.getTaskId();
            log.info("VOD PullUpload success: taskId={}, mediaName={}", taskId, mediaName);

            // PullUpload 是异步的，只返回 TaskId，FileId 需要通过任务回调获取
            Map<String, String> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("fileId", taskId);  // 暂时用 taskId 作为标识
            result.put("playbackUrl", "");  // 转码完成后才有播放地址
            result.put("note", "视频已提交转码，稍后可通过 DescribeTaskDetail 查询 FileId");
            return result;
        } catch (TencentCloudSDKException e) {
            log.error("VOD PullUpload error: mediaName={}", mediaName, e);
            throw new RuntimeException("VOD上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> directUpload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String mediaName = originalFilename;
        String mediaType = "mp4";
        if (originalFilename != null && originalFilename.contains(".")) {
            mediaName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ext.isEmpty()) {
                mediaType = ext;
            }
        }

        try {
            // 1. ApplyUpload - 申请上传，获取临时凭证和存储信息
            ApplyUploadRequest applyReq = new ApplyUploadRequest();
            applyReq.setMediaType(mediaType);
            applyReq.setMediaName(originalFilename);
            if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
                applyReq.setSubAppId(config.getVod().getSubAppId().longValue());
            }

            ApplyUploadResponse applyResp = vodClient.ApplyUpload(applyReq);
            TempCertificate tempCert = applyResp.getTempCertificate();
            String storageBucket = applyResp.getStorageBucket();
            String storageRegion = applyResp.getStorageRegion();
            String mediaStoragePath = applyResp.getMediaStoragePath();
            String vodSessionKey = applyResp.getVodSessionKey();
            log.info("VOD ApplyUpload success: mediaName={}, bucket={}, region={}, path={}",
                    mediaName, storageBucket, storageRegion, mediaStoragePath);

            // 2. 使用临时凭证上传文件到 VOD 存储（COS）
            BasicSessionCredentials cosCred = new BasicSessionCredentials(
                    tempCert.getSecretId(),
                    tempCert.getSecretKey(),
                    tempCert.getToken()
            );
            ClientConfig cosConfig = new ClientConfig(new Region(storageRegion));
            COSClient cosClient = new COSClient(cosCred, cosConfig);

            byte[] fileBytes = file.getBytes();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileBytes.length);
            metadata.setContentType("video/" + mediaType);

            PutObjectRequest putReq = new PutObjectRequest(
                    storageBucket,
                    mediaStoragePath,
                    new ByteArrayInputStream(fileBytes),
                    metadata
            );
            cosClient.putObject(putReq);
            cosClient.shutdown();
            log.info("VOD file upload to COS success: path={}", mediaStoragePath);

            // 3. CommitUpload - 确认上传，获取 FileId
            CommitUploadRequest commitReq = new CommitUploadRequest();
            commitReq.setVodSessionKey(vodSessionKey);
            if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
                commitReq.setSubAppId(config.getVod().getSubAppId().longValue());
            }

            CommitUploadResponse commitResp = vodClient.CommitUpload(commitReq);
            String fileId = commitResp.getFileId();
            String mediaUrl = commitResp.getMediaUrl();
            log.info("VOD CommitUpload success: fileId={}, mediaUrl={}", fileId, mediaUrl);

            Map<String, String> result = new HashMap<>();
            result.put("fileId", fileId);
            if (fileId != null) {
                result.put("playbackUrl", "https://" + fileId + ".vod2.myqcloud.com");
            }
            if (mediaUrl != null) {
                result.put("mediaUrl", mediaUrl);
            }
            return result;
        } catch (TencentCloudSDKException e) {
            log.error("VOD directUpload error: {}", e.toString());
            throw new RuntimeException("VOD上传失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("VOD directUpload COS upload error", e);
            throw new RuntimeException("VOD上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getMediaInfo(String fileId) {
        try {
            DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
            req.setFileIds(new String[]{fileId});
            if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
                req.setSubAppId(config.getVod().getSubAppId().longValue());
            }

            DescribeMediaInfosResponse resp = vodClient.DescribeMediaInfos(req);
            MediaInfo[] mediaInfoSet = resp.getMediaInfoSet();

            Map<String, String> result = new HashMap<>();
            if (mediaInfoSet != null && mediaInfoSet.length > 0) {
                MediaInfo info = mediaInfoSet[0];
                result.put("fileId", info.getFileId());
                if (info.getBasicInfo() != null) {
                    result.put("name", info.getBasicInfo().getName());
                }
            }
            return result;
        } catch (TencentCloudSDKException e) {
            log.error("VOD DescribeMediaInfos error: fileId={}", fileId, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, String> describeMediaInfo(String fileId) {
        Map<String, String> result = new HashMap<>();
        result.put("fileId", fileId);
        result.put("transcodeStatus", "PENDING");

        try {
            DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
            req.setFileIds(new String[]{fileId});
            if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
                req.setSubAppId(config.getVod().getSubAppId().longValue());
            }

            DescribeMediaInfosResponse resp = vodClient.DescribeMediaInfos(req);
            MediaInfo[] mediaInfoSet = resp.getMediaInfoSet();

            if (mediaInfoSet == null || mediaInfoSet.length == 0) {
                log.warn("VOD media not found: fileId={}", fileId);
                result.put("transcodeStatus", "FAILED");
                return result;
            }

            MediaInfo info = mediaInfoSet[0];

            // 基本信息
            if (info.getBasicInfo() != null) {
                result.put("name", info.getBasicInfo().getName());
                result.put("status", info.getBasicInfo().getStatus());
                // VOD 媒体状态: NORMAL=正常, UPLOADING=上传中, CHECKING=审核中, BLOCKED=已封禁
            }

            // 元数据（时长等）
            if (info.getMetaData() != null) {
                if (info.getMetaData().getDuration() != null) {
                    result.put("duration", String.valueOf(Math.round(info.getMetaData().getDuration())));
                }
            }

            // 转码信息
            MediaTranscodeInfo transcodeInfo = info.getTranscodeInfo();
            if (transcodeInfo != null && transcodeInfo.getTranscodeSet() != null
                    && transcodeInfo.getTranscodeSet().length > 0) {
                MediaTranscodeItem[] items = transcodeInfo.getTranscodeSet();
                // 查找转码完成的输出（优先选标清/高清的 mp4 输出）
                for (MediaTranscodeItem item : items) {
                    if (item.getUrl() != null && !item.getUrl().isEmpty()) {
                        result.put("transcodeStatus", "DONE");
                        result.put("transcodePlaybackUrl", item.getUrl());
                        // 优先用 hls 自适应码流
                        if (item.getUrl().endsWith(".m3u8")) {
                            break;  // HLS 优先
                        }
                    }
                }
                log.info("VOD transcode info: fileId={}, status={}, url={}",
                        fileId, result.get("transcodeStatus"), result.get("transcodePlaybackUrl"));
            } else {
                log.info("VOD transcode not yet available: fileId={}, basicStatus={}",
                        fileId, result.get("status"));
            }

            return result;
        } catch (TencentCloudSDKException e) {
            log.error("VOD DescribeMediaInfos error: fileId={}", fileId, e);
            result.put("transcodeStatus", "FAILED");
            return result;
        }
    }
}
