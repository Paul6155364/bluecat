package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限表 Mapper 接口
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
