package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.AreaStatusSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 区域实时状态快照表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface AreaStatusSnapshotMapper extends BaseMapper<AreaStatusSnapshot> {

    /**
     * SQL层聚合：批量查询门店在指定时间范围内的每小时平均上座率
     * 返回字段: shop_id, date(日期), hour(小时), avg_rate(平均上座率)
     * <p>
     * 数据量对比：
     * - 原方案：一次性查出全部原始记录（可能数十万条）到 JVM 再 Java 聚合
     * - 新方案：数据库 GROUP BY 后只返回聚合结果（最多 shopIds.size * 日期数 * 24 条）
     */
    List<Map<String, Object>> selectHourlyOccupancyGrouped(
            @Param("shopIds") List<Long> shopIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
