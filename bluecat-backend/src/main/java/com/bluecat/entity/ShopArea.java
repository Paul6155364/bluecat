package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店区域表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_area")
@ApiModel(value = "ShopArea对象", description = "门店区域表")
public class ShopArea extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("区域名称")
    private String areaName;

    @ApiModelProperty("是否允许预订:1允许,0不允许")
    private Integer allow;

    @ApiModelProperty("最小预订数量")
    private Integer minNum;

    @ApiModelProperty("排序")
    private Integer sortOrder;
}
