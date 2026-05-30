package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 结业证书实体
 */
@Data
@TableName("certificate")
public class Certificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 课程ID(单个课程结业)，null表示全课程结业 */
    private Long courseId;

    /** 关联的考试记录ID（线下考试通过后颁发） */
    private Long examRecordId;

    /** 证书类型: COURSE单课程 / ALL全课程结业 */
    private String certType;

    /** 证书标题 */
    private String title;

    /** 证书内容描述 */
    private String content;

    /** 证书编号 */
    private String certNo;

    /** 颁发时间 */
    private LocalDateTime issueTime;

    /** 状态 1有效 0撤销 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
