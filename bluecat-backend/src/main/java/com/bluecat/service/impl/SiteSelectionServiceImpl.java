package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.dto.NearbyShopDTO;
import com.bluecat.dto.SiteSelectionRequest;
import com.bluecat.dto.SiteSelectionResult;
import com.bluecat.entity.ShopConfig;
import com.bluecat.entity.ShopInfo;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.service.ShopConfigService;
import com.bluecat.service.ShopInfoService;
import com.bluecat.service.ShopStatusSnapshotService;
import com.bluecat.service.SiteSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新店选址服务实现
 *
 * @author BlueCat
 * @since 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteSelectionServiceImpl implements SiteSelectionService {

    private final ShopInfoService shopInfoService;
    private final ShopStatusSnapshotService shopStatusSnapshotService;
    private final ShopConfigService shopConfigService;

    private static final double EARTH_RADIUS = 6371000; // 地球半径(米)

    @Override
    public SiteSelectionResult analyzeSite(SiteSelectionRequest request) {
        log.info("开始选址分析: 经度={}, 纬度={}, 半径={}", 
                request.getLongitude(), request.getLatitude(), request.getRadius());
        
        SiteSelectionResult result = new SiteSelectionResult();
        result.setLongitude(request.getLongitude());
        result.setLatitude(request.getLatitude());
        result.setRadius(request.getRadius());

        try {
            // 1. 查询所有门店
            log.debug("查询所有门店...");
            LambdaQueryWrapper<ShopInfo> shopWrapper = new LambdaQueryWrapper<ShopInfo>()
                    .isNotNull(ShopInfo::getLongitude)
                    .isNotNull(ShopInfo::getLatitude);
            
            List<ShopInfo> allShops = shopInfoService.list(shopWrapper);
            log.info("查询到 {} 家门店", allShops.size());

            // 2. 筛选范围内的门店并计算距离
            List<NearbyShopDTO> nearbyShops = new ArrayList<>();
            for (ShopInfo shop : allShops) {
                try {
                    // 防止空指针
                    if (shop.getLatitude() == null || shop.getLongitude() == null) {
                        log.warn("门店 {} 经纬度为空，跳过", shop.getId());
                        continue;
                    }
                    
                    Double distance = calculateDistance(
                            request.getLatitude().doubleValue(),
                            request.getLongitude().doubleValue(),
                            shop.getLatitude().doubleValue(),
                            shop.getLongitude().doubleValue()
                    );

                    if (distance <= request.getRadius()) {
                        NearbyShopDTO dto = convertToDTO(shop, distance);
                        nearbyShops.add(dto);
                    }
                } catch (Exception e) {
                    log.error("处理门店 {} 时出错: {}", shop.getId(), e.getMessage());
                }
            }
            log.info("筛选出 {} 家附近门店", nearbyShops.size());

            // 按距离排序
            nearbyShops.sort(Comparator.comparingDouble(NearbyShopDTO::getDistance));

            // 3. 获取门店实时状态
            log.debug("填充实时状态...");
            fillRealtimeStatus(nearbyShops);

            // 4. 计算历史平均上座率
            if (request.getStartTime() != null && request.getEndTime() != null) {
                log.debug("填充历史数据: {} - {}", request.getStartTime(), request.getEndTime());
                fillHistoricalData(nearbyShops, request.getStartTime(), request.getEndTime());
            }

            result.setNearbyShops(nearbyShops);
            result.setTotalShops(nearbyShops.size());

            // 5. 统计分析
            if (!nearbyShops.isEmpty()) {
                log.debug("计算统计数据...");
                calculateStatistics(result, nearbyShops, request.getRadius());
            } else {
                // 无附近门店的特殊处理
                log.info("无附近门店，执行特殊处理");
                handleNoNearbyShops(result, request.getRadius());
            }
            
            log.info("选址分析完成: 总门店={}, 推荐指数={}", result.getTotalShops(), result.getRecommendationScore());
        } catch (Exception e) {
            log.error("选址分析过程出错", e);
            throw new RuntimeException("选址分析失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 计算两点之间的距离(米) - Haversine公式
     */
    private Double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 转换为DTO
     */
    private NearbyShopDTO convertToDTO(ShopInfo shop, Double distance) {
        NearbyShopDTO dto = new NearbyShopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setSnbName(shop.getSnbName());
        dto.setAddress(shop.getAddress());
        dto.setZoneName(shop.getZoneName());
        dto.setLongitude(shop.getLongitude());
        dto.setLatitude(shop.getLatitude());
        dto.setDistance(Math.round(distance * 10.0) / 10.0); // 保留1位小数
        dto.setConfigId(shop.getConfigId());
        dto.setStatus(shop.getStatus());

        // 获取网咖配置名称
        if (shop.getConfigId() != null) {
            try {
                ShopConfig config = shopConfigService.getById(shop.getConfigId());
                if (config != null) {
                    dto.setConfigName(config.getConfigName());
                }
            } catch (Exception e) {
                log.warn("获取门店配置失败: shopId={}, configId={}, error={}", 
                        shop.getId(), shop.getConfigId(), e.getMessage());
            }
        }

        return dto;
    }

    /**
     * 填充实时状态
     */
    private void fillRealtimeStatus(List<NearbyShopDTO> shops) {
        for (NearbyShopDTO shop : shops) {
            try {
                ShopStatusSnapshot latest = shopStatusSnapshotService.getLatestByShopId(shop.getId());
                if (latest != null) {
                    shop.setTotalMachines(latest.getTotalMachines() != null ? latest.getTotalMachines() : 0);
                    shop.setFreeMachines(latest.getFreeMachines() != null ? latest.getFreeMachines() : 0);
                    shop.setBusyMachines(latest.getBusyMachines() != null ? latest.getBusyMachines() : 0);
                    shop.setOccupancyRate(latest.getOccupancyRate() != null ? latest.getOccupancyRate() : BigDecimal.ZERO);
                } else {
                    shop.setTotalMachines(0);
                    shop.setFreeMachines(0);
                    shop.setBusyMachines(0);
                    shop.setOccupancyRate(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                log.warn("获取门店 {} 实时状态失败: {}", shop.getId(), e.getMessage());
                shop.setTotalMachines(0);
                shop.setFreeMachines(0);
                shop.setBusyMachines(0);
                shop.setOccupancyRate(BigDecimal.ZERO);
            }
        }
    }

    /**
     * 填充历史数据
     */
    private void fillHistoricalData(List<NearbyShopDTO> shops, LocalDateTime startTime, LocalDateTime endTime) {
        for (NearbyShopDTO shop : shops) {
            LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShopStatusSnapshot::getShopId, shop.getId())
                    .ge(ShopStatusSnapshot::getSnapshotTime, startTime)
                    .le(ShopStatusSnapshot::getSnapshotTime, endTime);

            List<ShopStatusSnapshot> snapshots = shopStatusSnapshotService.list(wrapper);

            if (!snapshots.isEmpty()) {
                double avgRate = snapshots.stream()
                        .filter(s -> s.getOccupancyRate() != null)
                        .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                        .average()
                        .orElse(0.0);

                shop.setAvgOccupancyRate(Math.round(avgRate * 10.0) / 10.0);
            }
        }
    }

    /**
     * 计算统计数据和分析结论
     */
    private void calculateStatistics(SiteSelectionResult result, List<NearbyShopDTO> shops, Integer radius) {
        // 总机器数
        int totalMachines = shops.stream()
                .mapToInt(s -> s.getTotalMachines() != null ? s.getTotalMachines() : 0)
                .sum();
        result.setTotalMachines(totalMachines);

        // 当前平均上座率
        double currentAvgRate = shops.stream()
                .filter(s -> s.getOccupancyRate() != null)
                .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                .average()
                .orElse(0.0);
        result.setCurrentAvgOccupancyRate(Math.round(currentAvgRate * 10.0) / 10.0);

        // 历史平均上座率
        double avgRate = shops.stream()
                .filter(s -> s.getAvgOccupancyRate() != null)
                .mapToDouble(s -> s.getAvgOccupancyRate())
                .average()
                .orElse(currentAvgRate);
        result.setAvgOccupancyRate(Math.round(avgRate * 10.0) / 10.0);

        // 最高/最低上座率
        double maxRate = shops.stream()
                .filter(s -> s.getOccupancyRate() != null)
                .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                .max()
                .orElse(0.0);
        double minRate = shops.stream()
                .filter(s -> s.getOccupancyRate() != null)
                .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                .min()
                .orElse(0.0);
        result.setMaxOccupancyRate(Math.round(maxRate * 10.0) / 10.0);
        result.setMinOccupancyRate(Math.round(minRate * 10.0) / 10.0);

        // 品牌分布分析
        Map<Long, List<NearbyShopDTO>> brandMap = shops.stream()
                .filter(s -> s.getConfigId() != null)
                .collect(Collectors.groupingBy(NearbyShopDTO::getConfigId));

        List<SiteSelectionResult.BrandDistribution> brandList = new ArrayList<>();
        for (Map.Entry<Long, List<NearbyShopDTO>> entry : brandMap.entrySet()) {
            List<NearbyShopDTO> brandShops = entry.getValue();
            SiteSelectionResult.BrandDistribution brand = new SiteSelectionResult.BrandDistribution();
            brand.setConfigId(entry.getKey());
            brand.setConfigName(brandShops.get(0).getConfigName());
            brand.setShopCount(brandShops.size());
            brand.setMachineCount(brandShops.stream()
                    .mapToInt(s -> s.getTotalMachines() != null ? s.getTotalMachines() : 0)
                    .sum());
            brand.setAvgOccupancyRate(brandShops.stream()
                    .filter(s -> s.getOccupancyRate() != null)
                    .mapToDouble(s -> s.getOccupancyRate().doubleValue())
                    .average()
                    .orElse(0.0));
            brand.setPercentage(Math.round(brandShops.size() * 1000.0 / shops.size()) / 10.0);
            brandList.add(brand);
        }
        brandList.sort((a, b) -> b.getShopCount().compareTo(a.getShopCount()));

        result.setBrandDistribution(brandList);
        result.setCompetitorCount(brandMap.size());

        // 计算市场饱和度评分 (基于门店密度和机器密度)
        // 圆形区域面积 = π * r^2 (平方公里)
        double areaKm2 = Math.PI * Math.pow(radius / 1000.0, 2);
        int machineDensity = (int) (totalMachines / areaKm2);
        result.setMachineDensity(Math.round(machineDensity * 10.0) / 10.0);

        // 饱和度评分：门店数量 + 机器密度因素
        int saturationScore = calculateSaturationScore(shops.size(), machineDensity);
        result.setSaturationScore(saturationScore);
        result.setSaturationLevel(getSaturationLevel(saturationScore));

        // 竞争激烈程度评分 (基于品牌数量、平均上座率)
        int competitionScore = calculateCompetitionScore(brandMap.size(), avgRate, shops.size());
        result.setCompetitionScore(competitionScore);
        result.setCompetitionLevel(getCompetitionLevel(competitionScore));

        // 开店推荐指数 (饱和度低、竞争低、平均上座率高中等)
        int recommendationScore = calculateRecommendationScore(saturationScore, competitionScore, avgRate);
        result.setRecommendationScore(recommendationScore);
        result.setRecommendationLevel(getRecommendationLevel(recommendationScore));

        // 生成综合建议
        String suggestion = generateSuggestion(recommendationScore, saturationScore, competitionScore, avgRate, shops.size());
        result.setSuggestion(suggestion);
    }

    /**
     * 计算市场饱和度评分 (0-100, 分数越高饱和度越高)
     */
    private int calculateSaturationScore(int shopCount, int machineDensity) {
        // 门店数量因子 (0-40分)
        int shopScore = Math.min(shopCount * 4, 40);

        // 机器密度因子 (0-60分)
        int densityScore = Math.min(machineDensity / 10, 60);

        return Math.min(shopScore + densityScore, 100);
    }

    /**
     * 计算竞争激烈程度评分 (0-100, 分数越高竞争越激烈)
     */
    private int calculateCompetitionScore(int brandCount, double avgRate, int shopCount) {
        // 品牌数量因子 (0-30分)
        int brandScore = Math.min(brandCount * 6, 30);

        // 门店数量因子 (0-30分)
        int shopScore = Math.min(shopCount * 3, 30);

        // 上座率因子 (上座率越高竞争越激烈, 0-40分)
        int rateScore = (int) (avgRate * 0.4);

        return Math.min(brandScore + shopScore + rateScore, 100);
    }

    /**
     * 计算开店推荐指数 (0-100, 分数越高越推荐)
     */
    private int calculateRecommendationScore(int saturationScore, int competitionScore, double avgRate) {
        // 饱和度反向影响 (饱和度越高,推荐度越低)
        int saturationFactor = 100 - saturationScore;

        // 竞争反向影响 (竞争越激烈,推荐度越低)
        int competitionFactor = 100 - competitionScore;

        // 上座率正向影响 (上座率越高,说明市场需求旺盛)
        int rateFactor = (int) (avgRate * 0.5);

        // 综合评分 (饱和度30% + 竞争30% + 上座率40%)
        int score = (int) (saturationFactor * 0.3 + competitionFactor * 0.3 + rateFactor * 0.4);

        return Math.max(0, Math.min(score, 100));
    }

    /**
     * 获取饱和度等级
     */
    private String getSaturationLevel(int score) {
        if (score < 25) return "低";
        if (score < 50) return "中";
        if (score < 75) return "高";
        return "极高";
    }

    /**
     * 获取竞争程度等级
     */
    private String getCompetitionLevel(int score) {
        if (score < 25) return "低";
        if (score < 50) return "中";
        if (score < 75) return "高";
        return "极高";
    }

    /**
     * 获取推荐等级
     */
    private String getRecommendationLevel(int score) {
        if (score < 20) return "不推荐";
        if (score < 40) return "谨慎考虑";
        if (score < 60) return "可以考虑";
        if (score < 80) return "推荐";
        return "强烈推荐";
    }

    /**
     * 生成综合建议
     */
    private String generateSuggestion(int recommendationScore, int saturationScore,
                                       int competitionScore, double avgRate, int shopCount) {
        StringBuilder sb = new StringBuilder();

        if (recommendationScore >= 80) {
            sb.append("该区域市场潜力较大，");
            if (saturationScore < 40) {
                sb.append("竞争相对较小，");
            }
            if (avgRate > 60) {
                sb.append("周边门店上座率较高，市场需求旺盛。建议优先考虑此位置。");
            } else {
                sb.append("周边门店经营情况良好。建议考虑此位置。");
            }
        } else if (recommendationScore >= 60) {
            sb.append("该区域有一定市场机会，");
            if (shopCount < 3) {
                sb.append("周边门店数量较少，存在市场空白。需要进一步考察周边人流和消费能力。");
            } else {
                sb.append("需要通过差异化经营策略提升竞争力。建议结合自身优势谨慎决策。");
            }
        } else if (recommendationScore >= 40) {
            sb.append("该区域市场竞争较为激烈，");
            if (saturationScore > 60) {
                sb.append("市场接近饱和，新店生存压力较大。建议寻找差异化定位或考虑其他位置。");
            } else {
                sb.append("需要充分的竞争优势才能立足。建议详细评估投资回报率。");
            }
        } else {
            sb.append("该区域市场已高度饱和，");
            if (competitionScore > 70) {
                sb.append("竞争非常激烈，不建议在此开店。建议重新选择位置。");
            } else {
                sb.append("开店风险较高，建议慎重考虑或寻找其他机会区域。");
            }
        }

        return sb.toString();
    }

    /**
     * 无附近门店的特殊处理
     */
    private void handleNoNearbyShops(SiteSelectionResult result, Integer radius) {
        result.setTotalMachines(0);
        result.setCurrentAvgOccupancyRate(0.0);
        result.setAvgOccupancyRate(0.0);
        result.setMaxOccupancyRate(0.0);
        result.setMinOccupancyRate(0.0);
        result.setCompetitorCount(0);
        result.setBrandDistribution(new ArrayList<>());
        result.setSaturationScore(0);
        result.setSaturationLevel("低");
        result.setCompetitionScore(0);
        result.setCompetitionLevel("低");
        result.setRecommendationScore(90);
        result.setRecommendationLevel("强烈推荐");

        double areaKm2 = Math.PI * Math.pow(radius / 1000.0, 2);
        result.setMachineDensity(0.0);

        result.setSuggestion("该区域" + radius + "米范围内暂无网咖门店，属于空白市场。建议进一步调研周边人流、消费水平和竞品情况，把握市场机会！");
    }
}
