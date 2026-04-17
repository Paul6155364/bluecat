-- ============================================
-- 检查24小时上座率数据分布情况
-- ============================================

-- 1. 查看每个时段的数据量和平均上座率
SELECT 
    HOUR(snapshot_time) as '时段',
    COUNT(*) as '数据量',
    COUNT(DISTINCT DATE(snapshot_time)) as '采集天数',
    ROUND(COUNT(*) / COUNT(DISTINCT DATE(snapshot_time)), 1) as '每天平均数据量',
    ROUND(AVG(occupancy_rate), 1) as '平均上座率(%)',
    ROUND(MIN(occupancy_rate), 1) as '最低上座率(%)',
    ROUND(MAX(occupancy_rate), 1) as '最高上座率(%)',
    ROUND(STDDEV(occupancy_rate), 1) as '标准差'
FROM area_status_snapshot
WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND snapshot_time IS NOT NULL
GROUP BY HOUR(snapshot_time)
ORDER BY HOUR(snapshot_time);

-- 2. 查看最近7天每天的采集情况
SELECT 
    DATE(snapshot_time) as '日期',
    HOUR(snapshot_time) as '时段',
    COUNT(*) as '数据量',
    ROUND(AVG(occupancy_rate), 1) as '平均上座率(%)'
FROM area_status_snapshot
WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND snapshot_time IS NOT NULL
GROUP BY DATE(snapshot_time), HOUR(snapshot_time)
ORDER BY DATE(snapshot_time) DESC, HOUR(snapshot_time);

-- 3. 检查是否有数据缺失的时段
SELECT 
    DATE(snapshot_time) as '日期',
    GROUP_CONCAT(DISTINCT HOUR(snapshot_time) ORDER BY HOUR(snapshot_time)) as '已采集时段',
    COUNT(DISTINCT HOUR(snapshot_time)) as '采集时段数'
FROM area_status_snapshot
WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND snapshot_time IS NOT NULL
GROUP BY DATE(snapshot_time)
ORDER BY DATE(snapshot_time) DESC;

-- 4. 对比工作日和周末的上座率差异
SELECT 
    CASE 
        WHEN DAYOFWEEK(snapshot_time) IN (1, 7) THEN '周末'
        ELSE '工作日'
    END as '日期类型',
    HOUR(snapshot_time) as '时段',
    COUNT(*) as '数据量',
    ROUND(AVG(occupancy_rate), 1) as '平均上座率(%)'
FROM area_status_snapshot
WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND snapshot_time IS NOT NULL
GROUP BY 
    CASE 
        WHEN DAYOFWEEK(snapshot_time) IN (1, 7) THEN '周末'
        ELSE '工作日'
    END,
    HOUR(snapshot_time)
ORDER BY 日期类型 DESC, 时段;

-- 5. 查看具体的某个时段的详细数据（比如14时和20时）
SELECT 
    snapshot_time as '快照时间',
    shop_id as '门店ID',
    area_name as '区域',
    total_machines as '总机器',
    free_machines as '空闲',
    busy_machines as '占用',
    occupancy_rate as '上座率(%)'
FROM area_status_snapshot
WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
  AND HOUR(snapshot_time) IN (14, 20)
ORDER BY snapshot_time DESC, area_name;

-- 6. 统计数据采集的时间分布（检查是否有采集空白时段）
WITH hours AS (
    SELECT 0 as hour UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION
    SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION
    SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION
    SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION
    SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23
),
recent_data AS (
    SELECT 
        HOUR(snapshot_time) as hour,
        COUNT(*) as count
    FROM area_status_snapshot
    WHERE snapshot_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
      AND snapshot_time IS NOT NULL
    GROUP BY HOUR(snapshot_time)
)
SELECT 
    h.hour as '时段',
    CONCAT(h.hour, ':00') as '时间',
    COALESCE(r.count, 0) as '数据量',
    CASE 
        WHEN COALESCE(r.count, 0) = 0 THEN '⚠️ 无数据'
        WHEN COALESCE(r.count, 0) < 10 THEN '⚠️ 数据较少'
        ELSE '✅ 数据充足'
    END as '状态'
FROM hours h
LEFT JOIN recent_data r ON h.hour = r.hour
ORDER BY h.hour;
