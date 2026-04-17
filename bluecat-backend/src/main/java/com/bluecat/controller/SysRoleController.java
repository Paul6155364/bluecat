package com.bluecat.controller;

import com.bluecat.common.Result;
import com.bluecat.dto.RoleDTO;
import com.bluecat.entity.SysRole;
import com.bluecat.service.SysMenuService;
import com.bluecat.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;

    @ApiOperation("角色列表")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        List<SysRole> roles = sysRoleService.listAll();
        return Result.success(roles);
    }

    @ApiOperation("新增角色")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoleDTO roleDTO) {
        sysRoleService.saveRole(roleDTO);
        return Result.success();
    }

    @ApiOperation("编辑角色")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        roleDTO.setId(id);
        sysRoleService.updateRole(roleDTO);
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @ApiOperation("分配菜单权限")
    @PutMapping("/assignMenu")
    public Result<Void> assignMenu(@RequestBody RoleDTO roleDTO) {
        sysRoleService.assignMenu(roleDTO.getId(), roleDTO.getMenuIds());
        return Result.success();
    }

    @ApiOperation("获取角色菜单ID列表")
    @GetMapping("/menus/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<Long> menuIds = sysMenuService.listMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }
}
