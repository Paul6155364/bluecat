package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.ShopStatusSnapshot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店实时状态快照表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface ShopStatusSnapshotMapper extends BaseMapper<ShopStatusSnapshot> {
}
