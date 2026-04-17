package com.bluecat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树 DTO
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Data
@ApiModel(value = "MenuTreeDTO", description = "菜单树传输对象")
public class MenuTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("菜单ID")
    private Long id;

    @ApiModelProperty("父菜单ID")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String menuName;

    @ApiModelProperty("菜单编码")
    private String menuCode;

    @ApiModelProperty("路由路径")
    private String path;

    @ApiModelProperty("菜单图标")
    private String icon;

    @ApiModelProperty("组件路径")
    private String component;

    @ApiModelProperty("类型:1目录,2菜单,3按钮")
    private Integer menuType;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("是否可见")
    private Integer visible;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("子菜单")
    private List<MenuTreeDTO> children;
}
