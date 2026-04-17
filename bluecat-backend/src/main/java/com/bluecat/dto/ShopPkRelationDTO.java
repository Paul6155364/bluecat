package com.bluecat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 门店PK关系 DTO
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Data
@ApiModel(value = "ShopPkRelationDTO", description = "门店PK关系传输对象")
public class ShopPkRelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("PK关系名称")
    @NotBlank(message = "PK关系名称不能为空")
    private String name;

    @ApiModelProperty("主门店ID")
    private Long mainShopId;

    @ApiModelProperty("主门店名称")
    private String mainShopName;

    @ApiModelProperty("PK对手门店ID列表")
    @NotEmpty(message = "请选择至少一个PK对手门店")
    private List<Long> competitorShopIds;

    @ApiModelProperty("PK对手门店名称列表")
    private List<String> competitorShopNames;
}
