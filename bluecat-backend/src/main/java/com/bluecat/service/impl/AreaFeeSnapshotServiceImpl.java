package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaFeeSnapshot;
import com.bluecat.mapper.AreaFeeSnapshotMapper;
import com.bluecat.service.AreaFeeSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
