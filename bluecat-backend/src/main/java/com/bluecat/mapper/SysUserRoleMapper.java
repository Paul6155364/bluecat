package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联表 Mapper 接口
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除关联关系
     */
    int deleteByUserId(@Param("userId") Long userId);
}
