package com.onlinetrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 微信openid */
    private String openid;

    /** 密码 */
    private String password;

    /** 角色: ADMIN/STUDENT */
    private String role;

    /** 状态: 1正常 0禁用 */
    private Integer status;

    /** 审核状态: APPROVED/PENDING/REJECTED */
    private String approvalStatus;

    /** 真实姓名 */
    private String realName;

    /** 性别 */
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 学历 */
    private String education;

    /** 专业 */
    private String major;

    /** 注册时间 */
    private LocalDateTime registerTime;

    /** 总学习时长(秒) */
    private Long totalStudyDuration;

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
