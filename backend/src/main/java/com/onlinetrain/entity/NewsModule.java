package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("news_module")
public class NewsModule {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模块名称 */
    private String name;

    /** 模块类型标识 */
    private String type;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 1启用 0禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
