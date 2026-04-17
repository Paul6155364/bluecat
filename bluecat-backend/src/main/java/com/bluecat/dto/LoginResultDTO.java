package com.bluecat.dto;

import com.bluecat.entity.SysRole;
import com.bluecat.entity.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录结果DTO
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Data
@ApiModel(value = "登录结果DTO", description = "登录成功后返回的数据")
public class LoginResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Token")
    private String token;

    @ApiModelProperty("用户信息")
    private SysUser user;

    @ApiModelProperty("角色列表")
    private List<SysRole> roles;

    @ApiModelProperty("菜单树")
    private List<MenuTreeDTO> menus;
}
