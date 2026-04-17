package com.bluecat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 附近门店信息
 *
 * @author BlueCat
 * @since 2026-04-03
 */
@Data
@ApiModel(value = "NearbyShopDTO", description = "附近门店信息")
public class NearbyShopDTO {

    @ApiModelProperty("门店ID")
    private Long id;

    @ApiModelProperty("门店名称")
    private String name;

    @ApiModelProperty("门店简称")
    private String snbName;

    @ApiModelProperty("详细地址")
    private String address;

    @ApiModelProperty("区县")
    private String zoneName;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度")
    private BigDecimal latitude;

    @ApiModelProperty("距离(米)")
    private Double distance;

    @ApiModelProperty("网咖配置ID")
    private Long configId;

    @ApiModelProperty("网咖名称")
    private String configName;

    @ApiModelProperty("机器总数")
    private Integer totalMachines;

    @ApiModelProperty("当前空闲机器数")
    private Integer freeMachines;

    @ApiModelProperty("当前占用机器数")
    private Integer busyMachines;

    @ApiModelProperty("当前上座率(%)")
    private BigDecimal occupancyRate;

    @ApiModelProperty("平均上座率(%)")
    private Double avgOccupancyRate;

    @ApiModelProperty("营业状态:2正常")
    private Integer status;
}
