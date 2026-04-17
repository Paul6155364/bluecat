package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.dto.RoleDTO;
import com.bluecat.entity.SysRole;

import java.util.List;

/**
 * 角色服务类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询所有角色
     */
    List<SysRole> listAll();

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> listByUserId(Long userId);

    /**
     * 新增角色并分配菜单
     */
    void saveRole(RoleDTO roleDTO);

    /**
     * 更新角色并更新菜单
     */
    void updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 分配菜单权限
     */
    void assignMenu(Long roleId, List<Long> menuIds);
}
