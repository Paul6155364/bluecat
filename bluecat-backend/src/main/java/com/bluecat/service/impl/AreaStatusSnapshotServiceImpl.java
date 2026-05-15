package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.mapper.AreaStatusSnapshotMapper;
import com.bluecat.service.AreaStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 区域实时状态快照表 Service 实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class AreaStatusSnapshotServiceImpl extends ServiceImpl<AreaStatusSnapshotMapper, AreaStatusSnapshot>
        implements AreaStatusSnapshotService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<AreaStatusSnapshot> listBySnapshotId(Long snapshotId) {
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<AreaStatusSnapshot>()
                .eq(AreaStatusSnapshot::getSnapshotId, snapshotId)
                .orderByAsc(AreaStatusSnapshot::getAreaName);
        return list(wrapper);
    }

    @Override
    public List<AreaStatusSnapshot> listByShopIdsAndTimeRange(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<AreaStatusSnapshot>()
                .in(AreaStatusSnapshot::getShopId, shopIds)
                .ge(startTime != null, AreaStatusSnapshot::getSnapshotTime, startTime)
                .le(endTime != null, AreaStatusSnapshot::getSnapshotTime, endTime)
                .orderByAsc(AreaStatusSnapshot::getShopId, AreaStatusSnapshot::getSnapshotTime);
        return list(wrapper);
    }

    /**
     * 获取门店每日24小时上座率数据（SQL层聚合）
     * <p>
     * 优化说明：
     * - 旧方案：查出全部原始记录到 JVM，再 Java 循环分组聚合。
     *          数据量大时（数十万条）会造成 OOM、GC 压力、响应慢。
     * - 新方案：通过 SQL GROUP BY 直接在数据库层聚合，只返回聚合后的结果集。
     *          返回量级：shopIds.size × 天数 × 24，即使一年数据也只有 ~8,760 条/门店。
     */
    @Override
    public Map<Long, Map<String, Map<Integer, Double>>> getHourlyOccupancyByShops(
            List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime) {

        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }

        // 数据库层 SQL 聚合
        List<Map<String, Object>> rows = baseMapper.selectHourlyOccupancyGrouped(shopIds, startTime, endTime);

        // 按 门店ID -> 日期 -> 小时 构建结果
        Map<Long, Map<String, Map<Integer, Double>>> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Long shopId = ((Number) row.get("shopId")).longValue();
            String date = row.get("date").toString();
            int hour = ((Number) row.get("hour")).intValue();
            double avgRate = ((Number) row.get("avgRate")).doubleValue();

            result.computeIfAbsent(shopId, k -> new TreeMap<>())
                  .computeIfAbsent(date, k -> new TreeMap<>())
                  .put(hour, avgRate);
        }

        return result;
    }
}
