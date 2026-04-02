package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ShopConfig;

import java.util.List;

/**
 * 网吧配置表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface ShopConfigService extends IService<ShopConfig> {

    /**
     * 分页查询
     */
    Page<ShopConfig> pageList(Integer pageNum, Integer pageSize, String configName, Integer status);

    /**
     * 根据snbid查询
     */
    ShopConfig getBySnbid(String snbid);

    /**
     * 获取所有启用的配置
     */
    List<ShopConfig> listEnabled();
}
