-- ============================================
-- BlueCat 数据权限 SQL 脚本（网吧配置级别）
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 创建时间: 2026-04-17
-- ============================================

USE bluecat;

-- ============================================
-- 1. 删除旧的表（如果存在）
-- ============================================
DROP TABLE IF EXISTS sys_user_shop;

-- ============================================
-- 2. 修改用户表，添加数据权限字段
-- ============================================
-- 使用存储过程安全添加字段（兼容所有MySQL版本）

DELIMITER //

DROP PROCEDURE IF EXISTS add_column_if_not_exists //

CREATE PROCEDURE add_column_if_not_exists()
BEGIN
    -- 检查列是否存在
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns 
        WHERE table_schema = DATABASE() 
        AND table_name = 'sys_user' 
        AND column_name = 'data_scope'
    ) THEN
        ALTER TABLE sys_user ADD COLUMN data_scope TINYINT DEFAULT 1 COMMENT '数据权限范围:1仅授权配置,2全部数据' AFTER last_login_ip;
    END IF;
END //

DELIMITER ;

-- 执行存储过程
CALL add_column_if_not_exists();

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ============================================
-- 3. 创建用户-网吧配置关联表
-- ============================================
DROP TABLE IF EXISTS sys_user_config;
CREATE TABLE sys_user_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    config_id BIGINT NOT NULL COMMENT '网吧配置ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_config (user_id, config_id),
    INDEX idx_user_id (user_id),
    INDEX idx_config_id (config_id),
    CONSTRAINT fk_user_config_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_config_config FOREIGN KEY (config_id) REFERENCES shop_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户网吧配置关联表';

-- ============================================
-- 4. 初始化超级管理员数据权限
-- ============================================
-- 给admin用户设置全部数据权限
UPDATE sys_user SET data_scope = 2 WHERE username = 'admin';

-- ============================================
-- 5. 创建索引优化查询性能
-- ============================================
-- 用户表添加数据权限索引
ALTER TABLE sys_user ADD INDEX idx_data_scope (data_scope);

-- ============================================
-- 完成提示
-- ============================================
SELECT '数据权限初始化完成!' AS message;
SELECT '已给admin用户设置全部数据权限' AS info;
SELECT '其他用户默认无任何网吧配置权限，需要手动分配' AS info;
SELECT '用户被授权某个网吧配置后，能看到该配置下的所有门店数据' AS info;
