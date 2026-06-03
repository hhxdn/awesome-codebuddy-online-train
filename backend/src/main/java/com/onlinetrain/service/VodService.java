package com.onlinetrain.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 腾讯云 VOD 云点播服务
 */
public interface VodService {

    /**
     * 从 COS URL 拉取上传视频到 VOD（保留兼容）
     * @param cosUrl COS 上的视频 URL
     * @param mediaName 媒体名称
     * @return 包含 fileId 和 playbackUrl 的 Map
     */
    Map<String, String> pullUpload(String cosUrl, String mediaName);

    /**
     * 直接上传视频文件到 VOD（ApplyUpload → PUT → CommitUpload）
     * @param file 视频文件
     * @return 包含 fileId 和 playbackUrl 的 Map
     */
    Map<String, String> directUpload(MultipartFile file) throws IOException;

    /**
     * 获取视频播放信息
     * @param fileId VOD 文件ID
     * @return 包含播放URL等信息的 Map
     */
    Map<String, String> getMediaInfo(String fileId);

    /**
     * 获取视频详情（包含转码状态、转码后播放地址、时长等）
     * @param fileId VOD 文件ID
     * @return 包含 transcodeStatus, transcodePlaybackUrl, duration 等字段的 Map
     */
    Map<String, String> describeMediaInfo(String fileId);
}
