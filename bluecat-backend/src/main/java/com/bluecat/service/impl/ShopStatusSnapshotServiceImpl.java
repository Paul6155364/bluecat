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
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<ShopStatusSnapshot>()
                .eq(ShopStatusSnapshot::getShopId, shopId)
                .orderByDesc(ShopStatusSnapshot::getSnapshotTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public List<ShopStatusSnapshot> listLatestAll() {
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<ShopStatusSnapshot>()
                .inSql(ShopStatusSnapshot::getId, 
                        "SELECT id FROM shop_status_snapshot WHERE (shop_id, snapshot_time) IN (SELECT shop_id, MAX(snapshot_time) FROM shop_status_snapshot GROUP BY shop_id)")
                .orderByDesc(ShopStatusSnapshot::getOccupancyRate);
        return list(wrapper);
    }

    @Override
    public List<ShopStatusSnapshot> listByShopIdAndTimeRange(Long shopId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<ShopStatusSnapshot>()
                .eq(ShopStatusSnapshot::getShopId, shopId)
                .ge(startTime != null, ShopStatusSnapshot::getSnapshotTime, startTime)
                .le(endTime != null, ShopStatusSnapshot::getSnapshotTime, endTime)
                .orderByAsc(ShopStatusSnapshot::getSnapshotTime);
        return list(wrapper);
    }
}
