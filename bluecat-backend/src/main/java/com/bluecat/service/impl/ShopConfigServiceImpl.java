package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopConfig;
import com.bluecat.mapper.ShopConfigMapper;
import com.bluecat.service.ShopConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

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
        return page(page, wrapper);
    }

    @Override
    public ShopConfig getBySnbid(String snbid) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getSnbid, snbid);
        return getOne(wrapper);
    }

    @Override
    public List<ShopConfig> listEnabled() {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1)
                .eq(ShopConfig::getPlatformType, 0);
        return list(wrapper);
    }
}
