package com.bluecat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.Result;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.entity.MachineInfo;
import com.bluecat.entity.MachineStatusHistory;
import com.bluecat.entity.ShopConfig;
import com.bluecat.entity.ShopInfo;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.service.AreaStatusSnapshotService;
import com.bluecat.service.MachineInfoService;
import com.bluecat.service.MachineStatusHistoryService;
import com.bluecat.service.ShopConfigService;
import com.bluecat.service.ShopInfoService;
import com.bluecat.service.ShopStatusSnapshotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 经营数据分析报告控制器
 *
 * @author BlueCat
 * @since 2026-04-06
 */
@Slf4j
@Api(tags = "经营数据分析报告")
@RestController
@RequestMapping("/business-report")
@RequiredArgsConstructor
public class BusinessReportController {

    private final ShopConfigService shopConfigService;
    private final ShopInfoService shopInfoService;
    private final ShopStatusSnapshotService shopStatusSnapshotService;
    private final AreaStatusSnapshotService areaStatusSnapshotService;
    private final MachineInfoService machineInfoService;
    private final MachineStatusHistoryService machineStatusHistoryService;

    // 最大查询天数限制
    private static final int MAX_QUERY_DAYS = 31;

    /**
     * 获取网咖配置列表
     */
    @ApiOperation("获取网咖配置列表")
    @GetMapping("/configs")
    public Result<List<Map<String, Object>>> listConfigs() {
        List<ShopConfig> configs = shopConfigService.listEnabled();
        List<Map<String, Object>> result = configs.stream().map(config -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", config.getId());
            item.put("configName", config.getConfigName());
            return item;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 根据网咖配置获取门店列表
     */
    @ApiOperation("根据网咖配置获取门店列表")
    @GetMapping("/shops")
    public Result<List<Map<String, Object>>> listShops(@RequestParam Long configId) {
        List<ShopInfo> shops = shopInfoService.listByConfigId(configId);
        List<Map<String, Object>> result = shops.stream().map(shop -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", shop.getId());
            item.put("name", shop.getName());
            item.put("address", shop.getAddress());
            return item;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 生成经营数据分析报告
     */
    @ApiOperation("生成经营数据分析报告")
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateReport(
            @RequestParam Long shopId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("生成经营数据分析报告: shopId={}, startDate={}, endDate={}", shopId, startDate, endDate);

        // 验证门店是否存在
        ShopInfo shop = shopInfoService.getById(shopId);
        if (shop == null) {
            return Result.error("门店不存在");
        }

        // 验证时间范围
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 0) {
            return Result.error("结束日期不能早于开始日期");
        }
        if (days > MAX_QUERY_DAYS) {
            return Result.error("查询时间范围不能超过" + MAX_QUERY_DAYS + "天，当前选择" + days + "天");
        }

        Map<String, Object> report = new LinkedHashMap<>();
        
        // 基本信息
        report.put("shopId", shopId);
        report.put("shopName", shop.getName());
        report.put("startDate", startDate.toString());
        report.put("endDate", endDate.toString());
        report.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.put("days", days);

        // 计算时间范围
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 1. 核心指标（只包含真实数据）
        report.put("kpi", generateKpiData(shopId, startDateTime, endDateTime, days));

        // 2. 每日上座率趋势
        report.put("dailyTrend", generateDailyTrend(shopId, startDateTime, endDateTime));

        // 3. 时段分布
        report.put("hourlyDistribution", generateHourlyDistribution(shopId, startDateTime, endDateTime));

        // 4. 星期分布
        report.put("weeklyDistribution", generateWeeklyDistribution(shopId, startDateTime, endDateTime));

        // 5. 区域经营分析
        report.put("areaAnalysis", generateAreaAnalysis(shopId, startDateTime, endDateTime));

        // 6. 座位热力图
        report.put("seatHeatmap", generateSeatHeatmap(shopId, startDateTime, endDateTime));

        // 7. 数据洞察
        report.put("insights", generateInsights(report));

        return Result.success(report);
    }

    /**
     * 生成核心指标数据 - 只包含真实数据
     */
    private Map<String, Object> generateKpiData(Long shopId, LocalDateTime start, LocalDateTime end, int days) {
        Map<String, Object> kpi = new LinkedHashMap<>();

        // 统计机器数
        LambdaQueryWrapper<MachineInfo> machineWrapper = new LambdaQueryWrapper<>();
        machineWrapper.eq(MachineInfo::getShopId, shopId);
        long totalSeats = machineInfoService.count(machineWrapper);

        // 查询时间范围内的所有快照
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopStatusSnapshot::getShopId, shopId)
               .ge(ShopStatusSnapshot::getSnapshotTime, start)
               .le(ShopStatusSnapshot::getSnapshotTime, end)
               .last("LIMIT 10000");
        
        List<ShopStatusSnapshot> snapshots = shopStatusSnapshotService.list(wrapper);

        if (!snapshots.isEmpty()) {
            // 计算平均上座率
            double avgOccupancyRate = snapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .average()
                    .orElse(0);

            // 计算峰值上座率
            double maxOccupancyRate = snapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .max()
                    .orElse(0);

            // 计算平均占用机器数
            double avgBusyMachines = snapshots.stream()
                    .filter(s -> s.getBusyMachines() != null)
                    .mapToInt(ShopStatusSnapshot::getBusyMachines)
                    .average()
                    .orElse(0);

            // 计算峰值占用机器数
            int maxBusyMachines = snapshots.stream()
                    .filter(s -> s.getBusyMachines() != null)
                    .mapToInt(ShopStatusSnapshot::getBusyMachines)
                    .max()
                    .orElse(0);

            // 快照记录数
            int recordCount = snapshots.size();

            kpi.put("totalSeats", totalSeats);
            kpi.put("avgOccupancyRate", Math.round(avgOccupancyRate * 10) / 10.0);
            kpi.put("maxOccupancyRate", Math.round(maxOccupancyRate * 10) / 10.0);
            kpi.put("avgBusyMachines", Math.round(avgBusyMachines * 10) / 10.0);
            kpi.put("maxBusyMachines", maxBusyMachines);
            kpi.put("recordCount", recordCount);
            kpi.put("dataDays", days);
        } else {
            kpi.put("totalSeats", totalSeats);
            kpi.put("avgOccupancyRate", 0);
            kpi.put("maxOccupancyRate", 0);
            kpi.put("avgBusyMachines", 0);
            kpi.put("maxBusyMachines", 0);
            kpi.put("recordCount", 0);
            kpi.put("dataDays", days);
        }

        return kpi;
    }

    /**
     * 生成每日趋势数据 - 只包含上座率等真实数据
     */
    private Map<String, Object> generateDailyTrend(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        List<Double> avgOccupancyRates = new ArrayList<>();
        List<Double> maxOccupancyRates = new ArrayList<>();
        List<Double> avgBusyMachines = new ArrayList<>();
        List<Integer> recordCounts = new ArrayList<>();

        // 一次性查询所有快照数据
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopStatusSnapshot::getShopId, shopId)
               .ge(ShopStatusSnapshot::getSnapshotTime, start)
               .le(ShopStatusSnapshot::getSnapshotTime, end)
               .last("LIMIT 10000");
        
        List<ShopStatusSnapshot> allSnapshots = shopStatusSnapshotService.list(wrapper);

        // 按日期分组
        Map<LocalDate, List<ShopStatusSnapshot>> snapshotsByDate = allSnapshots.stream()
                .collect(Collectors.groupingBy(s -> s.getSnapshotTime().toLocalDate()));

        // 按天统计
        LocalDate current = start.toLocalDate();
        while (!current.isAfter(end.toLocalDate())) {
            dates.add(current.toString());
            
            List<ShopStatusSnapshot> daySnapshots = snapshotsByDate.getOrDefault(current, Collections.emptyList());

            if (!daySnapshots.isEmpty()) {
                double avgRate = daySnapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                        .average()
                        .orElse(0);
                double maxRate = daySnapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                        .max()
                        .orElse(0);
                double avgBusy = daySnapshots.stream()
                        .filter(s -> s.getBusyMachines() != null)
                        .mapToInt(ShopStatusSnapshot::getBusyMachines)
                        .average()
                        .orElse(0);

                avgOccupancyRates.add(Math.round(avgRate * 10) / 10.0);
                maxOccupancyRates.add(Math.round(maxRate * 10) / 10.0);
                avgBusyMachines.add(Math.round(avgBusy * 10) / 10.0);
                recordCounts.add(daySnapshots.size());
            } else {
                avgOccupancyRates.add(0.0);
                maxOccupancyRates.add(0.0);
                avgBusyMachines.add(0.0);
                recordCounts.add(0);
            }

            current = current.plusDays(1);
        }

        trend.put("dates", dates);
        trend.put("avgOccupancyRates", avgOccupancyRates);
        trend.put("maxOccupancyRates", maxOccupancyRates);
        trend.put("avgBusyMachines", avgBusyMachines);
        trend.put("recordCounts", recordCounts);

        return trend;
    }

    /**
     * 生成时段分布数据
     */
    private Map<String, Object> generateHourlyDistribution(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> distribution = new LinkedHashMap<>();

        // 查询区域快照数据
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AreaStatusSnapshot::getShopId, shopId)
               .ge(AreaStatusSnapshot::getCreateTime, start)
               .le(AreaStatusSnapshot::getCreateTime, end)
               .last("LIMIT 10000");
        
        List<AreaStatusSnapshot> snapshots = areaStatusSnapshotService.list(wrapper);

        // 按小时统计
        List<Integer> hours = new ArrayList<>();
        List<Double> avgOccupancyRates = new ArrayList<>();
        List<Integer> recordCounts = new ArrayList<>();

        for (int h = 0; h < 24; h++) {
            hours.add(h);
            
            final int hour = h;
            List<AreaStatusSnapshot> hourSnapshots = snapshots.stream()
                    .filter(s -> s.getCreateTime().getHour() == hour)
                    .collect(Collectors.toList());

            double avgRate = hourSnapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .average()
                    .orElse(0);

            avgOccupancyRates.add(Math.round(avgRate * 10) / 10.0);
            recordCounts.add(hourSnapshots.size());
        }

        distribution.put("hours", hours);
        distribution.put("avgOccupancyRates", avgOccupancyRates);
        distribution.put("recordCounts", recordCounts);

        return distribution;
    }

