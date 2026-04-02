package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门店图片表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("shop_image")
@ApiModel(value = "ShopImage对象", description = "门店图片表")
public class ShopImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("图片ID")
    private Long picId;

    @ApiModelProperty("图片URL")
    private String picUrl;

    @ApiModelProperty("是否封面:1是,0否")
    private Integer cover;

    @ApiModelProperty("系统图片标记")
    private String systemPic;

    @ApiModelProperty("图片状态:1正常")
    private Integer picState;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dtUpdate;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
