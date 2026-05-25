-- Online Train Database Schema
CREATE DATABASE IF NOT EXISTS online_train DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_train;

-- User table
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone` VARCHAR(20) COMMENT '手机号',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像',
    `openid` VARCHAR(100) COMMENT '微信openid',
    `password` VARCHAR(200) COMMENT '密码',
    `role` ENUM('ADMIN','STUDENT') DEFAULT 'STUDENT' COMMENT '角色',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `register_time` DATETIME COMMENT '注册时间',
    `total_study_duration` BIGINT DEFAULT 0 COMMENT '总学习时长(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- Course category table
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `cover` VARCHAR(500) COMMENT '封面',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- Course table
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
    `cover` VARCHAR(500) COMMENT '封面图',
    `description` TEXT COMMENT '课程描述',
    `category_id` BIGINT COMMENT '分类ID',
    `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    `is_free` TINYINT DEFAULT 1 COMMENT '是否免费',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐',
    `status` ENUM('UP','DOWN') DEFAULT 'UP' COMMENT '状态 UP上架 DOWN下架',
    `student_count` INT DEFAULT 0 COMMENT '学习人数',
    `update_status` VARCHAR(50) COMMENT '更新状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- Chapter table
DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
    `video_url` VARCHAR(500) COMMENT '视频地址',
    `video_duration` INT DEFAULT 0 COMMENT '视频时长(秒)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

-- Question table
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id` BIGINT COMMENT '课程ID',
    `chapter_id` BIGINT COMMENT '章节ID',
    `type` ENUM('SINGLE','MULTIPLE','JUDGE','ESSAY') NOT NULL COMMENT '题型',
    `content` TEXT NOT NULL COMMENT '题目内容',
    `score` INT DEFAULT 1 COMMENT '分值',
    `answer` TEXT COMMENT '答案',
    `analysis` TEXT COMMENT '解析',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- Question option table
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `option_label` VARCHAR(10) COMMENT '选项标签 A/B/C/D',
    `content` TEXT COMMENT '选项内容',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确答案'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';

-- Exam paper table
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id` BIGINT COMMENT '课程ID',
    `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
    `duration_minutes` INT DEFAULT 60 COMMENT '考试时长(分钟)',
    `total_score` INT DEFAULT 100 COMMENT '总分',
    `pass_score` INT DEFAULT 60 COMMENT '及格分',
    `max_attempts` INT DEFAULT 1 COMMENT '最大考试次数',
    `status` ENUM('DRAFT','PUBLISHED','ENDED') DEFAULT 'DRAFT' COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- Exam paper question relation
DROP TABLE IF EXISTS `exam_paper_question`;
CREATE TABLE `exam_paper_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- Exam record table
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `exam_paper_id` BIGINT NOT NULL COMMENT '试卷ID',
    `score` DECIMAL(5,1) DEFAULT 0.0 COMMENT '得分',
    `is_pass` TINYINT DEFAULT 0 COMMENT '是否通过',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `submit_time` DATETIME COMMENT '提交时间',
    `status` ENUM('DOING','SUBMITTED') DEFAULT 'DOING' COMMENT '状态',
    `cheat_count` INT DEFAULT 0 COMMENT '作弊次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- Exam answer table
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_answer` TEXT COMMENT '用户答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确',
    `score` INT DEFAULT 0 COMMENT '得分'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试答题表';

-- Learning record table
DROP TABLE IF EXISTS `learning_record`;
CREATE TABLE `learning_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
    `watch_duration` BIGINT DEFAULT 0 COMMENT '观看时长(秒)',
    `watch_percent` DECIMAL(5,2) DEFAULT 0 COMMENT '观看百分比',
    `is_finished` TINYINT DEFAULT 0 COMMENT '是否完成',
    `last_position` BIGINT DEFAULT 0 COMMENT '最后观看位置(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- Wrong question table
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `wrong_count` INT DEFAULT 1 COMMENT '错误次数',
    `last_wrong_time` DATETIME COMMENT '最后错误时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题表';

-- Orders table
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    `pay_method` ENUM('WECHAT','ALIPAY') COMMENT '支付方式',
    `status` ENUM('PENDING','PAID','CANCELLED','REFUNDED') DEFAULT 'PENDING' COMMENT '订单状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME COMMENT '支付时间',
    `expire_time` DATETIME COMMENT '过期时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- Payment log table
DROP TABLE IF EXISTS `payment_log`;
CREATE TABLE `payment_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT COMMENT '订单ID',
    `order_no` VARCHAR(32) COMMENT '订单号',
    `transaction_id` VARCHAR(64) COMMENT '交易流水号',
    `pay_method` VARCHAR(20) COMMENT '支付方式',
    `amount` DECIMAL(10,2) COMMENT '支付金额',
    `status` VARCHAR(20) COMMENT '支付状态',
    `callback_data` TEXT COMMENT '回调数据',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付日志表';

-- 插入默认管理员 admin/admin123 (BCrypt加密)
INSERT INTO `user` (`phone`, `nickname`, `password`, `role`, `status`, `register_time`) VALUES
('admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'ADMIN', 1, NOW());
