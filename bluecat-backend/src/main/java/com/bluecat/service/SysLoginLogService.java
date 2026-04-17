package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.SysLoginLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务
 *
 * @author BlueCat
 * @since 2026-04-17
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     */
    Page<SysLoginLog> pageList(Integer pageNum, Integer pageSize, String username, 
                               Integer loginStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录登录日志
     */
    void recordLoginLog(Long userId, String username, String realName, String ip, 
                       String browser, String os, Integer loginStatus, String loginMsg);

    /**
     * 获取登录统计
     */
    Map<String, Object> getLoginStats();

    /**
     * 获取最近登录的用户列表
     */
    List<Map<String, Object>> getRecentLoginUsers(Integer limit);
}
