package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联表 Mapper 接口
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID删除关联关系
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
}
