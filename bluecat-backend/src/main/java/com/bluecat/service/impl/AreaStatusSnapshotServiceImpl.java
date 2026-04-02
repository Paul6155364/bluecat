package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.mapper.AreaStatusSnapshotMapper;
import com.bluecat.service.AreaStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 区域实时状态快照表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class AreaStatusSnapshotServiceImpl extends ServiceImpl<AreaStatusSnapshotMapper, AreaStatusSnapshot> implements AreaStatusSnapshotService {

    @Override
    public List<AreaStatusSnapshot> listBySnapshotId(Long snapshotId) {
        return list(new LambdaQueryWrapper<AreaStatusSnapshot>()
                .eq(AreaStatusSnapshot::getSnapshotId, snapshotId)
                .orderByAsc(AreaStatusSnapshot::getAreaName));
    }
}
