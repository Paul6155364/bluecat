package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.AreaFeeSnapshot;

import java.util.List;

/**
 * 区域费用快照表 Service
 *
 * @author BlueCat
 * @since 2026-04-18
 */
public interface AreaFeeSnapshotService extends IService<AreaFeeSnapshot> {

    /**
     * 根据门店快照ID查询区域费用快照列表
     */
    List<AreaFeeSnapshot> listBySnapshotId(Long snapshotId);

    /**
     * 批量根据门店快照ID列表查询区域费用快照
     */
    List<AreaFeeSnapshot> listBySnapshotIds(List<Long> snapshotIds);
}
