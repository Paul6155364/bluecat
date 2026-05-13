package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.AreaStatusSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 区域实时状态快照表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface AreaStatusSnapshotService extends IService<AreaStatusSnapshot> {

    /**
     * 根据门店快照ID查询区域快照列表
     */
    List<AreaStatusSnapshot> listBySnapshotId(Long snapshotId);

    /**
     * 批量查询门店在指定时间范围内的区域快照数据
     */
    List<AreaStatusSnapshot> listByShopIdsAndTimeRange(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取门店每日24小时上座率数据
     * 返回结构: Map<shopId, Map<date, Map<hour, avgRate>>>
     */
    Map<Long, Map<String, Map<Integer, Double>>> getHourlyOccupancyByShops(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime);
}
