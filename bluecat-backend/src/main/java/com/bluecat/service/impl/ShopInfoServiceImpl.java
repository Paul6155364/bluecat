package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopInfo;
import com.bluecat.mapper.ShopInfoMapper;
import com.bluecat.service.ShopInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 门店信息表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class ShopInfoServiceImpl extends ServiceImpl<ShopInfoMapper, ShopInfo> implements ShopInfoService {

    @Override
    public Page<ShopInfo> pageList(Integer pageNum, Integer pageSize, Long configId, String name) {
        Page<ShopInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(configId != null, ShopInfo::getConfigId, configId)
                .like(StringUtils.hasText(name), ShopInfo::getName, name)
                .orderByDesc(ShopInfo::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public List<ShopInfo> listByConfigId(Long configId) {
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getConfigId, configId);
        
        return list(wrapper);
    }

    @Override
    public ShopInfo getBySnbid(String snbid) {
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<ShopInfo>()
                .eq(ShopInfo::getSnbid, snbid);
        
        return getOne(wrapper);
    }
}
