-- ============================================
-- 添加经营数据分析报告菜单
-- 执行时间: 2026-04-06
-- ============================================

USE bluecat;

-- 添加经营数据分析报告菜单 (在经营分析之后，sort_order=4)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, icon, component, menu_type, sort_order, visible, status) 
VALUES (0, '经营数据分析', 'business-report', '/report', 'FundOutlined', 'views/report/index.vue', 2, 4, 1, 1);

-- 获取新插入的菜单ID
SET @new_menu_id = LAST_INSERT_ID();

-- 给超级管理员分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @new_menu_id);

-- 给运营管理员分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, @new_menu_id);

-- 给数据查看员分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, @new_menu_id);

-- 更新其他菜单的排序 (原sort_order>=4的菜单需要+1)
UPDATE sys_menu SET sort_order = sort_order + 1 
WHERE parent_id = 0 AND sort_order >= 4 AND id != @new_menu_id;

SELECT '经营数据分析报告菜单添加成功!' AS message;
