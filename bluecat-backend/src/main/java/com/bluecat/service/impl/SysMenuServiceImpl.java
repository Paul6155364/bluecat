package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.dto.MenuTreeDTO;
import com.bluecat.entity.SysMenu;
import com.bluecat.mapper.SysMenuMapper;
import com.bluecat.service.SysMenuService;
import com.bluecat.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> listByUserId(Long userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<MenuTreeDTO> treeAll() {
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDeleted, 0)
                .orderByAsc(SysMenu::getSortOrder));
        return buildTree(menus);
    }

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuService.listMenuIdsByRoleId(roleId);
    }

    @Override
    public List<MenuTreeDTO> buildTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        List<MenuTreeDTO> treeList = menus.stream().map(menu -> {
            MenuTreeDTO dto = new MenuTreeDTO();
            BeanUtils.copyProperties(menu, dto);
            return dto;
        }).collect(Collectors.toList());

        // 构建树结构
        Map<Long, List<MenuTreeDTO>> parentMap = treeList.stream()
                .collect(Collectors.groupingBy(MenuTreeDTO::getParentId));

        treeList.forEach(menu -> {
            List<MenuTreeDTO> children = parentMap.get(menu.getId());
            if (children != null && !children.isEmpty()) {
                menu.setChildren(children);
            }
        });

        // 返回顶级菜单
        return treeList.stream()
                .filter(menu -> menu.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuTreeDTO> userMenus(Long userId) {
        List<SysMenu> menus = listByUserId(userId);
        return buildTree(menus);
    }
}
