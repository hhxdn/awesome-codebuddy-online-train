-- 用户表新增审核和资料字段
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `approval_status` ENUM('APPROVED','PENDING','REJECTED') DEFAULT 'APPROVED' COMMENT '审核状态: APPROVED已通过 PENDING待审核 REJECTED已拒绝' AFTER `status`,
    ADD COLUMN IF NOT EXISTS `real_name` VARCHAR(50) COMMENT '真实姓名' AFTER `approval_status`,
    ADD COLUMN IF NOT EXISTS `gender` VARCHAR(10) COMMENT '性别 男/女' AFTER `real_name`,
    ADD COLUMN IF NOT EXISTS `age` INT COMMENT '年龄' AFTER `gender`,
    ADD COLUMN IF NOT EXISTS `education` VARCHAR(50) COMMENT '学历' AFTER `age`,
    ADD COLUMN IF NOT EXISTS `major` VARCHAR(100) COMMENT '专业' AFTER `education`;

-- 将现有学员的审核状态设为已通过
UPDATE `user` SET `approval_status` = 'APPROVED' WHERE `role` = 'STUDENT' AND `approval_status` IS NULL;

-- 课程表新增课程类型、线下打卡、前置课程字段
ALTER TABLE `course`
    ADD COLUMN IF NOT EXISTS `course_type` ENUM('ONLINE','OFFLINE') DEFAULT 'ONLINE' COMMENT '课程类型: ONLINE线上 OFFLINE线下' AFTER `category_id`,
    ADD COLUMN IF NOT EXISTS `longitude` DECIMAL(11,8) COMMENT '经度(线下课程打卡位置)' AFTER `update_status`,
    ADD COLUMN IF NOT EXISTS `latitude` DECIMAL(10,8) COMMENT '纬度(线下课程打卡位置)' AFTER `longitude`,
    ADD COLUMN IF NOT EXISTS `checkin_radius` INT DEFAULT 3000 COMMENT '打卡半径(米)，默认3000' AFTER `latitude`,
    ADD COLUMN IF NOT EXISTS `prerequisite_course_id` BIGINT COMMENT '前置线上课程ID，学完才能打卡此线下课程' AFTER `checkin_radius`;

-- 将现有课程设为线上课程
UPDATE `course` SET `course_type` = 'ONLINE' WHERE `course_type` IS NULL;

-- 线下课程打卡记录表
CREATE TABLE IF NOT EXISTS `offline_checkin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '线下课程ID',
    `checkin_longitude` DECIMAL(11,8) COMMENT '打卡时经度',
    `checkin_latitude` DECIMAL(10,8) COMMENT '打卡时纬度',
    `distance` INT COMMENT '打卡距离(米)',
    `checkin_type` ENUM('SELF','ADMIN') DEFAULT 'SELF' COMMENT '打卡方式: SELF自主打卡 ADMIN后台代打卡',
    `operator_id` BIGINT COMMENT '操作人ID(后台代打卡时记录)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1有效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下课程打卡记录表';

-- 结业证书表
CREATE TABLE IF NOT EXISTS `certificate` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT COMMENT '课程ID(单个课程结业)',
    `exam_record_id` BIGINT COMMENT '关联的线下考试记录ID(线下考试通过后颁发)',
    `cert_type` ENUM('COURSE','ALL') DEFAULT 'COURSE' COMMENT '证书类型: COURSE单课程 ALL全课程结业',
    `title` VARCHAR(200) NOT NULL COMMENT '证书标题',
    `content` TEXT COMMENT '证书内容描述',
    `cert_no` VARCHAR(50) NOT NULL COMMENT '证书编号',
    `issue_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1有效 0撤销',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY `uk_cert_no` (`cert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结业证书表';

-- 试卷表新增考试类型字段
ALTER TABLE `exam_paper`
    ADD COLUMN IF NOT EXISTS `exam_type` ENUM('ONLINE','OFFLINE') DEFAULT 'ONLINE' COMMENT '考试类型: ONLINE线上考试 OFFLINE线下考试' AFTER `status`;

-- 将现有试卷设为线上考试
UPDATE `exam_paper` SET `exam_type` = 'ONLINE' WHERE `exam_type` IS NULL;

-- 证书表新增考试记录关联字段
ALTER TABLE `certificate`
    ADD COLUMN IF NOT EXISTS `exam_record_id` BIGINT COMMENT '关联的线下考试记录ID(线下考试通过后颁发)' AFTER `course_id`;
