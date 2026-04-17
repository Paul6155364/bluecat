-- ============================================
-- BlueCat 权限系统 SQL 脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 创建时间: 2026-04-02
-- ============================================

USE bluecat;

-- ============================================
-- 1. 角色表
-- ============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 2. 菜单表
-- ============================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID,0为顶级菜单',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) COMMENT '菜单编码(权限标识)',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '菜单图标',
    component VARCHAR(200) COMMENT '组件路径',
    menu_type TINYINT DEFAULT 1 COMMENT '类型:1目录,2菜单,3按钮',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见:1是,0否',
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0禁用',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ============================================
-- 3. 角色菜单关联表
-- ============================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================
-- 4. 用户角色关联表
-- ============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 初始化角色数据
-- ============================================
INSERT INTO sys_role (role_name, role_code, description, status, sort_order) VALUES
('超级管理员', 'admin', '拥有所有权限', 1, 1),
('运营管理员', 'operator', '负责日常运营管理', 1, 2),
('数据查看员', 'viewer', '只能查看数据', 1, 3);

-- ============================================
-- 初始化菜单数据
-- ============================================
-- 一级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, path, icon, component, menu_type, sort_order) VALUES
(1, 0, '数据大屏', 'dashboard', '/dashboard', 'DashboardOutlined', 'views/dashboard/index.vue', 2, 1),
(2, 0, '竞品大屏', 'competitor', '/competitor', 'StockOutlined', 'views/competitor/index.vue', 2, 2),
(3, 0, '经营分析', 'analysis', '/analysis', 'BarChartOutlined', 'views/analysis/index.vue', 2, 3),
(4, 0, '网吧配置', 'config', '/config', 'SettingOutlined', 'views/config/index.vue', 2, 4),
(5, 0, '门店管理', 'shop', '/shop', 'ShopOutlined', 'views/shop/index.vue', 2, 5),
(6, 0, '机器监控', 'machine', '/machine', 'DesktopOutlined', 'views/machine/index.vue', 2, 6),
(7, 0, '状态快照', 'status', '/status', 'LineChartOutlined', 'views/status/index.vue', 2, 7),
(8, 0, '历史数据', 'history', '/history', 'HistoryOutlined', 'views/history/index.vue', 2, 8),
(12, 0, 'PK关系管理', 'pk-relation', '/pk-relation', 'ThunderboltOutlined', 'views/system/pk/index.vue', 2, 9),
(9, 0, '门店PK', 'pk', '/pk', 'ThunderboltOutlined', 'views/pk/index.vue', 2, 10),
(10, 0, '时段热力图', 'heatmap', '/heatmap', 'HeatMapOutlined', 'views/heatmap/index.vue', 2, 11),
(11, 0, '系统管理', 'system', '/system', 'SettingOutlined', NULL, 1, 99);

-- 二级菜单 - 系统管理
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, icon, component, menu_type, sort_order) VALUES
(11, '用户管理', 'user', '/system/user', 'UserOutlined', 'views/system/user/index.vue', 2, 1),
(11, '角色管理', 'role', '/system/role', 'TeamOutlined', 'views/system/role/index.vue', 2, 2),
(11, '菜单管理', 'menu', '/system/menu', 'MenuOutlined', 'views/system/menu/index.vue', 2, 3);

-- 按钮权限 - 用户管理
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, sort_order) VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'user') t), '新增用户', 'user:add', 3, 1),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'user') t), '编辑用户', 'user:edit', 3, 2),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'user') t), '删除用户', 'user:delete', 3, 3),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'user') t), '重置密码', 'user:resetPwd', 3, 4),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'user') t), '分配角色', 'user:assignRole', 3, 5);

-- 按钮权限 - 角色管理
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, sort_order) VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'role') t), '新增角色', 'role:add', 3, 1),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'role') t), '编辑角色', 'role:edit', 3, 2),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'role') t), '删除角色', 'role:delete', 3, 3),
((SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'role') t), '分配权限', 'role:assignMenu', 3, 4);

-- ============================================
-- 初始化角色菜单关联数据
-- ============================================
-- 超级管理员拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 运营管理员权限 (除系统管理外的所有菜单)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE id <= 12 AND id != 11;

-- 数据查看员权限 (只能查看大屏和状态)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE menu_code IN ('dashboard', 'competitor', 'status', 'heatmap', 'pk-relation');

-- ============================================
-- 初始化用户角色关联数据
-- ============================================
-- 给admin用户分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT id, 1 FROM sys_user WHERE username = 'admin';

-- ============================================
-- 创建视图 - 用户权限视图
-- ============================================
DROP VIEW IF EXISTS v_user_permissions;
CREATE VIEW v_user_permissions AS
SELECT 
    u.id AS user_id,
    u.username,
    u.real_name,
    r.id AS role_id,
    r.role_name,
    r.role_code,
    m.id AS menu_id,
    m.menu_name,
    m.menu_code,
    m.path,
    m.icon,
    m.menu_type
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE u.deleted = 0 AND r.deleted = 0 AND m.deleted = 0;

-- ============================================
-- 创建存储过程 - 获取用户菜单树
-- ============================================
DROP PROCEDURE IF EXISTS sp_get_user_menus;
DELIMITER //
CREATE PROCEDURE sp_get_user_menus(IN p_user_id BIGINT)
BEGIN
    SELECT DISTINCT
        m.id,
        m.parent_id,
        m.menu_name,
        m.menu_code,
        m.path,
        m.icon,
        m.component,
        m.menu_type,
        m.sort_order
    FROM sys_menu m
    INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
    INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
    WHERE ur.user_id = p_user_id
      AND m.status = 1
      AND m.visible = 1
      AND m.deleted = 0
    ORDER BY m.sort_order;
END //
DELIMITER ;

-- ============================================
-- 完成提示
-- ============================================
SELECT '权限系统初始化完成!' AS message;
SELECT '已创建角色: 超级管理员、运营管理员、数据查看员' AS info;
SELECT 'admin 用户已分配超级管理员角色' AS info;
