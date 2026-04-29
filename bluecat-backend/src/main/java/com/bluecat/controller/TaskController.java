package com.bluecat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.entity.ApiCallLog;
import com.bluecat.entity.DataCollectionTask;
import com.bluecat.entity.ShopConfig;
import com.bluecat.service.ApiCallLogService;
import com.bluecat.service.DataCollectionTaskService;
import com.bluecat.service.LaobanApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 采集任务控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "采集任务管理")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final DataCollectionTaskService dataCollectionTaskService;
    private final ApiCallLogService apiCallLogService;
    private final LaobanApiService laobanApiService;
    private final com.bluecat.service.ShopConfigService shopConfigService;

    @ApiOperation("手动触发数据采集-所有配置")
    @PostMapping("/execute")
    public Result<Void> executeAll() {
        laobanApiService.executeAllCollection();
        return Result.success("数据采集任务已触发");
    }

    @ApiOperation("手动触发数据采集-指定配置")
    @PostMapping("/execute/{configId}")
    public Result<Void> executeByConfigId(@PathVariable Long configId) {
        laobanApiService.executeCollection(configId);
        return Result.success("数据采集任务已触发");
    }

    // ========== 银杏管家采集接口 ==========

    @ApiOperation("手动触发银杏管家数据采集-所有配置")
    @PostMapping("/yinxing/execute")
    public Result<Void> executeAllYinxing() {
        laobanApiService.executeAllYinxingCollection();
        return Result.success("银杏管家数据采集任务已触发");
    }

    @ApiOperation("手动触发银杏管家数据采集-指定配置")
    @PostMapping("/yinxing/execute/{configId}")
    public Result<Void> executeYinxingByConfigId(@PathVariable Long configId) {
        laobanApiService.executeYinxingCollection(configId);
        return Result.success("银杏管家数据采集任务已触发");
    }

    // ========== 网鱼网咖采集接口 ==========

    @ApiOperation("手动触发网鱼网咖数据采集-所有配置")
    @PostMapping("/wangyu/execute")
    public Result<Void> executeAllWangyu() {
        laobanApiService.executeAllWangyuCollection();
        return Result.success("网鱼网咖数据采集任务已触发");
    }

    @ApiOperation("手动触发网鱼网咖数据采集-指定配置")
    @PostMapping("/wangyu/execute/{configId}")
    public Result<Void> executeWangyuByConfigId(@PathVariable Long configId) {
        laobanApiService.executeWangyuCollection(configId);
        return Result.success("网鱼网咖数据采集任务已触发");
    }

    @ApiOperation("网鱼网咖-测试连接")
    @PostMapping("/wangyu/test/{configId}")
    public Result<Map<String, Object>> testWangyu(@PathVariable Long configId) {
        ShopConfig config = shopConfigService.getById(configId);
        if (config == null) {
            return Result.error("配置不存在");
        }
        if (config.getPlatformType() != 2) {
            return Result.error("该配置不是网鱼网咖平台");
        }
        Map<String, Object> result = laobanApiService.testWangyu(config);
        if (result != null && result.get("code") != null) {
            Integer code = (Integer) result.get("code");
            if (code == 0) {
                return Result.success("连接成功", result);
            } else {
                return Result.error("连接失败: " + result.get("message"));
            }
        }
        return Result.error("连接失败，请检查网络或配置");
    }

    @ApiOperation("分页查询采集任务")
    @GetMapping("/page")
    public Result<PageResult<DataCollectionTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long configId, String taskType, Integer status) {
        Page<DataCollectionTask> page = dataCollectionTaskService.pageList(pageNum, pageSize, configId, taskType, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询任务详情")
    @GetMapping("/{id}")
    public Result<DataCollectionTask> getById(@PathVariable Long id) {
        return Result.success(dataCollectionTaskService.getById(id));
    }

    @ApiOperation("分页查询API调用日志")
    @GetMapping("/log/page")
    public Result<PageResult<ApiCallLog>> pageLog(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long configId, String apiName) {
        Page<ApiCallLog> page = apiCallLogService.pageList(pageNum, pageSize, configId, apiName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询API调用日志详情")
    @GetMapping("/log/{id}")
    public Result<ApiCallLog> getLogById(@PathVariable Long id) {
        return Result.success(apiCallLogService.getById(id));
    }
}
