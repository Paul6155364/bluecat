package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.DataCollectionTask;

/**
 * 数据采集任务表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface DataCollectionTaskService extends IService<DataCollectionTask> {

    /**
     * 分页查询
     */
    Page<DataCollectionTask> pageList(Integer pageNum, Integer pageSize, Long configId, String taskType, Integer status);
}
