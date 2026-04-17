package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ApiCallLog;
import com.bluecat.mapper.ApiCallLogMapper;
import com.bluecat.service.ApiCallLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * API调用日志表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallLogServiceImpl extends ServiceImpl<ApiCallLogMapper, ApiCallLog> implements ApiCallLogService {

    @Override
    public Page<ApiCallLog> pageList(Integer pageNum, Integer pageSize, Long configId, String apiName) {
        Page<ApiCallLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(configId != null, ApiCallLog::getConfigId, configId)
                .like(StringUtils.hasText(apiName), ApiCallLog::getApiName, apiName)
                .orderByDesc(ApiCallLog::getCallTime);
        return page(page, wrapper);
    }
    
    @Override
    @Async("apiLogExecutor")
    public void saveAsync(ApiCallLog apiLog) {
        try {
            save(apiLog);
        } catch (Exception e) {
            log.error("异步保存API日志失败: apiName={}", apiLog.getApiName(), e);
        }
    }
}
