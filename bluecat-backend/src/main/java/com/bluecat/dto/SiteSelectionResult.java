package com.bluecat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 新店选址分析结果
 *
 * @author BlueCat
 * @since 2026-04-03
 */
@Data
@ApiModel(value = "SiteSelectionResult", description = "新店选址分析结果")
public class SiteSelectionResult {

    @ApiModelProperty("选址经度")
    private BigDecimal longitude;

    @ApiModelProperty("选址纬度")
    private BigDecimal latitude;

    @ApiModelProperty("分析半径(米)")
    private Integer radius;

    @ApiModelProperty("附近门店列表")
    private List<NearbyShopDTO> nearbyShops;

    @ApiModelProperty("附近门店总数")
    private Integer totalShops;

    @ApiModelProperty("总机器数")
    private Integer totalMachines;

    @ApiModelProperty("平均上座率(%)")
    private Double avgOccupancyRate;

    @ApiModelProperty("当前平均上座率(%)")
    private Double currentAvgOccupancyRate;

    @ApiModelProperty("最高上座率(%)")
    private Double maxOccupancyRate;

    @ApiModelProperty("最低上座率(%)")
    private Double minOccupancyRate;

    // ========== 竞争分析 ==========

    @ApiModelProperty("竞争品牌数量")
    private Integer competitorCount;

    @ApiModelProperty("竞争品牌分布")
    private List<BrandDistribution> brandDistribution;

    // ========== 市场分析 ==========

    @ApiModelProperty("市场饱和度评分(0-100)")
    private Integer saturationScore;

    @ApiModelProperty("市场饱和度等级: 低/中/高/极高")
    private String saturationLevel;

    @ApiModelProperty("竞争激烈程度评分(0-100)")
    private Integer competitionScore;

    @ApiModelProperty("竞争激烈程度等级: 低/中/高/极高")
    private String competitionLevel;

    @ApiModelProperty("开店推荐指数(0-100)")
    private Integer recommendationScore;

    @ApiModelProperty("开店推荐等级: 不推荐/谨慎考虑/可以考虑/推荐/强烈推荐")
    private String recommendationLevel;

    @ApiModelProperty("综合建议")
    private String suggestion;

    // ========== 区域分析 ==========

    @ApiModelProperty("区域机器密度(台/平方公里)")
    private Double machineDensity;

    @ApiModelProperty("高峰时段平均上座率(%)")
    private Double peakHourOccupancyRate;

    /**
     * 品牌分布
     */
    @Data
    @ApiModel(value = "BrandDistribution", description = "品牌分布")
    public static class BrandDistribution {

        @ApiModelProperty("网咖配置ID")
        private Long configId;

        @ApiModelProperty("网咖名称")
        private String configName;

        @ApiModelProperty("门店数量")
        private Integer shopCount;

        @ApiModelProperty("机器总数")
        private Integer machineCount;

        @ApiModelProperty("平均上座率(%)")
        private Double avgOccupancyRate;

        @ApiModelProperty("占比(%)")
        private Double percentage;
    }
}
