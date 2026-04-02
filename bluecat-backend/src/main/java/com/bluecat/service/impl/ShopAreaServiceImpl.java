package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopArea;
import com.bluecat.mapper.ShopAreaMapper;
import com.bluecat.service.ShopAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店区域表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class ShopAreaServiceImpl extends ServiceImpl<ShopAreaMapper, ShopArea> implements ShopAreaService {

    @Override
    public List<ShopArea> listByShopId(Long shopId) {
        return list(new LambdaQueryWrapper<ShopArea>()
                .eq(ShopArea::getShopId, shopId)
                .orderByAsc(ShopArea::getSortOrder));
    }
}
