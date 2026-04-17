package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ShopPkRelation;

import java.util.List;

/**
 * 门店PK关系服务接口
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public interface ShopPkRelationService extends IService<ShopPkRelation> {

    /**
     * 获取用户的PK关系列表
     */
    List<ShopPkRelation> listByUserId(Long userId);
}
