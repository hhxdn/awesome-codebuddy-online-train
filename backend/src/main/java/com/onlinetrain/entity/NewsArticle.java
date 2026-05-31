package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻资讯实体
 */
@Data
@TableName("news_article")
public class NewsArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 封面图 */
    private String cover;

    /** 图文内容(HTML) */
    private String content;

    /** 来源 */
    private String source;

    /** 阅读量 */
    private Integer viewCount;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 1发布 0草稿 */
    private Integer status;

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
