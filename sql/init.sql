-- ============================================
-- BlueCat 网吧管理系统 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 创建时间: 2026-03-30
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bluecat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE bluecat;

-- ============================================
-- 1. 管理员表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态:1正常,0禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ============================================
-- 2. 网吧配置表
-- ============================================
DROP TABLE IF EXISTS shop_config;
CREATE TABLE shop_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    snbid VARCHAR(50) NOT NULL COMMENT '网吧主账号snbid',
    app_id VARCHAR(50) NOT NULL COMMENT 'AppID',
    jwt_token TEXT NOT NULL COMMENT 'JWT Token',
    cookie TEXT COMMENT 'Cookie',
    token_expire_time DATETIME COMMENT 'Token过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态:1启用,0禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_snbid (snbid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网吧配置表';

-- ============================================
-- 3. 门店信息表 (存储getSnbidInfo原始数据)
-- ============================================
DROP TABLE IF EXISTS shop_info;
CREATE TABLE shop_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '网吧配置ID',
    snbid VARCHAR(50) NOT NULL COMMENT '门店编号',
    name VARCHAR(200) COMMENT '门店名称',
    snb_name VARCHAR(200) COMMENT '门店简称',
    login_province VARCHAR(50) COMMENT '登录省份',
    login_city VARCHAR(50) COMMENT '登录城市',
    login_zone VARCHAR(50) COMMENT '登录区县',
    login_address VARCHAR(500) COMMENT '登录详细地址',
    address VARCHAR(500) COMMENT '地址',
    province_name VARCHAR(50) COMMENT '省份名称',
    city_name VARCHAR(50) COMMENT '城市名称',
    zone_name VARCHAR(50) COMMENT '区县名称',
    stel VARCHAR(50) COMMENT '前台电话',
    sboss_tel VARCHAR(50) COMMENT '老板电话',
    net_mobile VARCHAR(50) COMMENT '网吧手机',
    lb_phone VARCHAR(50) COMMENT '蓝猫电话',
    qq VARCHAR(50) COMMENT 'QQ号',
    wifi_name VARCHAR(100) COMMENT 'WiFi名称',
    wifi_pwd VARCHAR(100) COMMENT 'WiFi密码',
    head VARCHAR(500) COMMENT '门店头像URL',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude_tencent DECIMAL(10,7) COMMENT '腾讯经度',
    latitude_tencent DECIMAL(10,7) COMMENT '腾讯纬度',
    region_bidu_id INT COMMENT '区域百度ID',
    province_name_id INT COMMENT '省份ID',
    city_name_id INT COMMENT '城市ID',
    have_room TINYINT COMMENT '是否有包间:1是,0否',
    have_book_seat TINYINT COMMENT '是否支持预订:1是,0否',
    have_ode_new_activity TINYINT COMMENT '是否有新活动:1是,0否',
    status TINYINT COMMENT '营业状态:2正常',
    have_open_lease_benefit TINYINT COMMENT '是否开启租赁权益:1是,0否',
    have_open_coupon_benefit TINYINT COMMENT '是否开启优惠券权益:1是,0否',
    shop_id BIGINT COMMENT '系统店铺ID',
    raw_json JSON COMMENT 'getSnbidInfo原始JSON数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_config_snbid (config_id, snbid),
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店信息表';

-- ============================================
-- 4. 门店图片表
-- ============================================
DROP TABLE IF EXISTS shop_image;
CREATE TABLE shop_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    pic_id BIGINT COMMENT '图片ID',
    pic_url VARCHAR(500) COMMENT '图片URL',
    cover TINYINT DEFAULT 0 COMMENT '是否封面:1是,0否',
    system_pic VARCHAR(10) COMMENT '系统图片标记',
    pic_state TINYINT DEFAULT 1 COMMENT '图片状态:1正常',
    dt_update DATETIME COMMENT '更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店图片表';

