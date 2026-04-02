package com.bluecat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.Result;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.entity.MachineStatusHistory;
import com.bluecat.entity.ShopInfo;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.service.AreaStatusSnapshotService;
import com.bluecat.service.MachineStatusHistoryService;
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
import java.time.format.DateTimeFormatter;
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
    private final MachineStatusHistoryService machineStatusHistoryService;
    private final AreaStatusSnapshotService areaStatusSnapshotService;

    /**
     * 门店PK对比数据
     */
    @ApiOperation("门店PK对比数据")
    @GetMapping("/pk/shops")
    public Result<Map<String, Object>> pkShops(
            @RequestParam List<Long> shopIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> shopDataList = new ArrayList<>();
        
        for (Long shopId : shopIds) {
            ShopInfo shop = shopInfoService.getById(shopId);
            if (shop == null) continue;
            
            Map<String, Object> shopData = new HashMap<>();
            shopData.put("shopId", shopId);
            shopData.put("shopName", shop.getName());
            
            // 获取最新快照
            ShopStatusSnapshot latestSnapshot = shopStatusSnapshotService.getLatestByShopId(shopId);
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
            
            // 获取历史数据计算平均值
            LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShopStatusSnapshot::getShopId, shopId);
            if (startTime != null) {
                wrapper.ge(ShopStatusSnapshot::getSnapshotTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(ShopStatusSnapshot::getSnapshotTime, endTime);
            }
            wrapper.orderByDesc(ShopStatusSnapshot::getSnapshotTime).last("LIMIT 100");
            
            List<ShopStatusSnapshot> snapshots = shopStatusSnapshotService.list(wrapper);
            if (!snapshots.isEmpty()) {
                double avgRate = snapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                        .average()
                        .orElse(0);
                int maxRate = snapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToInt(s -> s.getOccupancyRate().intValue())
                        .max()
                        .orElse(0);
                int minRate = snapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToInt(s -> s.getOccupancyRate().intValue())
                        .min()
                        .orElse(0);
                
                shopData.put("avgOccupancyRate", Math.round(avgRate * 10) / 10.0);
                shopData.put("maxOccupancyRate", maxRate);
                shopData.put("minOccupancyRate", minRate);
                
                // 趋势数据
                List<Map<String, Object>> trend = new ArrayList<>();
                for (int i = Math.min(snapshots.size() - 1, 19); i >= 0; i--) {
                    ShopStatusSnapshot s = snapshots.get(i);
                    Map<String, Object> point = new HashMap<>();
                    point.put("time", s.getSnapshotTime());
                    point.put("rate", s.getOccupancyRate());
                    trend.add(point);
                }
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
        
        result.put("shops", shopDataList);
        result.put("rankTime", LocalDateTime.now());
        
        return Result.success(result);
    }

    /**
     * 高峰时段热力图数据
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
        
        // 查询机器状态历史数据
        LambdaQueryWrapper<MachineStatusHistory> wrapper = new LambdaQueryWrapper<>();
        if (shopId != null) {
            wrapper.eq(MachineStatusHistory::getShopId, shopId);
        }
        wrapper.ge(MachineStatusHistory::getSnapshotTime, startDateTime)
              .le(MachineStatusHistory::getSnapshotTime, endDateTime)
              .select(MachineStatusHistory::getShopId, MachineStatusHistory::getAreaName,
                      MachineStatusHistory::getStatus, MachineStatusHistory::getSnapshotTime);
        
        List<MachineStatusHistory> historyList = machineStatusHistoryService.list(wrapper);
        
        // 按小时统计
        // hours: 0-23, days: 日期列表
        // [空闲次数, 占用次数] -> status: 1=空闲, 0=占用
        int[][] hourData = new int[24][2]; // [空闲次数, 占用次数]
        
        // 按日期+小时统计
        Map<String, Map<Integer, int[]>> dailyHourData = new TreeMap<>();
        
        for (MachineStatusHistory h : historyList) {
            int hour = h.getSnapshotTime().getHour();
            String dateKey = h.getSnapshotTime().toLocalDate().toString();
            
            // status: 1=空闲(索引0), 0=占用(索引1)
            int statusIndex = (h.getStatus() != null && h.getStatus() == 1) ? 0 : 1;
            hourData[hour][statusIndex]++;
            
            dailyHourData.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .computeIfAbsent(hour, k -> new int[2])[statusIndex]++;
        }
        
        // 计算每小时平均上座率
        List<Map<String, Object>> hourlyRate = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            int total = hourData[h][0] + hourData[h][1];
            int rate = total > 0 ? (hourData[h][1] * 100 / total) : 0;
            
            Map<String, Object> item = new HashMap<>();
            item.put("hour", h);
            item.put("hourLabel", String.format("%02d:00", h));
            item.put("rate", rate);
            item.put("total", total);
            item.put("busy", hourData[h][1]);
            item.put("free", hourData[h][0]);
            hourlyRate.add(item);
        }
        
        // 热力图数据 [日期, 小时, 上座率]
        List<List<Object>> heatmapData = new ArrayList<>();
        List<String> dateList = new ArrayList<>(dailyHourData.keySet());
        
        for (int dayIndex = 0; dayIndex < dateList.size(); dayIndex++) {
            String dateKey = dateList.get(dayIndex);
            Map<Integer, int[]> dayData = dailyHourData.get(dateKey);
            
            for (int h = 0; h < 24; h++) {
                int[] counts = dayData.getOrDefault(h, new int[2]);
                int total = counts[0] + counts[1];
                int rate = total > 0 ? (counts[1] * 100 / total) : 0;
                heatmapData.add(Arrays.asList(dayIndex, h, rate));
            }
        }
        
        // 按区域统计
        Map<String, int[]> areaData = new HashMap<>();
        for (MachineStatusHistory h : historyList) {
            String areaName = h.getAreaName() != null ? h.getAreaName() : "未知区域";
            // status: 1=空闲(索引0), 0=占用(索引1)
            int statusIndex = (h.getStatus() != null && h.getStatus() == 1) ? 0 : 1;
            areaData.computeIfAbsent(areaName, k -> new int[2])[statusIndex]++;
        }
        
        List<Map<String, Object>> areaRate = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : areaData.entrySet()) {
            int total = entry.getValue()[0] + entry.getValue()[1];
            int rate = total > 0 ? (entry.getValue()[1] * 100 / total) : 0;
            
            Map<String, Object> item = new HashMap<>();
            item.put("areaName", entry.getKey());
            item.put("rate", rate);
            item.put("total", total);
            item.put("busy", entry.getValue()[1]);
            areaRate.add(item);
        }
        areaRate.sort((a, b) -> ((Integer) b.get("rate")).compareTo((Integer) a.get("rate")));
        
        // 日历视图数据 (类似GitHub贡献图)
        List<Map<String, Object>> calendarData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        Map<String, Integer> dailyTotal = new HashMap<>();
        Map<String, Integer> dailyBusy = new HashMap<>();
        
        for (MachineStatusHistory h : historyList) {
            String dateKey = h.getSnapshotTime().toLocalDate().toString();
            dailyTotal.merge(dateKey, 1, Integer::sum);
            // status: 1=空闲, 0=占用, 统计占用数
            if (h.getStatus() != null && h.getStatus() == 0) {
                dailyBusy.merge(dateKey, 1, Integer::sum);
            }
        }
        
        for (Map.Entry<String, Integer> entry : dailyTotal.entrySet()) {
            int total = entry.getValue();
            int busy = dailyBusy.getOrDefault(entry.getKey(), 0);
            int rate = total > 0 ? (busy * 100 / total) : 0;
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("rate", rate);
            item.put("level", getLevel(rate));
            calendarData.add(item);
        }
        calendarData.sort(Comparator.comparing(m -> (String) m.get("date")));
        
        result.put("hourlyRate", hourlyRate);
        result.put("heatmapData", heatmapData);
        result.put("dateList", dateList);
        result.put("areaRate", areaRate);
        result.put("calendarData", calendarData);
        result.put("totalRecords", historyList.size());
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
     * 门店列表 (用于选择PK门店)
     */
    @ApiOperation("门店列表(简略)")
    @GetMapping("/shops")
    public Result<List<Map<String, Object>>> listShops() {
        List<ShopInfo> shops = shopInfoService.list(
                new LambdaQueryWrapper<ShopInfo>().select(ShopInfo::getId, ShopInfo::getName)
        );
        
        List<Map<String, Object>> result = shops.stream().map(shop -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", shop.getId());
            item.put("name", shop.getName());
            return item;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}
