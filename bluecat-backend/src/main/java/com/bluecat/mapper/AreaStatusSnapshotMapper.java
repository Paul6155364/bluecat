package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.AreaStatusSnapshot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区域实时状态快照表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface AreaStatusSnapshotMapper extends BaseMapper<AreaStatusSnapshot> {
}
