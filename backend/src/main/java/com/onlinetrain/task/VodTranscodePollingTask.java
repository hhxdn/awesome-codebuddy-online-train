package com.onlinetrain.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.onlinetrain.entity.Chapter;
import com.onlinetrain.service.ChapterService;
import com.onlinetrain.service.VodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * VOD 转码状态轮询任务
 * 
 * 定期查询 VOD 中处于"待转码"或"转码中"状态的视频，
 * 获取转码完成后替换 chapter 表中的播放地址。
 */
@Slf4j
@Component
public class VodTranscodePollingTask {

    private final ChapterService chapterService;
    private final VodService vodService;

    public VodTranscodePollingTask(ChapterService chapterService, VodService vodService) {
        this.chapterService = chapterService;
        this.vodService = vodService;
    }

    /**
     * 每 60 秒轮询一次 VOD 转码状态
     * 首次延迟 30 秒启动，避免和上传操作冲突
     */
    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    public void pollTranscodeStatus() {
        // 查询所有待转码和转码中的章节
        List<Chapter> pendingChapters = chapterService.list(
                new LambdaQueryWrapper<Chapter>()
                        .in(Chapter::getVodTranscodeStatus, "PENDING", "TRANSCODING")
                        .isNotNull(Chapter::getVodFileId)
                        .ne(Chapter::getVodFileId, "")
        );

        if (pendingChapters.isEmpty()) {
            return;  // 无待处理任务，跳过
        }

        log.info("VOD transcode polling: found {} pending chapters", pendingChapters.size());

        for (Chapter chapter : pendingChapters) {
            try {
                Map<String, String> mediaInfo = vodService.describeMediaInfo(chapter.getVodFileId());
                String transcodeStatus = mediaInfo.getOrDefault("transcodeStatus", "PENDING");

                switch (transcodeStatus) {
                    case "DONE": {
                        String transcodeUrl = mediaInfo.get("transcodePlaybackUrl");
                        String duration = mediaInfo.get("duration");

                        LambdaUpdateWrapper<Chapter> updateWrapper = new LambdaUpdateWrapper<Chapter>()
                                .eq(Chapter::getId, chapter.getId())
                                .set(Chapter::getVodTranscodeStatus, "DONE");

                        if (transcodeUrl != null && !transcodeUrl.isEmpty()) {
                            // 用转码后的地址替换 video_url
                            updateWrapper.set(Chapter::getVideoUrl, transcodeUrl);
                        } else {
                            // 没有转码输出URL，使用上传时的即时播放地址
                            updateWrapper.set(Chapter::getVideoUrl, chapter.getVodPlaybackUrl());
                        }

                        if (duration != null && !duration.isEmpty()) {
                            updateWrapper.set(Chapter::getVideoDuration, Integer.parseInt(duration));
                        }

                        chapterService.update(updateWrapper);
                        log.info("VOD transcode DONE: chapterId={}, fileId={}, url={}, duration={}s",
                                chapter.getId(), chapter.getVodFileId(), transcodeUrl, duration);
                        break;
                    }
                    case "TRANSCODING":
                    case "PENDING": {
                        // 标记为转码中以区分首次上传的 PENDING
                        if (!"TRANSCODING".equals(chapter.getVodTranscodeStatus())) {
                            chapterService.update(
                                    new LambdaUpdateWrapper<Chapter>()
                                            .eq(Chapter::getId, chapter.getId())
                                            .set(Chapter::getVodTranscodeStatus, "TRANSCODING")
                            );
                        }
                        log.debug("VOD transcode still processing: chapterId={}, fileId={}",
                                chapter.getId(), chapter.getVodFileId());
                        break;
                    }
                    case "FAILED":
                    default: {
                        chapterService.update(
                                new LambdaUpdateWrapper<Chapter>()
                                        .eq(Chapter::getId, chapter.getId())
                                        .set(Chapter::getVodTranscodeStatus, "FAILED")
                                        .set(Chapter::getVideoUrl, chapter.getVodPlaybackUrl())  // 失败时仍用即时地址
                        );
                        log.warn("VOD transcode FAILED: chapterId={}, fileId={}, using raw playback url",
                                chapter.getId(), chapter.getVodFileId());
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("VOD transcode polling error: chapterId={}, fileId={}",
                        chapter.getId(), chapter.getVodFileId(), e);
            }
        }
    }
}
