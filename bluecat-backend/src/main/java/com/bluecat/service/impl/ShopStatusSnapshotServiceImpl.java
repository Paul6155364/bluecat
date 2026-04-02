package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.mapper.ShopStatusSnapshotMapper;
import com.bluecat.service.ShopStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门店实时状态快照表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class ShopStatusSnapshotServiceImpl extends ServiceImpl<ShopStatusSnapshotMapper, ShopStatusSnapshot> implements ShopStatusSnapshotService {

    @Override
    public Page<ShopStatusSnapshot> pageList(Integer pageNum, Integer pageSize, Long shopId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<ShopStatusSnapshot> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, ShopStatusSnapshot::getShopId, shopId)
                .ge(startTime != null, ShopStatusSnapshot::getSnapshotTime, startTime)
                .le(endTime != null, ShopStatusSnapshot::getSnapshotTime, endTime)
                .orderByDesc(ShopStatusSnapshot::getSnapshotTime);
        return page(page, wrapper);
    }

    @Override
    public ShopStatusSnapshot getLatestByShopId(Long shopId) {
        return getOne(new LambdaQueryWrapper<ShopStatusSnapshot>()
                .eq(ShopStatusSnapshot::getShopId, shopId)
                .orderByDesc(ShopStatusSnapshot::getSnapshotTime)
                .last("LIMIT 1"));
    }

    @Override
    public List<ShopStatusSnapshot> listLatestAll() {
        // 获取每个门店最新快照的ID
        return list(new LambdaQueryWrapper<ShopStatusSnapshot>()
                .inSql(ShopStatusSnapshot::getId, 
                        "SELECT MAX(id) FROM shop_status_snapshot GROUP BY shop_id")
                .orderByDesc(ShopStatusSnapshot::getOccupancyRate));
    }

    @Override
    public List<ShopStatusSnapshot> listByShopIdAndTimeRange(Long shopId, LocalDateTime startTime, LocalDateTime endTime) {
        return list(new LambdaQueryWrapper<ShopStatusSnapshot>()
                .eq(ShopStatusSnapshot::getShopId, shopId)
                .ge(startTime != null, ShopStatusSnapshot::getSnapshotTime, startTime)
                .le(endTime != null, ShopStatusSnapshot::getSnapshotTime, endTime)
                .orderByAsc(ShopStatusSnapshot::getSnapshotTime));
    }
}
