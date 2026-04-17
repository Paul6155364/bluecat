package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopConfig;
import com.bluecat.mapper.ShopConfigMapper;
import com.bluecat.service.ShopConfigService;
import com.bluecat.util.DataScopeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 网吧配置表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class ShopConfigServiceImpl extends ServiceImpl<ShopConfigMapper, ShopConfig> implements ShopConfigService {

    @Override
    public Page<ShopConfig> pageList(Integer pageNum, Integer pageSize, String configName, Integer status) {
        Page<ShopConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), ShopConfig::getConfigName, configName)
                .eq(status != null, ShopConfig::getStatus, status)
                .orderByDesc(ShopConfig::getCreateTime);
        
        // 添加数据权限过滤 - 按id过滤（即config_id）
        DataScopeUtil.addDataScopeFilter(wrapper, ShopConfig::getId);
        
        return page(page, wrapper);
    }

    @Override
    public ShopConfig getBySnbid(String snbid) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getSnbid, snbid);
        
        // 添加数据权限过滤
        DataScopeUtil.addDataScopeFilter(wrapper, ShopConfig::getId);
        
        return getOne(wrapper);
    }

    @Override
    public List<ShopConfig> listEnabled() {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1);
        
        // 添加数据权限过滤
        DataScopeUtil.addDataScopeFilter(wrapper, ShopConfig::getId);
        
        return list(wrapper);
    }
}