    /**
     * 生成星期分布数据
     */
    private Map<String, Object> generateWeeklyDistribution(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> distribution = new LinkedHashMap<>();

        // 查询快照数据
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopStatusSnapshot::getShopId, shopId)
               .ge(ShopStatusSnapshot::getSnapshotTime, start)
               .le(ShopStatusSnapshot::getSnapshotTime, end)
               .last("LIMIT 10000");
        
        List<ShopStatusSnapshot> snapshots = shopStatusSnapshotService.list(wrapper);

        // 按星期统计
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<String> days = Arrays.asList(dayNames);
        List<Double> avgOccupancyRates = new ArrayList<>();
        List<Integer> recordCounts = new ArrayList<>();

        for (int d = 1; d <= 7; d++) {
            final int dayOfWeek = d == 7 ? 0 : d;
            List<ShopStatusSnapshot> daySnapshots = snapshots.stream()
                    .filter(s -> s.getSnapshotTime().getDayOfWeek().getValue() % 7 == dayOfWeek)
                    .collect(Collectors.toList());

            double avgRate = daySnapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .average()
                    .orElse(0);

            avgOccupancyRates.add(Math.round(avgRate * 10) / 10.0);
            recordCounts.add(daySnapshots.size());
        }

        distribution.put("days", days);
        distribution.put("avgOccupancyRates", avgOccupancyRates);
        distribution.put("recordCounts", recordCounts);

