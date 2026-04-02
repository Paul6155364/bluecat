package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.MachineStatusHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器实时状态历史表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface MachineStatusHistoryService extends IService<MachineStatusHistory> {

    /**
     * 分页查询
     */
    Page<MachineStatusHistory> pageList(Integer pageNum, Integer pageSize, Long shopId, Long machineId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据快照ID查询
     */
    List<MachineStatusHistory> listBySnapshotId(Long snapshotId);
}
