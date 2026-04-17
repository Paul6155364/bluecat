package com.bluecat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.Result;
import com.bluecat.entity.SysLoginLog;
import com.bluecat.service.SysLoginLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志控制器
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Api(tags = "登录日志")
@RestController
@RequestMapping("/system/login-log")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    @ApiOperation("分页查询登录日志")
    @GetMapping("/page")
    public Result<Page<SysLoginLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer loginStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Page<SysLoginLog> page = sysLoginLogService.pageList(pageNum, pageSize, username, loginStatus, startTime, endTime);
        return Result.success(page);
    }

    @ApiOperation("获取登录统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(sysLoginLogService.getLoginStats());
    }

    @ApiOperation("获取最近登录用户")
    @GetMapping("/recent")
    public Result<List<Map<String, Object>>> getRecentUsers(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(sysLoginLogService.getRecentLoginUsers(limit));
    }

    @ApiOperation("删除登录日志")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysLoginLogService.removeByIds(ids);
        return Result.success("删除成功");
    }

    @ApiOperation("清空登录日志")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        sysLoginLogService.remove(new LambdaQueryWrapper<>());
        return Result.success("清空成功");
    }
}
