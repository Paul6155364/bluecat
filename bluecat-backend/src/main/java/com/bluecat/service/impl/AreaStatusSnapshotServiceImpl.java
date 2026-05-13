package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.mapper.AreaStatusSnapshotMapper;
import com.bluecat.service.AreaStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaStatusSnapshotServiceImpl extends ServiceImpl<AreaStatusSnapshotMapper, AreaStatusSnapshot> implements AreaStatusSnapshotService {

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

    @Override
    public Map<Long, Map<String, Map<Integer, Double>>> getHourlyOccupancyByShops(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime) {
        List<AreaStatusSnapshot> list = listByShopIdsAndTimeRange(shopIds, startTime, endTime);

        // 按门店ID -> 日期 -> 小时分组，计算平均上座率
        Map<Long, Map<String, List<BigDecimal>>> tempMap = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AreaStatusSnapshot snapshot : list) {
            if (snapshot.getSnapshotTime() == null || snapshot.getOccupancyRate() == null) {
                continue;
            }
            Long shopId = snapshot.getShopId();
            String date = snapshot.getSnapshotTime().toLocalDate().format(dateFormatter);
            int hour = snapshot.getSnapshotTime().getHour();

            tempMap.computeIfAbsent(shopId, k -> new HashMap<>())
                    .computeIfAbsent(date + "_" + hour, k -> new ArrayList<>())
                    .add(snapshot.getOccupancyRate());
        }

        // 计算平均值
        Map<Long, Map<String, Map<Integer, Double>>> result = new HashMap<>();
        for (Map.Entry<Long, Map<String, List<BigDecimal>>> shopEntry : tempMap.entrySet()) {
            Long shopId = shopEntry.getKey();
            Map<String, Map<Integer, Double>> dateHourMap = new TreeMap<>();

            for (Map.Entry<String, List<BigDecimal>> entry : shopEntry.getValue().entrySet()) {
                String[] parts = entry.getKey().split("_");
                String date = parts[0];
                int hour = Integer.parseInt(parts[1]);

                double avgRate = entry.getValue().stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .average()
                        .orElse(0.0);

                dateHourMap.computeIfAbsent(date, k -> new TreeMap<>())
                        .put(hour, Math.round(avgRate * 10) / 10.0);
            }

            result.put(shopId, dateHourMap);
        }

        return result;
    }
}
