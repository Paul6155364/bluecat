package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.dto.MenuTreeDTO;
import com.bluecat.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysMenu> listByUserId(Long userId);

    /**
     * 查询所有菜单树
     */
    List<MenuTreeDTO> treeAll();

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> listMenuIdsByRoleId(Long roleId);

    /**
     * 构建菜单树
     */
    List<MenuTreeDTO> buildTree(List<SysMenu> menus);

    /**
     * 获取用户菜单树
     */
    List<MenuTreeDTO> userMenus(Long userId);
}
