package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.MachineStatusHistory;
import com.bluecat.mapper.MachineStatusHistoryMapper;
import com.bluecat.service.MachineStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器实时状态历史表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class MachineStatusHistoryServiceImpl extends ServiceImpl<MachineStatusHistoryMapper, MachineStatusHistory> implements MachineStatusHistoryService {

    @Override
    public Page<MachineStatusHistory> pageList(Integer pageNum, Integer pageSize, Long shopId, Long machineId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<MachineStatusHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MachineStatusHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, MachineStatusHistory::getShopId, shopId)
                .eq(machineId != null, MachineStatusHistory::getMachineId, machineId)
                .ge(startTime != null, MachineStatusHistory::getSnapshotTime, startTime)
                .le(endTime != null, MachineStatusHistory::getSnapshotTime, endTime)
                .orderByDesc(MachineStatusHistory::getSnapshotTime);
        return page(page, wrapper);
    }

    @Override
    public List<MachineStatusHistory> listBySnapshotId(Long snapshotId) {
        return list(new LambdaQueryWrapper<MachineStatusHistory>()
                .eq(MachineStatusHistory::getSnapshotId, snapshotId)
                .orderByAsc(MachineStatusHistory::getComName));
    }
}
