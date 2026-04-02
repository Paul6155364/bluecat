package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ShopInfo;

import java.util.List;

/**
 * 门店信息表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface ShopInfoService extends IService<ShopInfo> {

    /**
     * 分页查询
     */
    Page<ShopInfo> pageList(Integer pageNum, Integer pageSize, Long configId, String name);

    /**
     * 根据配置ID查询门店列表
     */
    List<ShopInfo> listByConfigId(Long configId);

    /**
     * 根据snbid查询
     */
    ShopInfo getBySnbid(String snbid);
}
