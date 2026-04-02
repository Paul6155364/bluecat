package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API调用日志表
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Data
@TableName("api_call_log")
@ApiModel(value = "ApiCallLog对象", description = "API调用日志表")
public class ApiCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("网吧配置ID")
    private Long configId;

    @ApiModelProperty("门店ID")
    private Long shopId;

    @ApiModelProperty("API名称")
    private String apiName;

    @ApiModelProperty("API地址")
    private String apiUrl;

    @ApiModelProperty("请求方法")
    private String requestMethod;

    @ApiModelProperty("请求头")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object requestHeaders;

    @ApiModelProperty("请求体")
    private String requestBody;

    @ApiModelProperty("响应码")
    private Integer responseCode;

    @ApiModelProperty("响应体")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object responseBody;

    @ApiModelProperty("状态:1成功,0失败")
    private Integer status;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("耗时(毫秒)")
    private Integer durationMs;

    @ApiModelProperty("调用时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
