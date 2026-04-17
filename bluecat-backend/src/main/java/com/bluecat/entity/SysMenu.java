package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限表
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@ApiModel(value = "SysMenu对象", description = "菜单权限表")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("父菜单ID,0为顶级菜单")
    private Long parentId;

    @ApiModelProperty("菜单名称")
    private String menuName;

    @ApiModelProperty("菜单编码(权限标识)")
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

    @ApiModelProperty("是否可见:1是,0否")
    private Integer visible;

    @ApiModelProperty("状态:1正常,0禁用")
    private Integer status;

    @ApiModelProperty("创建人")
    private String createBy;
}
