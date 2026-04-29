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
     * 银杏管家舱位信息结果（包含房间列表和扩展信息）
     */
    class YinxingRoomResult {
        private List<Map<String, Object>> roomList;
        private Map<String, Object> ext;

        public YinxingRoomResult() {}

        public YinxingRoomResult(List<Map<String, Object>> roomList, Map<String, Object> ext) {
            this.roomList = roomList;
            this.ext = ext;
        }

        public List<Map<String, Object>> getRoomList() { return roomList; }
        public void setRoomList(List<Map<String, Object>> roomList) { this.roomList = roomList; }
        public Map<String, Object> getExt() { return ext; }
        public void setExt(Map<String, Object> ext) { this.ext = ext; }
    }

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

    /**
     * 银杏管家-获取门店列表
     *
     * @param config 网吧配置
     * @return 门店列表
     */
    List<Map<String, Object>> getYinxingShopList(ShopConfig config);

    /**
     * 银杏管家-获取门店详情
     *
     * @param config  网吧配置
     * @param chainId 门店chain_id
     * @return 门店详情
     */
    Map<String, Object> getYinxingShopDetail(ShopConfig config, String chainId);

    /**
     * 银杏管家-获取舱位机器信息
     *
     * @param config  网吧配置
     * @param shopId  门店ID
     * @param chainId 门店chain_id
     * @param mchId   商户ID(mch_id)
     * @return 舱位信息结果（包含roomList和ext）
     */
    YinxingRoomResult getYinxingRoomInfo(ShopConfig config, Long shopId, String chainId, Long mchId);

    /**
     * 银杏管家-执行采集任务
     *
     * @param configId 网吧配置ID
     */
    void executeYinxingCollection(Long configId);

    /**
     * 银杏管家-执行所有银杏管家配置的采集
     */
    void executeAllYinxingCollection();

    // ========== 网鱼网咖采集接口 ==========

    /**
     * 网鱼网咖-测试连接
     *
     * @param config 网吧配置
     * @return 测试结果
     */
    Map<String, Object> testWangyu(ShopConfig config);

    /**
     * 网鱼网咖-获取门店列表
     *
     * @param config  网吧配置
     * @param keyword 搜索关键词（可选）
     * @param page    页码
     * @param pageSize 每页条数
     * @return 门店列表
     */
    Map<String, Object> getWangyuShopList(ShopConfig config, String keyword, Integer page, Integer pageSize);

    /**
     * 网鱼网咖-执行采集任务
     *
     * @param configId 网吧配置ID
     */
    void executeWangyuCollection(Long configId);

    /**
     * 网鱼网咖-获取门店座位布局
     * 接口: POST /surf-internet/shop/v3/get
     * 返回elements[]数组，包含PRIVATE_ROOM(包间)、SEAT(座位)、SPACE(过道)等元素
     * 每个PRIVATE_ROOM包含clientInfo数组，clientNo为机器编号，status为状态(0=空闲,1=占用)
     *
     * @param config    网吧配置
     * @param storeCode 门店编码(commonCode)
     * @return 座位布局数据（包含elements数组）
     */
    Map<String, Object> getWangyuShopLayout(ShopConfig config, String storeCode);

    /**
     * 网鱼网咖-执行所有网鱼网咖配置的采集
     */
    void executeAllWangyuCollection();
}
