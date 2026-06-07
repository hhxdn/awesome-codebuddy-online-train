-- 新闻资讯模块管理
CREATE TABLE IF NOT EXISTS news_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '模块名称',
    type VARCHAR(30) NOT NULL COMMENT '模块类型标识',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻资讯模块';

-- 预置6个模块
INSERT INTO news_module (name, type, sort_order) VALUES
('资讯', 'NEWS', 1),
('主办单位', 'ORGANIZER', 2),
('培训风采', 'STYLE', 3),
('课程介绍', 'COURSE_INTRO', 4),
('培训通知', 'NOTICE', 5),
('师资介绍', 'TEACHER', 6);

-- 给 news_article 增加 module_id 字段
ALTER TABLE news_article ADD COLUMN module_id BIGINT DEFAULT NULL COMMENT '所属模块ID';
