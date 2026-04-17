package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.Result;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.MenuTreeDTO;
import com.bluecat.entity.SysMenu;
import com.bluecat.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @ApiOperation("菜单树")
    @GetMapping("/tree")
    public Result<List<MenuTreeDTO>> tree() {
        List<MenuTreeDTO> tree = sysMenuService.treeAll();
        return Result.success(tree);
    }

    @ApiOperation("用户菜单")
    @GetMapping("/userMenus")
    public Result<List<MenuTreeDTO>> userMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SysMenu> menus = sysMenuService.listByUserId(userId);
        List<MenuTreeDTO> tree = sysMenuService.buildTree(menus);
        return Result.success(tree);
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysMenu menu) {
        menu.setCreateBy(StpUtil.getLoginIdAsString());
        menu.setCreateTime(LocalDateTime.now());
        menu.setDeleted(0);
        sysMenuService.save(menu);
        return Result.success();
    }

    @ApiOperation("编辑菜单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SysMenu menu) {
        SysMenu existMenu = sysMenuService.getById(id);
        if (existMenu == null) {
            throw new BusinessException("菜单不存在");
        }

        menu.setId(id);
        menu.setUpdateTime(LocalDateTime.now());
        sysMenuService.updateById(menu);
        return Result.success();
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        long count = sysMenuService.count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }

        // 逻辑删除
        menu.setDeleted(1);
        menu.setUpdateTime(LocalDateTime.now());
        sysMenuService.updateById(menu);

        return Result.success();
    }
}
