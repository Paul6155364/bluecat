package com.bluecat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.RoleDTO;
import com.bluecat.entity.SysRole;
import com.bluecat.mapper.SysRoleMapper;
import com.bluecat.service.SysRoleMenuService;
import com.bluecat.service.SysRoleService;
import com.bluecat.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuService sysRoleMenuService;
    private final SysUserRoleService sysUserRoleService;

    @Override
    public List<SysRole> listAll() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getSortOrder));
    }

    @Override
    public List<SysRole> listByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleService.listRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(RoleDTO roleDTO) {
        // 检查角色编码是否已存在
        long count = count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleDTO.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleCode(roleDTO.getRoleCode());
        role.setDescription(roleDTO.getDescription());
        role.setStatus(roleDTO.getStatus() != null ? roleDTO.getStatus() : 1);
        role.setSortOrder(roleDTO.getSortOrder() != null ? roleDTO.getSortOrder() : 0);
        role.setCreateBy(StpUtil.getLoginIdAsString());
        role.setCreateTime(LocalDateTime.now());
        role.setDeleted(0);

        save(role);

        // 分配菜单
        if (roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            sysRoleMenuService.assignMenus(role.getId(), roleDTO.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO roleDTO) {
        SysRole role = getById(roleDTO.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setRoleName(roleDTO.getRoleName());
        role.setDescription(roleDTO.getDescription());
        role.setStatus(roleDTO.getStatus());
        role.setSortOrder(roleDTO.getSortOrder());
        role.setUpdateTime(LocalDateTime.now());

        updateById(role);

        // 更新菜单权限
        if (roleDTO.getMenuIds() != null) {
            sysRoleMenuService.assignMenus(role.getId(), roleDTO.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除角色菜单关联
        sysRoleMenuService.deleteByRoleId(id);

        // 删除用户角色关联
        sysUserRoleService.lambdaUpdate()
                .eq(com.bluecat.entity.SysUserRole::getRoleId, id)
                .remove();

        // 逻辑删除角色
        role.setDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenu(Long roleId, List<Long> menuIds) {
        sysRoleMenuService.assignMenus(roleId, menuIds);
    }
}