        return distribution;
    }

    /**
     * 生成区域分析数据 - 只包含真实数据
     */
    private Map<String, Object> generateAreaAnalysis(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> analysis = new LinkedHashMap<>();

        // 查询区域快照
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AreaStatusSnapshot::getShopId, shopId)
               .ge(AreaStatusSnapshot::getCreateTime, start)
               .le(AreaStatusSnapshot::getCreateTime, end)
               .last("LIMIT 10000");
        
        List<AreaStatusSnapshot> snapshots = areaStatusSnapshotService.list(wrapper);

        // 按区域聚合
        Map<String, List<AreaStatusSnapshot>> areaGroups = snapshots.stream()
                .filter(s -> s.getAreaName() != null)
                .collect(Collectors.groupingBy(AreaStatusSnapshot::getAreaName));

        List<Map<String, Object>> areaList = new ArrayList<>();

        for (Map.Entry<String, List<AreaStatusSnapshot>> entry : areaGroups.entrySet()) {
            String areaName = entry.getKey();
            List<AreaStatusSnapshot> areaSnapshots = entry.getValue();

            double avgRate = areaSnapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .average()
                    .orElse(0);
            double maxRate = areaSnapshots.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .max()
                    .orElse(0);
            double avgBusyMachines = areaSnapshots.stream()
                    .filter(s -> s.getBusyMachines() != null)
                    .mapToInt(AreaStatusSnapshot::getBusyMachines)
                    .average()
                    .orElse(0);
            int totalMachines = areaSnapshots.stream()
                    .filter(s -> s.getTotalMachines() != null)
                    .mapToInt(AreaStatusSnapshot::getTotalMachines)
                    .findFirst()
                    .orElse(0);

            Map<String, Object> area = new LinkedHashMap<>();
            area.put("areaName", areaName);
            area.put("totalMachines", totalMachines);
            area.put("avgOccupancyRate", Math.round(avgRate * 10) / 10.0);
            area.put("maxOccupancyRate", Math.round(maxRate * 10) / 10.0);
            area.put("avgBusyMachines", Math.round(avgBusyMachines * 10) / 10.0);
            area.put("recordCount", areaSnapshots.size());

            areaList.add(area);
        }

        // 按平均上座率排序
        areaList.sort((a, b) -> Double.compare(
                (Double) b.get("avgOccupancyRate"), 
                (Double) a.get("avgOccupancyRate")));

        // 添加排名
        for (int i = 0; i < areaList.size(); i++) {
            areaList.get(i).put("rank", i + 1);
        }

        analysis.put("areas", areaList);

        return analysis;
    }

    /**
     * 生成座位热力图数据
     */
    private Map<String, Object> generateSeatHeatmap(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> heatmap = new LinkedHashMap<>();

        // 获取机器列表
        LambdaQueryWrapper<MachineInfo> machineWrapper = new LambdaQueryWrapper<>();
        machineWrapper.eq(MachineInfo::getShopId, shopId);
        List<MachineInfo> machines = machineInfoService.list(machineWrapper);

        // 获取区域列表
        List<String> areas = machines.stream()
                .map(MachineInfo::getAreaName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 获取座位列表
        List<String> seats = machines.stream()
                .map(MachineInfo::getComName)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        // 查询机器状态历史
        LambdaQueryWrapper<MachineStatusHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(MachineStatusHistory::getShopId, shopId)
                      .ge(MachineStatusHistory::getSnapshotTime, start)
                      .le(MachineStatusHistory::getSnapshotTime, end)
                      .last("LIMIT 5000");
        List<MachineStatusHistory> histories = machineStatusHistoryService.list(historyWrapper);

        // 按机器统计占用次数
        Map<String, Long> machineBusyCount = histories.stream()
                .filter(h -> h.getStatus() != null && h.getStatus() == 0)
                .filter(h -> h.getComName() != null)
                .collect(Collectors.groupingBy(MachineStatusHistory::getComName, Collectors.counting()));

        // 生成热力图数据
        List<List<Object>> heatmapData = new ArrayList<>();
        for (MachineInfo machine : machines) {
            if (machine.getComName() == null || machine.getAreaName() == null) continue;
            
            int seatIndex = seats.indexOf(machine.getComName());
            int areaIndex = areas.indexOf(machine.getAreaName());
            if (seatIndex < 0 || areaIndex < 0) continue;
            
            long busyCount = machineBusyCount.getOrDefault(machine.getComName(), 0L);
            heatmapData.add(Arrays.asList(seatIndex, areaIndex, busyCount));
        }

        heatmap.put("areas", areas);
        heatmap.put("seats", seats);
        heatmap.put("data", heatmapData);

        return heatmap;
    }

    /**
     * 生成数据洞察
     */
    private List<Map<String, Object>> generateInsights(Map<String, Object> report) {
        List<Map<String, Object>> insights = new ArrayList<>();

        // 分析核心指标
        @SuppressWarnings("unchecked")
        Map<String, Object> kpi = (Map<String, Object>) report.get("kpi");
        if (kpi != null) {
            double avgOccupancyRate = ((Number) kpi.getOrDefault("avgOccupancyRate", 0)).doubleValue();
            
            // 洞察1: 上座率评价
            Map<String, Object> insight1 = new LinkedHashMap<>();
            if (avgOccupancyRate >= 70) {
                insight1.put("type", "good");
                insight1.put("title", "上座率表现优秀");
                insight1.put("content", String.format("平均上座率 %.1f%%，处于较高水平，门店运营状况良好。", avgOccupancyRate));
            } else if (avgOccupancyRate >= 50) {
                insight1.put("type", "info");
                insight1.put("title", "上座率有待提升");
                insight1.put("content", String.format("平均上座率 %.1f%%，建议优化服务吸引更多顾客。", avgOccupancyRate));
            } else if (avgOccupancyRate > 0) {
                insight1.put("type", "warn");
                insight1.put("title", "上座率偏低");
                insight1.put("content", String.format("平均上座率 %.1f%%，需要重点关注运营策略。", avgOccupancyRate));
            } else {
                insight1.put("type", "warn");
                insight1.put("title", "暂无数据");
                insight1.put("content", "该时间段内暂无上座率数据记录。");
            }
            insights.add(insight1);

            // 洞察2: 机器配置
            long totalSeats = ((Number) kpi.getOrDefault("totalSeats", 0L)).longValue();
            if (totalSeats > 0) {
                Map<String, Object> insight2 = new LinkedHashMap<>();
                insight2.put("type", "info");
                insight2.put("title", "座位资源配置");
                insight2.put("content", String.format("门店共有 %d 个座位。", totalSeats));
                insights.add(insight2);
            }
        }

        // 分析时段分布
        @SuppressWarnings("unchecked")
        Map<String, Object> hourlyDist = (Map<String, Object>) report.get("hourlyDistribution");
        if (hourlyDist != null) {
            @SuppressWarnings("unchecked")
            List<Double> avgRates = (List<Double>) hourlyDist.get("avgOccupancyRates");
            if (avgRates != null && !avgRates.isEmpty()) {
                double maxRate = Collections.max(avgRates);
                int peakHour = avgRates.indexOf(maxRate);
                
                if (maxRate > 0) {
                    Map<String, Object> insight = new LinkedHashMap<>();
                    insight.put("type", "info");
                    insight.put("title", "高峰时段分析");
                    insight.put("content", String.format("%d:00 是每日高峰时段，上座率达到 %.1f%%。", peakHour, maxRate));
                    insights.add(insight);
                }
            }
        }

        // 分析区域数据
        @SuppressWarnings("unchecked")
        Map<String, Object> areaAnalysis = (Map<String, Object>) report.get("areaAnalysis");
        if (areaAnalysis != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> areas = (List<Map<String, Object>>) areaAnalysis.get("areas");
            if (areas != null && !areas.isEmpty()) {
                Map<String, Object> topArea = areas.get(0);
                double topRate = (Double) topArea.get("avgOccupancyRate");
                if (topRate > 0) {
                    Map<String, Object> insight = new LinkedHashMap<>();
                    insight.put("type", "good");
                    insight.put("title", "热门区域分析");
                    insight.put("content", String.format("「%s」区域上座率最高，平均达到 %.1f%%。", 
                            topArea.get("areaName"), topRate));
                    insights.add(insight);
                }
            }
        }

        return insights;
    }
}
