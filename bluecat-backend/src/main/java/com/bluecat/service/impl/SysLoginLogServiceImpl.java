package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.SysLoginLog;
import com.bluecat.entity.SysUser;
import com.bluecat.mapper.SysLoginLogMapper;
import com.bluecat.service.SysLoginLogService;
import com.bluecat.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 登录日志服务实现
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    private final SysUserService sysUserService;

    @Override
    public Page<SysLoginLog> pageList(Integer pageNum, Integer pageSize, String username, 
                                       Integer loginStatus, LocalDateTime startTime, LocalDateTime endTime) {
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                .eq(loginStatus != null, SysLoginLog::getLoginStatus, loginStatus)
                .ge(startTime != null, SysLoginLog::getLoginTime, startTime)
                .le(endTime != null, SysLoginLog::getLoginTime, endTime)
                .orderByDesc(SysLoginLog::getLoginTime);
        return page(page, wrapper);
    }

    @Override
    public void recordLoginLog(Long userId, String username, String realName, String ip, 
                               String browser, String os, Integer loginStatus, String loginMsg) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setRealName(realName);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setLoginIp(ip);
            loginLog.setBrowser(browser);
            loginLog.setOs(os);
            loginLog.setLoginStatus(loginStatus);
            loginLog.setLoginMsg(loginMsg);
            
            // 解析IP归属地（简化处理，实际可接入IP解析服务）
            loginLog.setLoginLocation(parseIpLocation(ip));
            
            save(loginLog);
            
            log.info("记录登录日志: userId={}, username={}, ip={}, status={}", 
                    userId, username, ip, loginStatus);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getLoginStats() {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = todayStart.minusDays(7);
        LocalDateTime monthStart = todayStart.minusDays(30);
        
        // 今日登录次数
        long todayCount = count(new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .eq(SysLoginLog::getLoginStatus, 1));
        
        // 本周登录次数
        long weekCount = count(new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, weekStart)
                .eq(SysLoginLog::getLoginStatus, 1));
        
        // 本月登录次数
        long monthCount = count(new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, monthStart)
                .eq(SysLoginLog::getLoginStatus, 1));
        
        // 今日登录失败次数
        long todayFailCount = count(new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .eq(SysLoginLog::getLoginStatus, 0));
        
        // 活跃用户数（最近30天登录过的用户）
        List<SysLoginLog> logs = list(new LambdaQueryWrapper<SysLoginLog>()
                .select(SysLoginLog::getUserId)
                .ge(SysLoginLog::getLoginTime, monthStart)
                .eq(SysLoginLog::getLoginStatus, 1)
                .isNotNull(SysLoginLog::getUserId)
                .groupBy(SysLoginLog::getUserId));
        long activeUsers = logs.size();
        
        result.put("todayCount", todayCount);
        result.put("weekCount", weekCount);
        result.put("monthCount", monthCount);
        result.put("todayFailCount", todayFailCount);
        result.put("activeUsers", activeUsers);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentLoginUsers(Integer limit) {
        // 查询每个用户最近一次登录记录
        List<SysLoginLog> logs = list(new LambdaQueryWrapper<SysLoginLog>()
                .inSql(SysLoginLog::getId, 
                        "SELECT MAX(id) FROM sys_login_log WHERE login_status = 1 GROUP BY user_id")
                .orderByDesc(SysLoginLog::getLoginTime)
                .last("LIMIT " + limit));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysLoginLog loginLog : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", loginLog.getUserId());
            item.put("username", loginLog.getUsername());
            item.put("realName", loginLog.getRealName());
            item.put("loginTime", loginLog.getLoginTime());
            item.put("loginIp", loginLog.getLoginIp());
            item.put("loginLocation", loginLog.getLoginLocation());
            result.add(item);
        }
        
        return result;
    }

    /**
     * 解析IP归属地（简化版本）
     */
    private String parseIpLocation(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        // 本地IP
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "本地";
        }
        // 实际项目中可接入IP解析服务如：淘宝IP、百度IP等
        return "未知";
    }
}
