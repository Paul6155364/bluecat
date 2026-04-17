package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单服务类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> listMenuIdsByRoleId(Long roleId);

    /**
     * 根据角色ID删除关联关系
     */
    void deleteByRoleId(Long roleId);

    /**
     * 分配菜单
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
