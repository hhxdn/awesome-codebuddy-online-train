-- ============================================
-- RBAC权限管理系统 - 建表+初始化数据
-- ============================================

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID，0为顶级菜单',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `type` VARCHAR(20) NOT NULL DEFAULT 'MENU' COMMENT '类型: MENU菜单 BUTTON按钮',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `icon` VARCHAR(50) COMMENT '图标',
    `permission_code` VARCHAR(100) COMMENT '权限标识',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `visible` TINYINT DEFAULT 1 COMMENT '是否可见 1可见 0隐藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================
-- 初始化数据
-- ============================================

-- 超级管理员角色
INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `status`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1);

-- 将现有管理员用户(phone=admin)关联到超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) 
SELECT u.id, 1 FROM `user` u WHERE u.role = 'ADMIN' AND u.phone = 'admin';

-- ============================================
-- 菜单数据（根据现有路由构建）
-- ============================================

-- 一级菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(1,  0,  '仪表盘',   'MENU', '/dashboard',           'DataAnalysis',  'dashboard',              1, 1),
(2,  0,  '内容管理', 'MENU', NULL,                    'Document',      NULL,                     2, 1),
(3,  0,  '考试管理', 'MENU', NULL,                    'Edit',          NULL,                     3, 1),
(4,  0,  '用户管理', 'MENU', NULL,                    'User',          NULL,                     4, 1),
(5,  0,  '订单管理', 'MENU', NULL,                    'ShoppingCart',  NULL,                     5, 1),
(6,  0,  '数据统计', 'MENU', NULL,                    'DataAnalysis',  NULL,                     6, 1),
(7,  0,  '系统管理', 'MENU', NULL,                    'Setting',       NULL,                     7, 1);

-- 内容管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(8,  2,  '课程分类',   'MENU', '/categories',           NULL, 'category:list',          1, 1),
(9,  2,  '课程管理',   'MENU', '/courses',              NULL, 'course:list',            2, 1),
(10, 2,  'Banner管理', 'MENU', '/banners',              NULL, 'banner:list',            3, 1),
(11, 2,  '新闻资讯',   'MENU', '/news',                 NULL, 'news:list',              4, 1);

-- 考试管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(12, 3,  '题库管理',   'MENU', '/questions',            NULL, 'question:list',          1, 1),
(13, 3,  '试卷管理',   'MENU', '/exams',                NULL, 'exam:paper:list',        2, 1),
(14, 3,  '随机组卷',   'MENU', '/exams/random',         NULL, 'exam:random',            3, 1),
(15, 3,  '考试记录',   'MENU', '/exams/records',        NULL, 'exam:record:list',       4, 1),
(16, 3,  '考试预约',   'MENU', '/reservations',         NULL, 'exam:reservation:list',  5, 1);

-- 用户管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(17, 4,  '学员管理',   'MENU', '/students',             NULL, 'user:list',              1, 1),
(18, 4,  '线下打卡',   'MENU', '/checkins',             NULL, 'checkin:list',           2, 1),
(19, 4,  '结业证书',   'MENU', '/certificates',         NULL, 'certificate:list',       3, 1);

-- 订单管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(20, 5,  '订单列表',   'MENU', '/orders',               NULL, 'order:list',             1, 1),
(21, 5,  '课程预约',   'MENU', '/course-reservations',  NULL, 'course:reservation:list',2, 1);

-- 数据统计子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(22, 6,  '学情统计',   'MENU', '/statistics/learning',  NULL, 'statistics:learning',    1, 1),
(23, 6,  '营收统计',   'MENU', '/statistics/revenue',   NULL, 'statistics:revenue',     2, 1),
(24, 6,  '考试统计',   'MENU', '/statistics/exam',      NULL, 'statistics:exam',        3, 1);

-- 系统管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `icon`, `permission_code`, `sort_order`, `visible`) VALUES
(25, 7,  '角色管理',   'MENU', '/system/roles',         NULL, 'system:role:list',       1, 1),
(26, 7,  '菜单管理',   'MENU', '/system/menus',         NULL, 'system:menu:list',       2, 1);

-- 按钮级权限（非菜单，仅权限标识）
INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `permission_code`, `sort_order`, `visible`) VALUES
-- 课程管理按钮
(9,  '新增课程', 'BUTTON', 'course:add',    1, 0),
(9,  '编辑课程', 'BUTTON', 'course:edit',   2, 0),
(9,  '删除课程', 'BUTTON', 'course:delete', 3, 0),
-- 分类管理按钮
(8,  '新增分类', 'BUTTON', 'category:add',    1, 0),
(8,  '编辑分类', 'BUTTON', 'category:edit',   2, 0),
(8,  '删除分类', 'BUTTON', 'category:delete', 3, 0),
-- 题库管理按钮
(12, '新增题目', 'BUTTON', 'question:add',    1, 0),
(12, '编辑题目', 'BUTTON', 'question:edit',   2, 0),
(12, '删除题目', 'BUTTON', 'question:delete', 3, 0),
(12, '导入题目', 'BUTTON', 'question:import', 4, 0),
-- 试卷管理按钮
(13, '新增试卷', 'BUTTON', 'exam:paper:add',    1, 0),
(13, '编辑试卷', 'BUTTON', 'exam:paper:edit',   2, 0),
(13, '删除试卷', 'BUTTON', 'exam:paper:delete', 3, 0),
(13, '预览试卷', 'BUTTON', 'exam:paper:preview',4, 0),
-- 学员管理按钮
(17, '编辑学员', 'BUTTON', 'user:edit',   1, 0),
(17, '禁用学员', 'BUTTON', 'user:disable', 2, 0),
-- 角色管理按钮
(25, '新增角色', 'BUTTON', 'system:role:add',    1, 0),
(25, '编辑角色', 'BUTTON', 'system:role:edit',   2, 0),
(25, '删除角色', 'BUTTON', 'system:role:delete', 3, 0),
-- 菜单管理按钮
(26, '新增菜单', 'BUTTON', 'system:menu:add',    1, 0),
(26, '编辑菜单', 'BUTTON', 'system:menu:edit',   2, 0),
(26, '删除菜单', 'BUTTON', 'system:menu:delete', 3, 0);

-- ============================================
-- 未在菜单树中的隐藏路由（如编辑页、详情页），不需要显示但需要权限
-- ============================================
INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `path`, `permission_code`, `sort_order`, `visible`) VALUES
(9,  '课程编辑', 'MENU', '/courses/edit/:id?',  'course:edit',   99, 0),
(13, '试卷编辑', 'MENU', '/exams/edit/:id?',    'exam:paper:edit', 99, 0),
(17, '学员详情', 'MENU', '/students/:id',      'user:detail',   99, 0);

-- ============================================
-- 超级管理员角色关联所有菜单
-- ============================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`;
