package com.onlinetrain.service;

import java.util.Map;

/**
 * 腾讯云 VOD 云点播服务
 */
public interface VodService {

    /**
     * 从 COS URL 拉取上传视频到 VOD
     * @param cosUrl COS 上的视频 URL
     * @param mediaName 媒体名称
     * @return 包含 fileId 和 playbackUrl 的 Map
     */
    Map<String, String> pullUpload(String cosUrl, String mediaName);

    /**
     * 获取视频播放信息
     * @param fileId VOD 文件ID
     * @return 包含播放URL等信息的 Map
     */
    Map<String, String> getMediaInfo(String fileId);
}
