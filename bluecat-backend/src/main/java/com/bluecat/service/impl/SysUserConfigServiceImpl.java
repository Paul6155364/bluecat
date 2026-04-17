package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.SysUserConfig;
import com.bluecat.mapper.SysUserConfigMapper;
import com.bluecat.service.SysUserConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户网吧配置关联 Service实现
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserConfigServiceImpl extends ServiceImpl<SysUserConfigMapper, SysUserConfig> implements SysUserConfigService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户数据权限缓存key前缀
     */
    private static final String USER_CONFIG_CACHE_KEY = "bluecat:user:config:";

    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public List<Long> getConfigIdsByUserId(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_CONFIG_CACHE_KEY + userId;
        
        try {
            List<Long> configIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);

            if (configIds != null) {
                log.debug("从缓存获取用户{}的网吧配置权限: {}", userId, configIds);
                return configIds;
            }
        } catch (Exception e) {
            log.warn("Redis连接失败，从数据库查询用户网吧配置权限: userId={}, error={}", userId, e.getMessage());
        }

        // 从数据库查询
        List<Long> configIds = baseMapper.selectConfigIdsByUserId(userId);
        if (configIds == null) {
            configIds = new ArrayList<>();
        }

        // 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, configIds, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("从数据库获取用户{}的网吧配置权限并缓存: {}", userId, configIds);
        } catch (Exception e) {
            log.warn("Redis写入缓存失败，不影响业务: userId={}, error={}", userId, e.getMessage());
        }

        return configIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserConfigs(Long userId, List<Long> configIds) {
        // 先删除旧的关联
        deleteUserConfigs(userId);

        // 批量插入新的关联
        if (configIds != null && !configIds.isEmpty()) {
            List<SysUserConfig> userConfigs = new ArrayList<>();
            for (Long configId : configIds) {
                SysUserConfig userConfig = new SysUserConfig();
                userConfig.setUserId(userId);
                userConfig.setConfigId(configId);
                userConfigs.add(userConfig);
            }
            saveBatch(userConfigs);
            log.info("保存用户{}的网吧配置授权: {}", userId, configIds);
        }

        // 清除缓存
        clearUserConfigCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserConfigs(Long userId) {
        remove(new LambdaQueryWrapper<SysUserConfig>()
                .eq(SysUserConfig::getUserId, userId));
        log.info("删除用户{}的所有网吧配置授权", userId);

        // 清除缓存
        clearUserConfigCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConfigId(Long configId) {
        baseMapper.deleteByConfigId(configId);
        log.info("删除网吧配置{}的所有用户关联", configId);
    }

    @Override
    public void clearUserConfigCache(Long userId) {
        try {
            String cacheKey = USER_CONFIG_CACHE_KEY + userId;
            redisTemplate.delete(cacheKey);
            log.debug("清除用户{}的数据权限缓存", userId);
        } catch (Exception e) {
            log.warn("Redis清除缓存失败，不影响业务: userId={}, error={}", userId, e.getMessage());
        }
    }
}
