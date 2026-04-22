package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实时状态快照表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("shop_status_snapshot")
@ApiModel(value = "ShopStatusSnapshot对象", description = "门店实时状态快照表")
public class ShopStatusSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("采集任务ID")
    private Long taskId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("快照时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime snapshotTime;

    @ApiModelProperty("机器总数")
    private Integer totalMachines;

    @ApiModelProperty("空闲机器数")
    private Integer freeMachines;

    @ApiModelProperty("占用机器数")
    private Integer busyMachines;

    @ApiModelProperty("整体上座率(%)")
    private BigDecimal occupancyRate;

    @ApiModelProperty("剩余时长")
    private BigDecimal remain;

    @ApiModelProperty("今日累计营收(元)")
    private BigDecimal dayRevenue;

    @ApiModelProperty("原始JSON")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object rawJson;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
