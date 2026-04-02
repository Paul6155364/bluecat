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
 * 区域实时状态快照表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("area_status_snapshot")
@ApiModel(value = "AreaStatusSnapshot对象", description = "区域实时状态快照表")
public class AreaStatusSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("门店快照ID")
    private Long snapshotId;

    @ApiModelProperty("采集任务ID")
    private Long taskId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("区域名称")
    private String areaName;

    @ApiModelProperty("是否VIP房间:1是,0否")
    private Integer vipRoom;

    @ApiModelProperty("机器总数")
    private Integer totalMachines;

    @ApiModelProperty("空闲机器数")
    private Integer freeMachines;

    @ApiModelProperty("占用机器数")
    private Integer busyMachines;

    @ApiModelProperty("空闲机器名称列表")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object freeMachineList;

    @ApiModelProperty("上座率(%)")
    private BigDecimal occupancyRate;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
