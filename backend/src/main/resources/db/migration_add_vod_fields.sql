-- 为 chapter 表增加 VOD 相关字段
-- 执行此 SQL 前请确认数据库已备份

ALTER TABLE `chapter`
ADD COLUMN `vod_file_id` VARCHAR(100) COMMENT 'VOD文件ID' AFTER `video_url`,
ADD COLUMN `vod_playback_url` VARCHAR(500) COMMENT 'VOD上传后即时播放地址(转码前)' AFTER `vod_file_id`,
ADD COLUMN `vod_transcode_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'VOD转码状态: PENDING待转码/TRANSCODING转码中/DONE已完成/FAILED失败' AFTER `vod_playback_url`;

-- 为优化轮询查询性能加索引
ALTER TABLE `chapter` ADD INDEX `idx_vod_status` (`vod_transcode_status`);
