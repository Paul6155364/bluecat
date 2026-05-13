package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaFeeSnapshot;
import com.bluecat.mapper.AreaFeeSnapshotMapper;
import com.bluecat.service.AreaFeeSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 区域费用快照表 Service 实现类
 *
 * @author BlueCat
 * @since 2026-04-18
 */
@Service
@RequiredArgsConstructor
public class AreaFeeSnapshotServiceImpl extends ServiceImpl<AreaFeeSnapshotMapper, AreaFeeSnapshot> implements AreaFeeSnapshotService {

    @Override
    public List<AreaFeeSnapshot> listBySnapshotId(Long snapshotId) {
        LambdaQueryWrapper<AreaFeeSnapshot> wrapper = new LambdaQueryWrapper<AreaFeeSnapshot>()
                .eq(AreaFeeSnapshot::getSnapshotId, snapshotId)
                .orderByAsc(AreaFeeSnapshot::getAreaName);
        return list(wrapper);
    }

    @Override
    public List<AreaFeeSnapshot> listBySnapshotIds(List<Long> snapshotIds) {
        if (CollectionUtils.isEmpty(snapshotIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AreaFeeSnapshot> wrapper = new LambdaQueryWrapper<AreaFeeSnapshot>()
                .in(AreaFeeSnapshot::getSnapshotId, snapshotIds)
                .orderByAsc(AreaFeeSnapshot::getSnapshotId, AreaFeeSnapshot::getAreaName);
        return list(wrapper);
    }

    /**
     * 根据快照ID分组获取费用列表
     */
    public Map<Long, List<AreaFeeSnapshot>> groupBySnapshotId(List<Long> snapshotIds) {
        List<AreaFeeSnapshot> all = listBySnapshotIds(snapshotIds);
        return all.stream().collect(Collectors.groupingBy(AreaFeeSnapshot::getSnapshotId));
    }
}