-- ============================================
-- 5. 门店区域表 (存储get-book-seat-config区域配置)
-- ============================================
DROP TABLE IF EXISTS shop_area;
CREATE TABLE shop_area (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    allow TINYINT DEFAULT 1 COMMENT '是否允许预订:1允许,0不允许',
    min_num INT DEFAULT 0 COMMENT '最小预订数量',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_shop_area (shop_id, area_name),
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店区域表';

-- ============================================
-- 6. 机器信息表 (存储get-area-com-set-info原始数据)
-- ============================================
DROP TABLE IF EXISTS machine_info;
CREATE TABLE machine_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    area_id BIGINT COMMENT '区域ID',
    com_name VARCHAR(50) NOT NULL COMMENT '机器名称(S001等)',
    area_name VARCHAR(100) COMMENT '所属区域名称',
    card_id VARCHAR(100) COMMENT '绑定卡ID(加密)',
    last_offline_time DATETIME COMMENT '最后下线时间',
    raw_json JSON COMMENT 'get-area-com-set-info原始JSON数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记:0未删除,1已删除',
    UNIQUE KEY uk_shop_com (shop_id, com_name),
    INDEX idx_shop_id (shop_id),
    INDEX idx_area_id (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器信息表';

-- ============================================
-- 7. 禁止预订机器表
-- ============================================
DROP TABLE IF EXISTS machine_ban_list;
CREATE TABLE machine_ban_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    com_name VARCHAR(50) NOT NULL COMMENT '机器名称',
    area_name VARCHAR(100) COMMENT '区域名称',
    vip_room VARCHAR(100) COMMENT 'VIP房间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁止预订机器表';

-- ============================================
-- 8. 数据采集任务表 (记录每次采集)
-- ============================================
DROP TABLE IF EXISTS data_collection_task;
CREATE TABLE data_collection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '网吧配置ID',
    shop_id BIGINT COMMENT '门店ID(单个门店采集时)',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型:SHOP_LIST/SHOP_INFO/MACHINE_STATUS',
    snapshot_time DATETIME COMMENT '快照时间(get-sys-current-seconds)',
    status TINYINT DEFAULT 0 COMMENT '状态:0执行中,1成功,2失败',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms INT COMMENT '耗时(毫秒)',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_time (config_id, create_time),
    INDEX idx_shop_time (shop_id, create_time),
    INDEX idx_snapshot_time (snapshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据采集任务表';

-- ============================================
-- 9. 门店实时状态快照表 (存储general-order-book-area-com-result)
-- ============================================
DROP TABLE IF EXISTS shop_status_snapshot;
CREATE TABLE shop_status_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '采集任务ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    snapshot_time DATETIME NOT NULL COMMENT '快照时间',
    total_machines INT DEFAULT 0 COMMENT '机器总数',
    free_machines INT DEFAULT 0 COMMENT '空闲机器数',
    busy_machines INT DEFAULT 0 COMMENT '占用机器数',
    occupancy_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '整体上座率(%)',
    remain DECIMAL(10,2) COMMENT '剩余时长',
    raw_json JSON COMMENT 'general-order-book-area-com-result原始JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_shop_time (shop_id, snapshot_time),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店实时状态快照表';

-- ============================================
-- 10. 区域实时状态快照表
-- ============================================
DROP TABLE IF EXISTS area_status_snapshot;
CREATE TABLE area_status_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    snapshot_id BIGINT NOT NULL COMMENT '门店快照ID',
    task_id BIGINT NOT NULL COMMENT '采集任务ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    vip_room TINYINT DEFAULT 0 COMMENT '是否VIP房间:1是,0否',
    total_machines INT DEFAULT 0 COMMENT '机器总数',
    free_machines INT DEFAULT 0 COMMENT '空闲机器数',
    busy_machines INT DEFAULT 0 COMMENT '占用机器数',
    free_machine_list JSON COMMENT '空闲机器名称列表',
    occupancy_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '上座率(%)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_shop_time (shop_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域实时状态快照表';

-- ============================================
-- 11. 机器实时状态表 (历史记录,全部保留)
-- ============================================
DROP TABLE IF EXISTS machine_status_history;
CREATE TABLE machine_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    snapshot_id BIGINT NOT NULL COMMENT '门店快照ID',
    task_id BIGINT NOT NULL COMMENT '采集任务ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    machine_id BIGINT NOT NULL COMMENT '机器ID',
    com_name VARCHAR(50) NOT NULL COMMENT '机器名称',
    area_name VARCHAR(100) COMMENT '所属区域',
    status TINYINT NOT NULL COMMENT '状态:1空闲,0占用',
    snapshot_time DATETIME NOT NULL COMMENT '快照时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_shop_time (shop_id, snapshot_time),
    INDEX idx_machine_time (machine_id, snapshot_time),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器实时状态历史表';

-- ============================================
-- 12. 区域费用快照表
-- ============================================
DROP TABLE IF EXISTS area_fee_snapshot;
CREATE TABLE area_fee_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    snapshot_id BIGINT NOT NULL COMMENT '门店快照ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    vip_room TINYINT DEFAULT 0 COMMENT '是否VIP房间',
    total_seats INT COMMENT '总座位数',
    is_single_up TINYINT DEFAULT 0 COMMENT '是否单上',
    rate DECIMAL(10,2) COMMENT '费率',
    estimate_fee DECIMAL(10,2) COMMENT '预估费用',
    unit_rate DECIMAL(10,2) COMMENT '单位费率',
    whole TINYINT DEFAULT 0 COMMENT '是否整包',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域费用快照表';

-- ============================================
-- 13. API调用日志表
-- ============================================
DROP TABLE IF EXISTS api_call_log;
CREATE TABLE api_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '网吧配置ID',
    shop_id BIGINT COMMENT '门店ID',
    api_name VARCHAR(100) NOT NULL COMMENT 'API名称',
    api_url VARCHAR(500) COMMENT 'API地址',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_headers JSON COMMENT '请求头',
    request_body TEXT COMMENT '请求体',
    response_code INT COMMENT '响应码',
    response_body JSON COMMENT '响应体',
    status TINYINT DEFAULT 1 COMMENT '状态:1成功,0失败',
    error_msg TEXT COMMENT '错误信息',
    duration_ms INT COMMENT '耗时(毫秒)',
    call_time DATETIME COMMENT '调用时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_time (config_id, call_time),
    INDEX idx_shop_time (shop_id, call_time),
    INDEX idx_api_name (api_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- ============================================
-- 初始化管理员数据
-- ============================================
-- 默认密码: admin123 (BCrypt加密)
INSERT INTO sys_user (username, password, real_name, status) 
VALUES ('admin', '$2a$10$TYNFpjKT1GVqGM3eRtxOVOf6WByTBP67reCZ4WS5MWWgAOK9lDe9e', '超级管理员', 1);

-- ============================================
-- 创建视图 - 门店实时状态视图
-- ============================================
DROP VIEW IF EXISTS v_shop_realtime_status;
CREATE VIEW v_shop_realtime_status AS
SELECT 
    ss.id AS snapshot_id,
    ss.shop_id,
    si.name AS shop_name,
    si.snbid,
    si.address,
    ss.snapshot_time,
    ss.total_machines,
    ss.free_machines,
    ss.busy_machines,
    ss.occupancy_rate,
    sc.config_name
FROM shop_status_snapshot ss
JOIN shop_info si ON ss.shop_id = si.id
JOIN shop_config sc ON si.config_id = sc.id
WHERE ss.id = (
    SELECT MAX(id) FROM shop_status_snapshot WHERE shop_id = ss.shop_id
);

-- ============================================
-- 创建视图 - 区域实时状态视图
-- ============================================
DROP VIEW IF EXISTS v_area_realtime_status;
CREATE VIEW v_area_realtime_status AS
SELECT 
    ass.id,
    ass.snapshot_id,
    ass.shop_id,
    si.name AS shop_name,
    ass.area_name,
    ass.vip_room,
    ass.total_machines,
    ass.free_machines,
    ass.busy_machines,
    ass.occupancy_rate,
    ass.create_time
FROM area_status_snapshot ass
JOIN shop_info si ON ass.shop_id = si.id
WHERE ass.snapshot_id = (
    SELECT MAX(id) FROM shop_status_snapshot WHERE shop_id = ass.shop_id
);

-- ============================================
-- 创建存储过程 - 清理过期日志
-- ============================================
DROP PROCEDURE IF EXISTS sp_clean_old_logs;
DELIMITER //
CREATE PROCEDURE sp_clean_old_logs(IN days_ago INT)
BEGIN
    DECLARE delete_before DATETIME;
    SET delete_before = DATE_SUB(NOW(), INTERVAL days_ago DAY);
    
    -- 清理API调用日志(保留30天)
    DELETE FROM api_call_log WHERE create_time < delete_before;
    
    -- 清理数据采集任务(保留30天)
    DELETE FROM data_collection_task WHERE create_time < delete_before;
    
    SELECT ROW_COUNT() AS deleted_rows;
END //
DELIMITER ;

-- ============================================
-- 创建存储过程 - 统计门店日报
-- ============================================
DROP PROCEDURE IF EXISTS sp_daily_statistics;
DELIMITER //
CREATE PROCEDURE sp_daily_statistics(IN shop_id_param BIGINT, IN stat_date DATE)
BEGIN
    SELECT 
        shop_id,
        DATE(snapshot_time) AS stat_date,
        COUNT(*) AS snapshot_count,
        AVG(occupancy_rate) AS avg_occupancy_rate,
        MAX(occupancy_rate) AS max_occupancy_rate,
        MIN(occupancy_rate) AS min_occupancy_rate,
        AVG(free_machines) AS avg_free_machines,
        AVG(busy_machines) AS avg_busy_machines
    FROM shop_status_snapshot
    WHERE shop_id = shop_id_param
      AND DATE(snapshot_time) = stat_date
    GROUP BY shop_id, DATE(snapshot_time);
END //
DELIMITER ;

-- ============================================
-- 完成提示
-- ============================================
SELECT '数据库初始化完成!' AS message;
SELECT '默认管理员账号: admin' AS username, '默认密码: admin123' AS password;
