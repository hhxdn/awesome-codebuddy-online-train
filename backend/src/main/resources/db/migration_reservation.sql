-- 线下考试预约表
CREATE TABLE IF NOT EXISTS `exam_reservation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_paper_id` BIGINT NOT NULL COMMENT '线下考试试卷ID',
    `user_id` BIGINT NOT NULL COMMENT '学员ID',
    `reservation_time` DATETIME COMMENT '预约考试时间',
    `status` ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING' COMMENT '预约状态: PENDING待确认 CONFIRMED已确认 CANCELLED已取消 COMPLETED已完成',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_exam` (`user_id`, `exam_paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下考试预约表';
