package com.bluecat.service;

import com.bluecat.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 蓝老板API服务接口
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface LaobanApiService {

    /**
     * 测试Token是否有效
     *
     * @param config 网吧配置
     * @return 测试结果
     */
    Map<String, Object> testToken(ShopConfig config);

    /**
     * 获取门店列表(getSnbidInfo)
     *
     * @param config 网吧配置
     * @return 门店列表
     */
    List<Map<String, Object>> getShopList(ShopConfig config);

    /**
     * 获取门店机器信息(get-area-com-set-info)
     *
     * @param config 网吧配置
     * @param shopId 门店ID
     * @param snbid  门店编号
     * @return 机器信息列表
     */
    List<Map<String, Object>> getMachineInfo(ShopConfig config, Long shopId, String snbid);

    /**
     * 获取门店实时状态(general-order-book-area-com-result)
     *
     * @param config  网吧配置
     * @param shopId  门店ID
     * @param snbid   门店编号
     * @return 实时状态数据
     */
    Map<String, Object> getShopStatus(ShopConfig config, Long shopId, String snbid);

    /**
     * 执行完整数据采集
     *
     * @param configId 网吧配置ID
     */
    void executeCollection(Long configId);

    /**
     * 执行所有启用的配置的数据采集
     */
    void executeAllCollection();

    /**
     * 刷新Token
     *
     * @param config 网吧配置
     * @return 新的Token，如果刷新失败返回null
     */
    String refreshToken(ShopConfig config);

    /**
     * 银杏管家-测试连接
     *
     * @param config 网吧配置
     * @return 测试结果
     */
    Map<String, Object> testYinxing(ShopConfig config);
}
