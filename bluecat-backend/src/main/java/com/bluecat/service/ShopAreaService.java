package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ShopArea;

import java.util.List;

/**
 * 门店区域表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface ShopAreaService extends IService<ShopArea> {

    /**
     * 根据门店ID查询区域列表
     */
    List<ShopArea> listByShopId(Long shopId);
}
