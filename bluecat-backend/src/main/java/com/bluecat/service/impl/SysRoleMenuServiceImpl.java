package com.bluecat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.SysRoleMenu;
import com.bluecat.mapper.SysRoleMenuMapper;
import com.bluecat.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色菜单服务实现类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        baseMapper.deleteByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有菜单
        deleteByRoleId(roleId);

        // 添加新菜单
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenus = new ArrayList<>();
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateTime(LocalDateTime.now());
                roleMenus.add(roleMenu);
            }
            saveBatch(roleMenus);
        }
    }
}
