package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 机器实时状态历史表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("machine_status_history")
@ApiModel(value = "MachineStatusHistory对象", description = "机器实时状态历史表")
public class MachineStatusHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("门店快照ID")
    private Long snapshotId;

    @ApiModelProperty("采集任务ID")
    private Long taskId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("机器ID")
    private Long machineId;

    @ApiModelProperty("机器名称")
    private String comName;

    @ApiModelProperty("所属区域")
    private String areaName;

    @ApiModelProperty("状态:1空闲,0占用")
    private Integer status;

    @ApiModelProperty("快照时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime snapshotTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
