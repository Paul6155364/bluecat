package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.MachineStatusHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 机器实时状态历史表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface MachineStatusHistoryMapper extends BaseMapper<MachineStatusHistory> {
}
