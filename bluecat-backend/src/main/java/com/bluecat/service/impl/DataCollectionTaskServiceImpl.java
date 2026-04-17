package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.DataCollectionTask;
import com.bluecat.mapper.DataCollectionTaskMapper;
import com.bluecat.service.DataCollectionTaskService;
import com.bluecat.util.DataScopeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 数据采集任务表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class DataCollectionTaskServiceImpl extends ServiceImpl<DataCollectionTaskMapper, DataCollectionTask> implements DataCollectionTaskService {

    @Override
    public Page<DataCollectionTask> pageList(Integer pageNum, Integer pageSize, Long configId, String taskType, Integer status) {
        Page<DataCollectionTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DataCollectionTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(configId != null, DataCollectionTask::getConfigId, configId)
                .eq(StringUtils.hasText(taskType), DataCollectionTask::getTaskType, taskType)
                .eq(status != null, DataCollectionTask::getStatus, status)
                .orderByDesc(DataCollectionTask::getCreateTime);
        
        // 添加数据权限过滤 - 按config_id过滤
        DataScopeUtil.addDataScopeFilter(wrapper, DataCollectionTask::getConfigId);
        
        return page(page, wrapper);
    }
}
