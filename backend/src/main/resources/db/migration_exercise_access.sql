-- 学员习题访问权限表
-- 管理员通过后台为学员开通某课程的习题权限后，学员才能看到该课程的"练习"按钮
DROP TABLE IF EXISTS `student_exercise_access`;
CREATE TABLE `student_exercise_access` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '学员ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员习题访问权限表';
