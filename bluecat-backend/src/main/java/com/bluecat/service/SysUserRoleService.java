package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.SysUserRole;

import java.util.List;

/**
 * 用户角色服务类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> listRoleIdsByUserId(Long userId);

    /**
     * 根据用户ID删除关联关系
     */
    void deleteByUserId(Long userId);

    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
}
