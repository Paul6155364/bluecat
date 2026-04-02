package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 禁止预订机器表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("machine_ban_list")
@ApiModel(value = "MachineBanList对象", description = "禁止预订机器表")
public class MachineBanList implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("机器名称")
    private String comName;

    @ApiModelProperty("区域名称")
    private String areaName;

    @ApiModelProperty("VIP房间")
    private String vipRoom;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
