package com.bluecat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.Result;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.entity.ShopInfo;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.service.AreaStatusSnapshotService;
import com.bluecat.service.ShopInfoService;
import com.bluecat.service.ShopStatusSnapshotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析控制器 - PK竞技场 & 热力图
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "数据分析")
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final ShopStatusSnapshotService shopStatusSnapshotService;
    private final ShopInfoService shopInfoService;
    private final AreaStatusSnapshotService areaStatusSnapshotService;

    /**
     * 门店PK对比数据
     */
    @ApiOperation("门店PK对比数据")
    @GetMapping("/pk/shops")
    public Result<Map<String, Object>> pkShops(
            @RequestParam String shopIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        // 解析逗号分隔的 shopIds 字符串
        List<Long> shopIdList = Arrays.stream(shopIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (shopIdList.isEmpty()) {
            return Result.success(new HashMap<>());
        }

        // 1. 批量查询门店信息
        Map<Long, ShopInfo> shopMap = shopInfoService.listByIds(shopIdList).stream()
                .collect(Collectors.toMap(ShopInfo::getId, s -> s));

        // 2. 批量查询最新快照
        Map<Long, ShopStatusSnapshot> latestMap = shopStatusSnapshotService.mapLatestByShopIds(shopIdList);

        // 3. 批量查询历史快照
        Map<Long, List<ShopStatusSnapshot>> historyMap = shopStatusSnapshotService
                .mapByShopIdsAndTimeRange(shopIdList, startTime, endTime);

        // 组装数据
        List<Map<String, Object>> shopDataList = new ArrayList<>();
        for (Long shopId : shopIdList) {
            ShopInfo shop = shopMap.get(shopId);
            if (shop == null) continue;

            Map<String, Object> shopData = new HashMap<>();
            shopData.put("shopId", shopId);
            shopData.put("shopName", shop.getName());

            // 最新快照
            ShopStatusSnapshot latestSnapshot = latestMap.get(shopId);
            if (latestSnapshot != null) {
                shopData.put("totalMachines", latestSnapshot.getTotalMachines());
                shopData.put("freeMachines", latestSnapshot.getFreeMachines());
                shopData.put("busyMachines", latestSnapshot.getBusyMachines());
                shopData.put("occupancyRate", latestSnapshot.getOccupancyRate());
            } else {
                shopData.put("totalMachines", 0);
                shopData.put("freeMachines", 0);
                shopData.put("busyMachines", 0);
                shopData.put("occupancyRate", 0);
            }

            // 历史数据计算平均值
            List<ShopStatusSnapshot> snapshots = historyMap.getOrDefault(shopId, Collections.emptyList());
            if (!snapshots.isEmpty()) {
                List<ShopStatusSnapshot> validSnapshots = snapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .collect(Collectors.toList());

                double avgRate = validSnapshots.stream()
                        .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                        .average()
                        .orElse(0);

                int maxRate = validSnapshots.stream()
                        .mapToInt(s -> s.getOccupancyRate().intValue())
                        .max()
                        .orElse(0);

                int minRate = validSnapshots.stream()
                        .mapToInt(s -> s.getOccupancyRate().intValue())
                        .min()
                        .orElse(0);

                shopData.put("avgOccupancyRate", Math.round(avgRate * 10) / 10.0);
                shopData.put("maxOccupancyRate", maxRate);
                shopData.put("minOccupancyRate", minRate);

                // 趋势数据：按天采样，每天取平均值
                List<Map<String, Object>> trend = validSnapshots.stream()
                        .collect(Collectors.groupingBy(s -> s.getSnapshotTime().toLocalDate()))
                        .entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> {
                            double dayAvg = entry.getValue().stream()
                                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                                    .average()
                                    .orElse(0);
                            Map<String, Object> point = new HashMap<>();
                            point.put("time", entry.getKey().atStartOfDay());
                            point.put("rate", (int) Math.round(dayAvg));
                            return point;
                        })
                        .collect(Collectors.toList());
                shopData.put("trend", trend);
            } else {
                shopData.put("avgOccupancyRate", 0);
                shopData.put("maxOccupancyRate", 0);
                shopData.put("minOccupancyRate", 0);
                shopData.put("trend", Collections.emptyList());
            }

            shopDataList.add(shopData);
        }

        // 排序
        shopDataList.sort((a, b) -> {
            Object rateA = a.get("occupancyRate");
            Object rateB = b.get("occupancyRate");
            int valA = rateA instanceof BigDecimal ? ((BigDecimal) rateA).intValue() : (rateA instanceof Integer ? (Integer) rateA : 0);
            int valB = rateB instanceof BigDecimal ? ((BigDecimal) rateB).intValue() : (rateB instanceof Integer ? (Integer) rateB : 0);
            return Integer.compare(valB, valA);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("shops", shopDataList);
        result.put("rankTime", LocalDateTime.now());

        return Result.success(result);
    }

    /**
     * 高峰时段热力图数据
     * 使用 AreaStatusSnapshot 表，该表已包含每个区域每个时间点的聚合数据
     */
    @ApiOperation("高峰时段热力图数据")
    @GetMapping("/heatmap")
    public Result<Map<String, Object>> heatmap(
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Map<String, Object> result = new HashMap<>();

        // 默认查询最近7天
        LocalDateTime endDateTime;
        LocalDateTime startDateTime;
        if (endDate == null) {
            endDateTime = LocalDateTime.now();
        } else {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }
        if (startDate == null) {
            startDateTime = endDateTime.minusDays(7);
        } else {
            startDateTime = startDate.atStartOfDay();
        }

        // 使用 AreaStatusSnapshot 表查询，该表已包含聚合好的数据
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        if (shopId != null) {
            wrapper.eq(AreaStatusSnapshot::getShopId, shopId);
        }
        wrapper.ge(AreaStatusSnapshot::getSnapshotTime, startDateTime)
              .le(AreaStatusSnapshot::getSnapshotTime, endDateTime);

        List<AreaStatusSnapshot> snapshotList = areaStatusSnapshotService.list(wrapper);

        // 按小时统计 - 每个快照计算一次上座率
        // Map<小时, List<上座率>>
        Map<Integer, List<Integer>> hourRateMap = new HashMap<>();

        // 按日期+小时统计
        Map<String, Map<Integer, List<Integer>>> dailyHourRateMap = new TreeMap<>();

        // 按区域统计
        Map<String, List<Integer>> areaRateMap = new HashMap<>();

        // 日历数据
        Map<String, List<Integer>> dailyRateMap = new HashMap<>();

        for (AreaStatusSnapshot snapshot : snapshotList) {
            LocalDateTime snapshotTime = snapshot.getSnapshotTime() != null
                ? snapshot.getSnapshotTime()
                : snapshot.getCreateTime();
            int hour = snapshotTime.getHour();
            String dateKey = snapshotTime.toLocalDate().toString();
            int rate = snapshot.getOccupancyRate() != null ? snapshot.getOccupancyRate().intValue() : 0;
            String areaName = snapshot.getAreaName() != null ? snapshot.getAreaName() : "未知区域";

            // 按小时收集上座率
            hourRateMap.computeIfAbsent(hour, k -> new ArrayList<>()).add(rate);

            // 按日期+小时收集上座率
            dailyHourRateMap.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .computeIfAbsent(hour, k -> new ArrayList<>()).add(rate);

            // 按区域收集上座率
            areaRateMap.computeIfAbsent(areaName, k -> new ArrayList<>()).add(rate);

            // 日历数据
            dailyRateMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(rate);
        }

        // 计算每小时平均上座率
        List<Map<String, Object>> hourlyRate = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            List<Integer> rates = hourRateMap.getOrDefault(h, Collections.emptyList());
            int avgRate = rates.isEmpty() ? 0 : (int) rates.stream().mapToInt(Integer::intValue).average().orElse(0);

            Map<String, Object> item = new HashMap<>();
            item.put("hour", h);
            item.put("hourLabel", String.format("%02d:00", h));
            item.put("rate", avgRate);
            item.put("total", rates.size()); // 快照数量
            item.put("busy", (int) rates.stream().mapToInt(Integer::intValue).average().orElse(0));
            item.put("free", 100 - avgRate);
            hourlyRate.add(item);
        }

        // 热力图数据 [日期, 小时, 上座率]
        List<List<Object>> heatmapData = new ArrayList<>();
        List<String> dateList = new ArrayList<>(dailyHourRateMap.keySet());

        for (int dayIndex = 0; dayIndex < dateList.size(); dayIndex++) {
            String dateKey = dateList.get(dayIndex);
            Map<Integer, List<Integer>> dayData = dailyHourRateMap.get(dateKey);

            for (int h = 0; h < 24; h++) {
                List<Integer> rates = dayData.getOrDefault(h, Collections.emptyList());
                int avgRate = rates.isEmpty() ? 0 : (int) rates.stream().mapToInt(Integer::intValue).average().orElse(0);
                heatmapData.add(Arrays.asList(dayIndex, h, avgRate));
            }
        }

        // 按区域计算平均上座率
        List<Map<String, Object>> areaRate = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : areaRateMap.entrySet()) {
            List<Integer> rates = entry.getValue();
            int avgRate = (int) rates.stream().mapToInt(Integer::intValue).average().orElse(0);

            Map<String, Object> item = new HashMap<>();
            item.put("areaName", entry.getKey());
            item.put("rate", avgRate);
            item.put("total", rates.size()); // 快照数量
            item.put("busy", avgRate);
            areaRate.add(item);
        }
        areaRate.sort((a, b) -> ((Integer) b.get("rate")).compareTo((Integer) a.get("rate")));

        // 日历视图数据 (类似GitHub贡献图)
        List<Map<String, Object>> calendarData = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : dailyRateMap.entrySet()) {
            List<Integer> rates = entry.getValue();
            int avgRate = (int) rates.stream().mapToInt(Integer::intValue).average().orElse(0);

            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("rate", avgRate);
            item.put("level", getLevel(avgRate));
            calendarData.add(item);
        }
        calendarData.sort(Comparator.comparing(m -> (String) m.get("date")));

        result.put("hourlyRate", hourlyRate);
        result.put("heatmapData", heatmapData);
        result.put("dateList", dateList);
        result.put("areaRate", areaRate);
        result.put("calendarData", calendarData);
        result.put("totalRecords", snapshotList.size());
        result.put("startDate", startDateTime.toLocalDate().toString());
        result.put("endDate", endDateTime.toLocalDate().toString());

        return Result.success(result);
    }

    /**
     * 获取热力等级 (0-4)
     */
    private int getLevel(int rate) {
        if (rate == 0) return 0;
        if (rate < 20) return 1;
        if (rate < 40) return 2;
        if (rate < 60) return 3;
        if (rate < 80) return 4;
        return 5;
    }

    /**
     * 门店24小时上座率对比数据
     * 用于PK关系门店的每日24小时对比表格
     */
    @ApiOperation("门店24小时上座率对比")
    @GetMapping("/pk/hourly")
    public Result<Map<String, Object>> pkHourly(
            @RequestParam String shopIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 解析门店ID列表
        List<Long> shopIdList = Arrays.stream(shopIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (shopIdList.isEmpty()) {
            return Result.success(new HashMap<>());
        }

        // 默认查询最近7天
        LocalDateTime endDateTime;
        LocalDateTime startDateTime;
        if (endDate == null) {
            endDateTime = LocalDateTime.now();
        } else {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }
        if (startDate == null) {
            startDateTime = endDateTime.minusDays(7).with(LocalTime.MIN);
        } else {
            startDateTime = startDate.atStartOfDay();
        }

        // 批量查询门店信息
        Map<Long, ShopInfo> shopMap = shopInfoService.listByIds(shopIdList).stream()
                .collect(Collectors.toMap(ShopInfo::getId, s -> s));

        // 获取每小时上座率数据
        Map<Long, Map<String, Map<Integer, Double>>> hourlyData = areaStatusSnapshotService
                .getHourlyOccupancyByShops(shopIdList, startDateTime, endDateTime);

        // 生成日期列表
        List<String> dateList = new ArrayList<>();
        LocalDate current = startDateTime.toLocalDate();
        LocalDate end = endDateTime.toLocalDate();
        while (!current.isAfter(end)) {
            dateList.add(current.toString());
            current = current.plusDays(1);
        }

        // 组装返回数据
        List<Map<String, Object>> shopList = new ArrayList<>();
        for (Long shopId : shopIdList) {
            ShopInfo shop = shopMap.get(shopId);
            if (shop == null) continue;

            Map<String, Object> shopData = new HashMap<>();
            shopData.put("shopId", shopId);
            shopData.put("shopName", shop.getName());

            Map<String, Map<Integer, Double>> dateHourMap = hourlyData.getOrDefault(shopId, new HashMap<>());

            // 每天的24小时数据
            List<Map<String, Object>> dailyData = new ArrayList<>();
            for (String date : dateList) {
                Map<Integer, Double> hourMap = dateHourMap.getOrDefault(date, new HashMap<>());

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date);

                // 24小时的上座率
                List<Map<String, Object>> hours = new ArrayList<>();
                for (int h = 0; h < 24; h++) {
                    Map<String, Object> hourData = new HashMap<>();
                    hourData.put("hour", h);
                    hourData.put("hourLabel", String.format("%02d:00", h));
                    hourData.put("rate", hourMap.getOrDefault(h, 0.0));
                    hours.add(hourData);
                }
                dayData.put("hours", hours);

                // 当天平均值
                double dayAvg = hourMap.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                dayData.put("dayAvg", Math.round(dayAvg * 10) / 10.0);

                dailyData.add(dayData);
            }
            shopData.put("dailyData", dailyData);
            shopList.add(shopData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("shops", shopList);
        result.put("dateList", dateList);
        result.put("startDate", startDateTime.toLocalDate().toString());
        result.put("endDate", endDateTime.toLocalDate().toString());

        return Result.success(result);
    }

    /**
     * 门店列表 (用于选择PK门店)
     */
    @ApiOperation("门店列表(简略)")
    @GetMapping("/shops")
    public Result<List<Map<String, Object>>> listShops() {
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<ShopInfo>()
                .select(ShopInfo::getId, ShopInfo::getName);

        List<ShopInfo> shops = shopInfoService.list(wrapper);

        List<Map<String, Object>> result = shops.stream().map(shop -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", shop.getId());
            item.put("name", shop.getName());
            return item;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
