package com.bluecat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 角色 DTO
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Data
@ApiModel(value = "RoleDTO", description = "角色传输对象")
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("角色ID")
    private Long id;

    @ApiModelProperty("角色名称")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @ApiModelProperty("角色编码")
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @ApiModelProperty("角色描述")
    private String description;

    @ApiModelProperty("状态:1正常,0禁用")
    private Integer status;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("菜单ID列表")
    private List<Long> menuIds;
}
