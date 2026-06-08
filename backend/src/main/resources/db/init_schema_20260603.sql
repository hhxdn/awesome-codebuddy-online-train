-- MySQL dump 10.13  Distrib 5.7.31, for macos10.14 (x86_64)
--
-- Host: 14.103.222.243    Database: online_train
-- ------------------------------------------------------
-- Server version	5.7.44-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='Banner轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `certificate`
--

DROP TABLE IF EXISTS `certificate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `certificate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) DEFAULT NULL COMMENT '课程ID',
  `exam_record_id` bigint(20) DEFAULT NULL COMMENT '关联的线下考试记录ID',
  `cert_type` enum('COURSE','ALL') DEFAULT 'COURSE' COMMENT '证书类型',
  `title` varchar(200) NOT NULL COMMENT '证书标题',
  `content` text COMMENT '证书内容描述',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '证书附件URL(Word/PDF/图片)',
  `cert_no` varchar(50) NOT NULL COMMENT '证书编号',
  `issue_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_no` (`cert_no`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='结业证书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chapter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `title` varchar(200) NOT NULL COMMENT '章节标题',
  `video_url` varchar(500) DEFAULT NULL COMMENT '视频地址',
  `vod_file_id` varchar(100) DEFAULT NULL COMMENT 'VOD文件ID',
  `vod_playback_url` varchar(500) DEFAULT NULL COMMENT 'VOD上传后即时播放地址(转码前)',
  `vod_transcode_status` varchar(20) DEFAULT 'PENDING' COMMENT 'VOD转码状态: PENDING/TRANSCODING/DONE/FAILED',
  `video_duration` int(11) DEFAULT NULL COMMENT '视频时长(秒)',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COMMENT='章节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '课程标题',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图',
  `description` text COMMENT '课程描述',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `course_type` enum('ONLINE','OFFLINE') DEFAULT 'ONLINE' COMMENT '课程类型',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `is_free` tinyint(4) DEFAULT '1' COMMENT '是否免费',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `is_recommend` tinyint(4) DEFAULT '0' COMMENT '是否推荐',
  `status` enum('UP','DOWN') DEFAULT 'UP' COMMENT '状态 UP上架 DOWN下架',
  `student_count` int(11) DEFAULT '0' COMMENT '学习人数',
  `update_status` varchar(50) DEFAULT NULL COMMENT '更新状态',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度',
  `checkin_radius` int(11) DEFAULT '3000' COMMENT '打卡半径',
  `prerequisite_course_id` bigint(20) DEFAULT NULL COMMENT '前置课程',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COMMENT='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_category`
--

DROP TABLE IF EXISTS `course_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父分类ID',
  `level` int(11) DEFAULT '1' COMMENT '层级:1一级/2二级/3三级',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `price` decimal(10,2) DEFAULT NULL COMMENT '分类售价',
  `is_free` int(11) DEFAULT '0' COMMENT '是否免费:0付费/1免费',
  `description` text COMMENT '分类描述',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_reservation`
--

DROP TABLE IF EXISTS `course_reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course_reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL COMMENT '线下课程ID',
  `user_id` bigint(20) NOT NULL COMMENT '学员ID',
  `reservation_time` datetime DEFAULT NULL COMMENT '预约课程时间',
  `status` enum('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING' COMMENT '预约状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='线下课程预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_answer`
--

DROP TABLE IF EXISTS `exam_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exam_answer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exam_record_id` bigint(20) NOT NULL COMMENT '考试记录ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `user_answer` text COMMENT '用户答案',
  `is_correct` tinyint(4) DEFAULT '0' COMMENT '是否正确',
  `score` int(11) DEFAULT '0' COMMENT '得分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8mb4 COMMENT='考试答题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_paper`
--

DROP TABLE IF EXISTS `exam_paper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exam_paper` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) DEFAULT NULL COMMENT '课程ID',
  `title` varchar(200) NOT NULL COMMENT '试卷标题',
  `duration_minutes` int(11) DEFAULT '60' COMMENT '考试时长(分钟)',
  `total_score` int(11) DEFAULT '100' COMMENT '总分',
  `pass_score` int(11) DEFAULT '60' COMMENT '及格分',
  `max_attempts` int(11) DEFAULT '1' COMMENT '最大考试次数',
  `status` enum('DRAFT','PUBLISHED','ENDED') DEFAULT 'DRAFT' COMMENT '状态',
  `exam_type` enum('ONLINE','OFFLINE') DEFAULT 'ONLINE' COMMENT '考试类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_paper_question`
--

DROP TABLE IF EXISTS `exam_paper_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exam_paper_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exam_paper_id` bigint(20) NOT NULL COMMENT '试卷ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_record`
--

DROP TABLE IF EXISTS `exam_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exam_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `exam_paper_id` bigint(20) NOT NULL COMMENT '试卷ID',
  `score` decimal(5,1) DEFAULT '0.0' COMMENT '得分',
  `is_pass` tinyint(4) DEFAULT '0' COMMENT '是否通过',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `status` enum('DOING','SUBMITTED') DEFAULT 'DOING' COMMENT '状态',
  `cheat_count` int(11) DEFAULT '0' COMMENT '作弊次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_reservation`
--

DROP TABLE IF EXISTS `exam_reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exam_reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exam_paper_id` bigint(20) NOT NULL COMMENT '线下考试试卷ID',
  `user_id` bigint(20) NOT NULL COMMENT '学员ID',
  `reservation_time` datetime DEFAULT NULL COMMENT '预约考试时间',
  `status` enum('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING' COMMENT '预约状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_exam` (`user_id`,`exam_paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下考试预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `learning_record`
--

DROP TABLE IF EXISTS `learning_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `learning_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `chapter_id` bigint(20) NOT NULL COMMENT '章节ID',
  `watch_duration` bigint(20) DEFAULT '0' COMMENT '观看时长(秒)',
  `watch_percent` decimal(5,2) DEFAULT '0.00' COMMENT '观看百分比',
  `is_finished` tinyint(4) DEFAULT '0' COMMENT '是否完成',
  `last_position` bigint(20) DEFAULT '0' COMMENT '最后观看位置(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `news_article`
--

DROP TABLE IF EXISTS `news_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `news_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图',
  `content` longtext COMMENT '图文内容(HTML)',
  `source` varchar(100) DEFAULT NULL COMMENT '来源',
  `view_count` int(11) DEFAULT '0' COMMENT '阅读量',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1发布 0草稿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='新闻资讯表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `offline_checkin`
--

DROP TABLE IF EXISTS `offline_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `offline_checkin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '线下课程ID',
  `checkin_longitude` decimal(11,8) DEFAULT NULL COMMENT '打卡时经度',
  `checkin_latitude` decimal(10,8) DEFAULT NULL COMMENT '打卡时纬度',
  `distance` int(11) DEFAULT NULL COMMENT '打卡距离(米)',
  `checkin_type` enum('SELF','ADMIN') DEFAULT 'SELF' COMMENT '打卡方式',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='线下课程打卡记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `product_type` varchar(20) DEFAULT 'COURSE' COMMENT '购买类型:COURSE/CATEGORY',
  `product_id` bigint(20) DEFAULT NULL COMMENT '产品ID(课程ID或分类ID)',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `pay_method` enum('WECHAT','ALIPAY') DEFAULT NULL COMMENT '支付方式',
  `status` enum('PENDING','PAID','CANCELLED','REFUNDED') DEFAULT 'PENDING' COMMENT '订单状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_log`
--

DROP TABLE IF EXISTS `payment_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payment_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '订单号',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '交易流水号',
  `pay_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `status` varchar(20) DEFAULT NULL COMMENT '支付状态',
  `callback_data` text COMMENT '回调数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='支付日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) DEFAULT NULL COMMENT '课程ID',
  `chapter_id` bigint(20) DEFAULT NULL COMMENT '章节ID',
  `type` enum('SINGLE','MULTIPLE','JUDGE','ESSAY') NOT NULL COMMENT '题型',
  `content` text NOT NULL COMMENT '题目内容',
  `score` int(11) DEFAULT '1' COMMENT '分值',
  `answer` text COMMENT '答案',
  `analysis` text COMMENT '解析',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=284 DEFAULT CHARSET=utf8mb4 COMMENT='题目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_option`
--

DROP TABLE IF EXISTS `question_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question_option` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `option_label` varchar(10) DEFAULT NULL COMMENT '选项标签 A/B/C/D',
  `content` text COMMENT '选项内容',
  `is_correct` tinyint(4) DEFAULT '0' COMMENT '是否正确答案',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=998 DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_exercise_access`
--

DROP TABLE IF EXISTS `student_exercise_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student_exercise_access` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '学员ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员习题访问权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父菜单ID，0为顶级菜单',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `type` varchar(20) NOT NULL DEFAULT 'MENU' COMMENT '类型: MENU菜单 BUTTON按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `visible` tinyint(4) DEFAULT '1' COMMENT '是否可见 1可见 0隐藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` longtext COMMENT '配置值（HTML富文本）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `password` varchar(200) DEFAULT NULL COMMENT '密码',
  `role` enum('ADMIN','STUDENT') DEFAULT 'STUDENT' COMMENT '角色',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1正常 0禁用',
  `approval_status` varchar(20) DEFAULT 'APPROVED' COMMENT '审核状态',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `education` varchar(50) DEFAULT NULL COMMENT '学历',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `register_time` datetime DEFAULT NULL COMMENT '注册时间',
  `total_study_duration` bigint(20) DEFAULT '0' COMMENT '总学习时长(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wrong_question`
--

DROP TABLE IF EXISTS `wrong_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wrong_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `wrong_count` int(11) DEFAULT '1' COMMENT '错误次数',
  `last_wrong_time` datetime DEFAULT NULL COMMENT '最后错误时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COMMENT='错题表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'online_train'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-03 11:50:20
