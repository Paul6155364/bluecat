package com.bluecat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 新店选址分析请求
 *
 * @author BlueCat
 * @since 2026-04-03
 */
@Data
@ApiModel(value = "SiteSelectionRequest", description = "新店选址分析请求")
public class SiteSelectionRequest {

    @ApiModelProperty(value = "经度", required = true)
    private BigDecimal longitude;

    @ApiModelProperty(value = "纬度", required = true)
    private BigDecimal latitude;

    @ApiModelProperty(value = "分析半径(米)", required = true, example = "1000")
    private Integer radius;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
