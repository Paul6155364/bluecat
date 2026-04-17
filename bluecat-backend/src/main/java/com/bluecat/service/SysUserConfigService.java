package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.SysUserConfig;

import java.util.List;

/**
 * 用户网吧配置关联 Service
 *
 * @author BlueCat
 * @since 2026-04-17
 */
public interface SysUserConfigService extends IService<SysUserConfig> {

    /**
     * 获取用户授权的网吧配置ID列表
     *
     * @param userId 用户ID
     * @return 网吧配置ID列表
     */
    List<Long> getConfigIdsByUserId(Long userId);

    /**
     * 保存用户授权网吧配置
     *
     * @param userId 用户ID
     * @param configIds 网吧配置ID列表
     */
    void saveUserConfigs(Long userId, List<Long> configIds);

    /**
     * 删除用户所有网吧配置授权
     *
     * @param userId 用户ID
     */
    void deleteUserConfigs(Long userId);

    /**
     * 根据网吧配置ID删除所有关联
     *
     * @param configId 网吧配置ID
     */
    void deleteByConfigId(Long configId);

    /**
     * 清除用户数据权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserConfigCache(Long userId);
}
