package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.mapper.ShopStatusSnapshotMapper;
import com.bluecat.service.ShopStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, ShopStatusSnapshot> mapLatestByShopIds(List<Long> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        // 查询所有门店的最新快照，然后按shopId分组取最新
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<ShopStatusSnapshot>()
                .in(ShopStatusSnapshot::getShopId, shopIds)
                .orderByDesc(ShopStatusSnapshot::getSnapshotTime);

        List<ShopStatusSnapshot> all = list(wrapper);

        // 按shopId分组，每组取第一个（最新的）
        return all.stream()
                .collect(Collectors.groupingBy(ShopStatusSnapshot::getShopId))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .max(Comparator.comparing(ShopStatusSnapshot::getSnapshotTime))
                                .orElse(null)
                ));
    }

    @Override
    public Map<Long, List<ShopStatusSnapshot>> mapByShopIdsAndTimeRange(List<Long> shopIds, LocalDateTime startTime, LocalDateTime endTime) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ShopStatusSnapshot> wrapper = new LambdaQueryWrapper<ShopStatusSnapshot>()
                .in(ShopStatusSnapshot::getShopId, shopIds)
                .ge(startTime != null, ShopStatusSnapshot::getSnapshotTime, startTime)
                .le(endTime != null, ShopStatusSnapshot::getSnapshotTime, endTime)
                .orderByAsc(ShopStatusSnapshot::getSnapshotTime);

        List<ShopStatusSnapshot> all = list(wrapper);

        return all.stream()
                .collect(Collectors.groupingBy(ShopStatusSnapshot::getShopId));
    }
}
