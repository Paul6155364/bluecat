package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopArea;
import com.bluecat.mapper.ShopAreaMapper;
import com.bluecat.service.ShopAreaService;
import com.bluecat.util.DataScopeUtil;
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
        LambdaQueryWrapper<ShopArea> wrapper = new LambdaQueryWrapper<ShopArea>()
                .eq(ShopArea::getShopId, shopId)
                .orderByAsc(ShopArea::getSortOrder);
        
        // 添加数据权限过滤
        DataScopeUtil.addDataScopeFilterByShopId(wrapper, ShopArea::getShopId);
        
        return list(wrapper);
    }
}
