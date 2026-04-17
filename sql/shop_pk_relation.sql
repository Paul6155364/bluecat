-- 门店PK关系表
CREATE TABLE IF NOT EXISTS `shop_pk_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT 'PK关系名称',
    `main_shop_id` BIGINT NOT NULL COMMENT '主门店ID',
    `main_shop_name` VARCHAR(200) DEFAULT NULL COMMENT '主门店名称',
    `competitor_shop_ids` VARCHAR(500) NOT NULL COMMENT 'PK对手门店ID列表（逗号分隔）',
    `competitor_shop_names` VARCHAR(1000) DEFAULT NULL COMMENT 'PK对手门店名称列表（逗号分隔）',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    PRIMARY KEY (`id`),
    KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店PK关系表';
