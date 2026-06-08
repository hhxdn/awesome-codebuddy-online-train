-- 修复 video_duration 默认值问题
-- 背景：之前 DEFAULT 0 导致所有未设置时长的章节显示"0秒"
-- 修改为 DEFAULT NULL，让后端可以用 null 判断是否显示"视频"

-- 1. 将已有数据中 video_duration=0 的改为 NULL（保留真正为 0 的边界情况极罕见，一并处理）
UPDATE `chapter` SET `video_duration` = NULL WHERE `video_duration` = 0;

-- 2. 修改列默认值
ALTER TABLE `chapter` MODIFY COLUMN `video_duration` INT NULL DEFAULT NULL COMMENT '视频时长(秒)';
