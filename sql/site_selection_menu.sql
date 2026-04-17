-- ============================================
-- 新店选址菜单 SQL 脚本
-- 创建时间: 2026-04-03
-- ============================================

USE bluecat;

-- 添加新店选址菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, icon, component, menu_type, sort_order) VALUES
(0, '新店选址', 'site-selection', '/site-selection', 'EnvironmentOutlined', 'views/site-selection/index.vue', 2, 12);

-- 获取新插入的菜单ID
SET @menu_id = LAST_INSERT_ID();

-- 为超级管理员添加新店选址权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @menu_id);

-- 为运营管理员添加新店选址权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, @menu_id);

-- 为数据查看员添加新店选址权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, @menu_id);

SELECT '新店选址菜单添加成功!' AS message;
