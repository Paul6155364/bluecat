package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopPkRelation;
import com.bluecat.mapper.ShopPkRelationMapper;
import com.bluecat.service.ShopPkRelationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 门店PK关系服务实现
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Service
public class ShopPkRelationServiceImpl extends ServiceImpl<ShopPkRelationMapper, ShopPkRelation> implements ShopPkRelationService {

    @Override
    public List<ShopPkRelation> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<ShopPkRelation>()
                .eq(ShopPkRelation::getCreateBy, userId)
                .eq(ShopPkRelation::getDeleted, 0)
                .orderByDesc(ShopPkRelation::getCreateTime));
    }
}
