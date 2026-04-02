package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.ApiCallLog;

/**
 * API调用日志表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface ApiCallLogService extends IService<ApiCallLog> {

    /**
     * 分页查询
     */
    Page<ApiCallLog> pageList(Integer pageNum, Integer pageSize, Long configId, String apiName);
}
