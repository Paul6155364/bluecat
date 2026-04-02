package com.bluecat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.AreaStatusSnapshot;

import java.util.List;

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
}
