package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopArea;
import com.bluecat.mapper.ShopAreaMapper;
import com.bluecat.service.ShopAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopAreaServiceImpl extends ServiceImpl<ShopAreaMapper, ShopArea> implements ShopAreaService {

    @Override
    public List<ShopArea> listByShopId(Long shopId) {
        LambdaQueryWrapper<ShopArea> wrapper = new LambdaQueryWrapper<ShopArea>()
                .eq(ShopArea::getShopId, shopId)
                .orderByAsc(ShopArea::getSortOrder);
        return list(wrapper);
    }
}
