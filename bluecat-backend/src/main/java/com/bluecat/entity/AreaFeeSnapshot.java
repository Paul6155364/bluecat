package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区域费用快照表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("area_fee_snapshot")
@ApiModel(value = "AreaFeeSnapshot对象", description = "区域费用快照表")
public class AreaFeeSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("门店快照ID")
    private Long snapshotId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("区域名称")
    private String areaName;

    @ApiModelProperty("是否VIP房间")
    private Integer vipRoom;

    @ApiModelProperty("总座位数")
    private Integer totalSeats;

    @ApiModelProperty("是否单上")
    private Integer isSingleUp;

    @ApiModelProperty("费率")
    private BigDecimal rate;

    @ApiModelProperty("预估费用")
    private BigDecimal estimateFee;

    @ApiModelProperty("单位费率")
    private BigDecimal unitRate;

    @ApiModelProperty("是否整包")
    private Integer whole;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
