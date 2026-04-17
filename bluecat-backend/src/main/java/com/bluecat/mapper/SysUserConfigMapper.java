package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.SysUserConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户网吧配置关联 Mapper
 *
 * @author BlueCat
 * @since 2026-04-17
 */
public interface SysUserConfigMapper extends BaseMapper<SysUserConfig> {

    /**
     * 根据用户ID查询授权的网吧配置ID列表
     *
     * @param userId 用户ID
     * @return 网吧配置ID列表
     */
    List<Long> selectConfigIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据网吧配置ID删除所有关联
     *
     * @param configId 网吧配置ID
     * @return 删除数量
     */
    int deleteByConfigId(@Param("configId") Long configId);
}
