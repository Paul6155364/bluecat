-- 为 area_status_snapshot 表添加 snapshot_time 字段
ALTER TABLE area_status_snapshot 
ADD COLUMN snapshot_time DATETIME COMMENT '快照时间' AFTER occupancy_rate;

-- 创建索引
ALTER TABLE area_status_snapshot 
ADD INDEX idx_snapshot_time (snapshot_time);

-- 将现有的 create_time 数据复制到 snapshot_time（临时修复历史数据）
UPDATE area_status_snapshot 
SET snapshot_time = create_time 
WHERE snapshot_time IS NULL;
