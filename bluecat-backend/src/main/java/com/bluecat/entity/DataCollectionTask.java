package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据采集任务表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("data_collection_task")
@ApiModel(value = "DataCollectionTask对象", description = "数据采集任务表")
public class DataCollectionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("网吧配置ID")
    private Long configId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("任务类型:SHOP_LIST/SHOP_INFO/MACHINE_STATUS")
    private String taskType;

    @ApiModelProperty("快照时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime snapshotTime;

    @ApiModelProperty("状态:0执行中,1成功,2失败")
    private Integer status;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("耗时(毫秒)")
    private Integer durationMs;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
