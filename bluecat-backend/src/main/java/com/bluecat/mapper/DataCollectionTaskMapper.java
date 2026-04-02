package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.DataCollectionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据采集任务表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface DataCollectionTaskMapper extends BaseMapper<DataCollectionTask> {
}
