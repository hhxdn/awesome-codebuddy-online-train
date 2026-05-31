package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner轮播图实体
 */
@Data
@TableName("banner")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
    private String linkUrl;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 1启用 0禁用 */
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
