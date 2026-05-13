/*
 Navicat Premium Dump SQL

 Source Server         : 8.156.34.133
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 8.156.34.133:3306
 Source Schema         : bluecat

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 29/04/2026 17:29:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for api_call_log
-- ----------------------------
DROP TABLE IF EXISTS `api_call_log`;
CREATE TABLE `api_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '网吧配置ID',
  `shop_id` bigint DEFAULT NULL COMMENT '门店ID',
  `api_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API名称',
  `api_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'API地址',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方法',
  `request_headers` json DEFAULT NULL COMMENT '请求头',
  `request_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求体',
  `response_code` int DEFAULT NULL COMMENT '响应码',
  `response_body` json DEFAULT NULL COMMENT '响应体',
  `status` tinyint DEFAULT '1' COMMENT '状态:1成功,0失败',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `duration_ms` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `call_time` datetime DEFAULT NULL COMMENT '调用时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_config_time` (`config_id`,`call_time`) USING BTREE,
  KEY `idx_shop_time` (`shop_id`,`call_time`) USING BTREE,
  KEY `idx_api_name` (`api_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2535 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='API调用日志表';

-- ----------------------------
-- Table structure for area_fee_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `area_fee_snapshot`;
CREATE TABLE `area_fee_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `snapshot_id` bigint NOT NULL COMMENT '门店快照ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `vip_room` tinyint DEFAULT '0' COMMENT '是否VIP房间',
  `total_seats` int DEFAULT NULL COMMENT '总座位数',
  `is_single_up` tinyint DEFAULT '0' COMMENT '是否单上',
  `rate` decimal(10,2) DEFAULT NULL COMMENT '费率',
  `estimate_fee` decimal(10,2) DEFAULT NULL COMMENT '预估费用',
  `unit_rate` decimal(10,2) DEFAULT NULL COMMENT '单位费率',
  `whole` tinyint DEFAULT '0' COMMENT '是否整包',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_snapshot_id` (`snapshot_id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5979 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域费用快照表';

-- ----------------------------
-- Table structure for area_status_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `area_status_snapshot`;
CREATE TABLE `area_status_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `snapshot_id` bigint NOT NULL COMMENT '门店快照ID',
  `task_id` bigint NOT NULL COMMENT '采集任务ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `vip_room` tinyint DEFAULT '0' COMMENT '是否VIP房间:1是,0否',
  `total_machines` int DEFAULT '0' COMMENT '机器总数',
  `free_machines` int DEFAULT '0' COMMENT '空闲机器数',
  `busy_machines` int DEFAULT '0' COMMENT '占用机器数',
  `free_machine_list` json DEFAULT NULL COMMENT '空闲机器名称列表',
  `occupancy_rate` decimal(5,2) DEFAULT '0.00' COMMENT '上座率(%)',
  `snapshot_time` datetime DEFAULT NULL COMMENT '快照时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_snapshot_id` (`snapshot_id`) USING BTREE,
  KEY `idx_shop_time` (`shop_id`,`create_time`) USING BTREE,
  KEY `idx_snapshot_time` (`snapshot_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7038 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域实时状态快照表';

-- ----------------------------
-- Table structure for data_collection_task
-- ----------------------------
DROP TABLE IF EXISTS `data_collection_task`;
CREATE TABLE `data_collection_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '网吧配置ID',
  `shop_id` bigint DEFAULT NULL COMMENT '门店ID(单个门店采集时)',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型:SHOP_LIST/SHOP_INFO/MACHINE_STATUS',
  `snapshot_time` datetime DEFAULT NULL COMMENT '快照时间(get-sys-current-seconds)',
  `status` tinyint DEFAULT '0' COMMENT '状态:0执行中,1成功,2失败',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_ms` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_config_time` (`config_id`,`create_time`) USING BTREE,
  KEY `idx_shop_time` (`shop_id`,`create_time`) USING BTREE,
  KEY `idx_snapshot_time` (`snapshot_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=455 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据采集任务表';

-- ----------------------------
-- Table structure for machine_ban_list
-- ----------------------------
DROP TABLE IF EXISTS `machine_ban_list`;
CREATE TABLE `machine_ban_list` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `com_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机器名称',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区域名称',
  `vip_room` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'VIP房间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='禁止预订机器表';

-- ----------------------------
-- Table structure for machine_info
-- ----------------------------
DROP TABLE IF EXISTS `machine_info`;
CREATE TABLE `machine_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `area_id` bigint DEFAULT NULL COMMENT '区域ID',
  `com_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机器名称(S001等)',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属区域名称',
  `card_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '绑定卡ID(加密)',
  `last_offline_time` datetime DEFAULT NULL COMMENT '最后下线时间',
  `raw_json` json DEFAULT NULL COMMENT 'get-area-com-set-info原始JSON数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_shop_com` (`shop_id`,`com_name`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE,
  KEY `idx_area_id` (`area_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23166 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器信息表';

-- ----------------------------
-- Table structure for machine_status_history
-- ----------------------------
DROP TABLE IF EXISTS `machine_status_history`;
CREATE TABLE `machine_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `snapshot_id` bigint NOT NULL COMMENT '门店快照ID',
  `task_id` bigint NOT NULL COMMENT '采集任务ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `machine_id` bigint NOT NULL COMMENT '机器ID',
  `com_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机器名称',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属区域',
  `status` tinyint NOT NULL COMMENT '状态:1空闲,0占用',
  `snapshot_time` datetime NOT NULL COMMENT '快照时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_snapshot_id` (`snapshot_id`) USING BTREE,
  KEY `idx_shop_time` (`shop_id`,`snapshot_time`) USING BTREE,
  KEY `idx_machine_time` (`machine_id`,`snapshot_time`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29998 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器实时状态历史表';

-- ----------------------------
-- Table structure for shop_area
-- ----------------------------
DROP TABLE IF EXISTS `shop_area`;
CREATE TABLE `shop_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `allow` tinyint DEFAULT '1' COMMENT '是否允许预订:1允许,0不允许',
  `min_num` int DEFAULT '0' COMMENT '最小预订数量',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_shop_area` (`shop_id`,`area_name`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店区域表';

-- ----------------------------
-- Table structure for shop_config
-- ----------------------------
DROP TABLE IF EXISTS `shop_config`;
CREATE TABLE `shop_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `snbid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '网吧主账号snbid',
  `platform_type` tinyint DEFAULT '0' COMMENT '平台类型:0=x管家,1=银杏管家,2=网鱼网咖',
  `app_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'AppID',
  `jwt_token` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'JWT Token',
  `cookie` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT 'Cookie',
  `token_expire_time` datetime DEFAULT NULL COMMENT 'Token过期时间',
  `token_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Token来源',
  `status` tinyint DEFAULT '1' COMMENT '状态:1启用,0禁用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_snbid` (`snbid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='网吧配置表';

-- ----------------------------
-- Table structure for shop_image
-- ----------------------------
DROP TABLE IF EXISTS `shop_image`;
CREATE TABLE `shop_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `pic_id` bigint DEFAULT NULL COMMENT '图片ID',
  `pic_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片URL',
  `cover` tinyint DEFAULT '0' COMMENT '是否封面:1是,0否',
  `system_pic` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '系统图片标记',
  `pic_state` tinyint DEFAULT '1' COMMENT '图片状态:1正常',
  `dt_update` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1752 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店图片表';

-- ----------------------------
-- Table structure for shop_info
-- ----------------------------
DROP TABLE IF EXISTS `shop_info`;
CREATE TABLE `shop_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '网吧配置ID',
  `snbid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门店编号',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店名称',
  `snb_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店简称',
  `login_province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录省份',
  `login_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录城市',
  `login_zone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录区县',
  `login_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录详细地址',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `province_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省份名称',
  `city_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '城市名称',
  `zone_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区县名称',
  `stel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '前台电话',
  `sboss_tel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '老板电话',
  `net_mobile` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '网吧手机',
  `lb_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '蓝猫电话',
  `qq` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'QQ号',
  `wifi_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'WiFi名称',
  `wifi_pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'WiFi密码',
  `head` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店头像URL',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude_tencent` decimal(10,7) DEFAULT NULL COMMENT '腾讯经度',
  `latitude_tencent` decimal(10,7) DEFAULT NULL COMMENT '腾讯纬度',
  `region_bidu_id` int DEFAULT NULL COMMENT '区域百度ID',
  `province_name_id` int DEFAULT NULL COMMENT '省份ID',
  `city_name_id` int DEFAULT NULL COMMENT '城市ID',
  `have_room` tinyint DEFAULT NULL COMMENT '是否有包间:1是,0否',
  `have_book_seat` tinyint DEFAULT NULL COMMENT '是否支持预订:1是,0否',
  `have_ode_new_activity` tinyint DEFAULT NULL COMMENT '是否有新活动:1是,0否',
  `status` tinyint DEFAULT NULL COMMENT '营业状态:2正常',
  `have_open_lease_benefit` tinyint DEFAULT NULL COMMENT '是否开启租赁权益:1是,0否',
  `have_open_coupon_benefit` tinyint DEFAULT NULL COMMENT '是否开启优惠券权益:1是,0否',
  `shop_id` bigint DEFAULT NULL COMMENT '系统店铺ID',
  `raw_json` json DEFAULT NULL COMMENT 'getSnbidInfo原始JSON数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_snbid` (`config_id`,`snbid`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=287 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店信息表';

-- ----------------------------
-- Table structure for shop_pk_relation
-- ----------------------------
DROP TABLE IF EXISTS `shop_pk_relation`;
CREATE TABLE `shop_pk_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PK关系名称',
  `main_shop_id` bigint NOT NULL COMMENT '主门店ID',
  `main_shop_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '主门店名称',
  `competitor_shop_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PK对手门店ID列表（逗号分隔）',
  `competitor_shop_names` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'PK对手门店名称列表（逗号分隔）',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记 0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_create_by` (`create_by`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店PK关系表';

-- ----------------------------
-- Table structure for shop_status_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `shop_status_snapshot`;
CREATE TABLE `shop_status_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '采集任务ID',
  `shop_id` bigint NOT NULL COMMENT '门店ID',
  `snapshot_time` datetime NOT NULL COMMENT '快照时间',
  `total_machines` int DEFAULT '0' COMMENT '机器总数',
  `free_machines` int DEFAULT '0' COMMENT '空闲机器数',
  `busy_machines` int DEFAULT '0' COMMENT '占用机器数',
  `occupancy_rate` decimal(5,2) DEFAULT '0.00' COMMENT '整体上座率(%)',
  `remain` decimal(10,2) DEFAULT NULL COMMENT '剩余时长',
  `day_revenue` decimal(10,2) DEFAULT NULL COMMENT '今日累计营收(元)',
  `raw_json` json DEFAULT NULL COMMENT 'general-order-book-area-com-result原始JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_time` (`shop_id`,`snapshot_time`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=344 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店实时状态快照表';

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '真实姓名',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作系统',
  `login_status` tinyint DEFAULT '1' COMMENT '登录状态:1成功,0失败',
  `login_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录消息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_login_time` (`login_time`) USING BTREE,
  KEY `idx_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID,0为顶级菜单',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单编码(权限标识)',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `menu_type` tinyint DEFAULT '1' COMMENT '类型:1目录,2菜单,3按钮',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `visible` tinyint DEFAULT '1' COMMENT '是否可见:1是,0否',
  `status` tinyint DEFAULT '1' COMMENT '状态:1正常,0禁用',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态:1正常,0禁用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_code` (`role_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_menu_id` (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态:1正常,0禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记:0未删除,1已删除',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据权限范围:1仅授权网吧,2全部数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `idx_data_scope` (`data_scope`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员表';

-- ----------------------------
-- Table structure for sys_user_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `config_id` bigint NOT NULL COMMENT '网吧配置ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_config` (`user_id`,`config_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_config_id` (`config_id`) USING BTREE,
  CONSTRAINT `fk_user_config_config` FOREIGN KEY (`config_id`) REFERENCES `shop_config` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_config_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户网吧配置关联表';

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ----------------------------
-- View structure for v_area_realtime_status
-- ----------------------------
DROP VIEW IF EXISTS `v_area_realtime_status`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_area_realtime_status` AS select `ass`.`id` AS `id`,`ass`.`snapshot_id` AS `snapshot_id`,`ass`.`shop_id` AS `shop_id`,`si`.`name` AS `shop_name`,`ass`.`area_name` AS `area_name`,`ass`.`vip_room` AS `vip_room`,`ass`.`total_machines` AS `total_machines`,`ass`.`free_machines` AS `free_machines`,`ass`.`busy_machines` AS `busy_machines`,`ass`.`occupancy_rate` AS `occupancy_rate`,`ass`.`create_time` AS `create_time` from (`area_status_snapshot` `ass` join `shop_info` `si` on((`ass`.`shop_id` = `si`.`id`))) where (`ass`.`snapshot_id` = (select max(`shop_status_snapshot`.`id`) from `shop_status_snapshot` where (`shop_status_snapshot`.`shop_id` = `ass`.`shop_id`)));

-- ----------------------------
-- View structure for v_shop_realtime_status
-- ----------------------------
DROP VIEW IF EXISTS `v_shop_realtime_status`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_shop_realtime_status` AS select `ss`.`id` AS `snapshot_id`,`ss`.`shop_id` AS `shop_id`,`si`.`name` AS `shop_name`,`si`.`snbid` AS `snbid`,`si`.`address` AS `address`,`ss`.`snapshot_time` AS `snapshot_time`,`ss`.`total_machines` AS `total_machines`,`ss`.`free_machines` AS `free_machines`,`ss`.`busy_machines` AS `busy_machines`,`ss`.`occupancy_rate` AS `occupancy_rate`,`sc`.`config_name` AS `config_name` from ((`shop_status_snapshot` `ss` join `shop_info` `si` on((`ss`.`shop_id` = `si`.`id`))) join `shop_config` `sc` on((`si`.`config_id` = `sc`.`id`))) where (`ss`.`id` = (select max(`shop_status_snapshot`.`id`) from `shop_status_snapshot` where (`shop_status_snapshot`.`shop_id` = `ss`.`shop_id`)));

-- ----------------------------
-- View structure for v_user_permissions
-- ----------------------------
DROP VIEW IF EXISTS `v_user_permissions`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_permissions` AS select `u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`r`.`id` AS `role_id`,`r`.`role_name` AS `role_name`,`r`.`role_code` AS `role_code`,`m`.`id` AS `menu_id`,`m`.`menu_name` AS `menu_name`,`m`.`menu_code` AS `menu_code`,`m`.`path` AS `path`,`m`.`icon` AS `icon`,`m`.`menu_type` AS `menu_type` from ((((`sys_user` `u` left join `sys_user_role` `ur` on((`u`.`id` = `ur`.`user_id`))) left join `sys_role` `r` on((`ur`.`role_id` = `r`.`id`))) left join `sys_role_menu` `rm` on((`r`.`id` = `rm`.`role_id`))) left join `sys_menu` `m` on((`rm`.`menu_id` = `m`.`id`))) where ((`u`.`deleted` = 0) and (`r`.`deleted` = 0) and (`m`.`deleted` = 0));

-- ----------------------------
-- Procedure structure for sp_clean_old_logs
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_clean_old_logs`;
delimiter ;;
CREATE PROCEDURE `sp_clean_old_logs`(IN days_ago INT)
BEGIN
    DECLARE delete_before DATETIME;
    SET delete_before = DATE_SUB(NOW(), INTERVAL days_ago DAY);
    
    -- 清理API调用日志(保留30天)
    DELETE FROM api_call_log WHERE create_time < delete_before;
    
    -- 清理数据采集任务(保留30天)
    DELETE FROM data_collection_task WHERE create_time < delete_before;
    
    SELECT ROW_COUNT() AS deleted_rows;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_daily_statistics
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_daily_statistics`;
delimiter ;;
CREATE PROCEDURE `sp_daily_statistics`(IN shop_id_param BIGINT, IN stat_date DATE)
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
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_get_user_menus
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_get_user_menus`;
delimiter ;;
CREATE PROCEDURE `sp_get_user_menus`(IN p_user_id BIGINT)
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
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
