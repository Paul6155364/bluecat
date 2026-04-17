package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.mapper.AreaStatusSnapshotMapper;
import com.bluecat.service.AreaStatusSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaStatusSnapshotServiceImpl extends ServiceImpl<AreaStatusSnapshotMapper, AreaStatusSnapshot> implements AreaStatusSnapshotService {

    @Override
    public List<AreaStatusSnapshot> listBySnapshotId(Long snapshotId) {
        LambdaQueryWrapper<AreaStatusSnapshot> wrapper = new LambdaQueryWrapper<AreaStatusSnapshot>()
                .eq(AreaStatusSnapshot::getSnapshotId, snapshotId)
                .orderByAsc(AreaStatusSnapshot::getAreaName);
        return list(wrapper);
    }
}
