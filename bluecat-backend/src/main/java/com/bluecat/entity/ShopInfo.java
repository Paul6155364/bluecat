package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 门店信息表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_info")
@ApiModel(value = "ShopInfo对象", description = "门店信息表")
public class ShopInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("网吧配置ID")
    private Long configId;

    @ApiModelProperty("门店编号")
    private String snbid;

    @ApiModelProperty("门店名称")
    private String name;

    @ApiModelProperty("门店简称")
    private String snbName;

    @ApiModelProperty("登录省份")
    private String loginProvince;

    @ApiModelProperty("登录城市")
    private String loginCity;

    @ApiModelProperty("登录区县")
    private String loginZone;

    @ApiModelProperty("登录详细地址")
    private String loginAddress;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("城市名称")
    private String cityName;

    @ApiModelProperty("区县名称")
    private String zoneName;

    @ApiModelProperty("前台电话")
    private String stel;

    @ApiModelProperty("老板电话")
    private String sbossTel;

    @ApiModelProperty("网吧手机")
    private String netMobile;

    @ApiModelProperty("蓝猫电话")
    private String lbPhone;

    @ApiModelProperty("QQ号")
    private String qq;

    @ApiModelProperty("WiFi名称")
    private String wifiName;

    @ApiModelProperty("WiFi密码")
    private String wifiPwd;

    @ApiModelProperty("门店头像URL")
    private String head;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度")
    private BigDecimal latitude;

    @ApiModelProperty("腾讯经度")
    private BigDecimal longitudeTencent;

    @ApiModelProperty("腾讯纬度")
    private BigDecimal latitudeTencent;

    @ApiModelProperty("区域百度ID")
    private Integer regionBiduId;

    @ApiModelProperty("省份ID")
    private Integer provinceNameId;

    @ApiModelProperty("城市ID")
    private Integer cityNameId;

    @ApiModelProperty("是否有包间:1是,0否")
    private Integer haveRoom;

    @ApiModelProperty("是否支持预订:1是,0否")
    private Integer haveBookSeat;

    @ApiModelProperty("是否有新活动:1是,0否")
    private Integer haveOdeNewActivity;

    @ApiModelProperty("营业状态:2正常")
    private Integer status;

    @ApiModelProperty("是否开启租赁权益:1是,0否")
    private Integer haveOpenLeaseBenefit;

    @ApiModelProperty("是否开启优惠券权益:1是,0否")
    private Integer haveOpenCouponBenefit;

    @ApiModelProperty("系统店铺ID")
    private Long shopId;

    @ApiModelProperty("getSnbidInfo原始JSON数据")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object rawJson;
}
