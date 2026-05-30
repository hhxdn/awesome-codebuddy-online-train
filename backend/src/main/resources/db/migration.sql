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
