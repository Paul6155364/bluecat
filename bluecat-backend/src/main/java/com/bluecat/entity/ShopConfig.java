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
 * 网吧配置表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_config")
@ApiModel(value = "ShopConfig对象", description = "网吧配置表")
public class ShopConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("配置名称")
    private String configName;

    @ApiModelProperty("网吧主账号snbid")
    private String snbid;

    @ApiModelProperty("平台类型:0=x管家,1=银杏管家")
    private Integer platformType;

    @ApiModelProperty("AppID")
    private String appId;

    @ApiModelProperty("JWT Token")
    private String jwtToken;

    @ApiModelProperty("Cookie")
    private String cookie;

    @ApiModelProperty("Token过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tokenExpireTime;

    @ApiModelProperty("Token来源")
    private String tokenSource;

    @ApiModelProperty("状态:1启用,0禁用")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("最后采集时间（瞬态字段，不映射数据库）")
    @TableField(exist = false)
    private String lastCollectTime;
}
