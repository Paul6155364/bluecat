package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.MachineBanList;
import org.apache.ibatis.annotations.Mapper;

/**
 * 禁止预订机器表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface MachineBanListMapper extends BaseMapper<MachineBanList> {
}
