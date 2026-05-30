package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程分类实体（支持多级树形结构）
 */
@Data
@TableName("course_category")
public class CourseCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类ID，NULL表示一级分类 */
    private Long parentId;

    /** 层级: 1一级/2二级/3三级 */
    private Integer level;

    /** 分类名称 */
    private String name;

    /** 封面 */
    private String cover;

    /** 排序 */
    private Integer sortOrder;

    /** 分类售价（末级分类可设置，用于购买） */
    private BigDecimal price;

    /** 是否免费: 0付费/1免费 */
    private Integer isFree;

    /** 分类描述（购买页展示） */
    private String description;

    /** 状态: 1启用 0禁用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 子分类列表（非DB字段） */
    @TableField(exist = false)
    private List<CourseCategory> children;
}
