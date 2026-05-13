package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ShopStatusSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门店实时状态快照表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface ShopStatusSnapshotService extends IService<ShopStatusSnapshot> {

    /**
     * 分页查询
     */
    Page<ShopStatusSnapshot> pageList(Integer pageNum, Integer pageSize, Long shopId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取门店最新快照
     */
    ShopStatusSnapshot getLatestByShopId(Long shopId);

    /**
     * 获取所有门店最新快照
     */
    List<ShopStatusSnapshot> listLatestAll();

    /**
     * 获取门店指定时间范围内的快照
     */
    List<ShopStatusSnapshot> listByShopIdAndTimeRange(Long shopId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量获取多个门店的最新快照
     */
    Map<Long, ShopStatusSnapshot> mapLatestByShopIds(List<Long> shopIds);

    /**
     * 批量获取多个门店指定时间范围内的快照
     */
    Map<Long, List<ShopStatusSnapshot>> mapByShopIdsAndTimeRange(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime);
}
