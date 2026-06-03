package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节实体
 */
@Data
@TableName("chapter")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 章节标题 */
    private String title;

    /** 视频地址 */
    private String videoUrl;

    /** VOD文件ID */
    private String vodFileId;

    /** VOD上传后即时播放地址(转码前) */
    private String vodPlaybackUrl;

    /** VOD转码状态: PENDING/TRANSCODING/DONE/FAILED */
    private String vodTranscodeStatus;

    /** 视频时长(秒) */
    private Integer videoDuration;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
