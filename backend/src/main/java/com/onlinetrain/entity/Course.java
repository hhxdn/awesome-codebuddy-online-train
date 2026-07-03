package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程标题 */
    private String title;

    /** 封面图 */
    private String cover;

    /** 课程描述 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 课程类型: ONLINE线上 / OFFLINE线下 */
    private String courseType;

    /** 价格 */
    private BigDecimal price;

    /** 是否免费 */
    private Integer isFree;

    /** 排序 */
    private Integer sortOrder;

    /** 是否推荐 */
    private Integer isRecommend;

    /** 状态: UP上架/DOWN下架 */
    private String status;

    /** 学习人数 */
    private Integer studentCount;

    /** 更新状态 */
    private String updateStatus;

    /** 经度(线下课程打卡位置) */
    private BigDecimal longitude;

    /** 纬度(线下课程打卡位置) */
    private BigDecimal latitude;

    /** 打卡半径(米)，默认3000 */
    private Integer checkinRadius;

    /** 前置线上课程ID，学完才能打卡此线下课程 */
    private Long prerequisiteCourseId;

    /** 免费章节数（付费课程可设置前N节免费试看，0或null表示不免费） */
    private Integer freeChapterCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
