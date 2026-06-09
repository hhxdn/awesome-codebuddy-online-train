-- 添加题目难度字段
ALTER TABLE question ADD COLUMN difficulty VARCHAR(20) DEFAULT NULL COMMENT '难度：简单/中等/困难' AFTER analysis;
