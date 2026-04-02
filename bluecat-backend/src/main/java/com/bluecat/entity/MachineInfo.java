package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 机器信息表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("machine_info")
@ApiModel(value = "MachineInfo对象", description = "机器信息表")
public class MachineInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("区域ID")
    private Long areaId;

    @ApiModelProperty("机器名称")
    private String comName;

    @ApiModelProperty("所属区域名称")
    private String areaName;

    @ApiModelProperty("绑定卡ID")
    private String cardId;

    @ApiModelProperty("最后下线时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOfflineTime;

    @ApiModelProperty("get-area-com-set-info原始JSON数据")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object rawJson;
}
