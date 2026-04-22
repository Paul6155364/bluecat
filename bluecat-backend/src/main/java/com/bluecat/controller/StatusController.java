package com.bluecat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.entity.AreaFeeSnapshot;
import com.bluecat.entity.AreaStatusSnapshot;
import com.bluecat.entity.MachineStatusHistory;
import com.bluecat.entity.ShopConfig;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.service.AreaFeeSnapshotService;
import com.bluecat.service.AreaStatusSnapshotService;
import com.bluecat.service.MachineStatusHistoryService;
import com.bluecat.service.ShopConfigService;
import com.bluecat.service.ShopInfoService;
import com.bluecat.service.ShopStatusSnapshotService;
import com.bluecat.entity.ShopInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 状态快照控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "状态快照管理")
@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusController {

    private final ShopStatusSnapshotService shopStatusSnapshotService;
    private final AreaStatusSnapshotService areaStatusSnapshotService;
    private final AreaFeeSnapshotService areaFeeSnapshotService;
    private final MachineStatusHistoryService machineStatusHistoryService;
    private final ShopInfoService shopInfoService;
    private final ShopConfigService shopConfigService;

    @ApiOperation("获取所有门店最新状态")
    @GetMapping("/realtime")
    public Result<List<Map<String, Object>>> listRealtime(Long configId, Long shopId) {
        List<ShopStatusSnapshot> snapshots = shopStatusSnapshotService.listLatestAll();

        // 转换为带门店信息的列表
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ShopStatusSnapshot snapshot : snapshots) {
            ShopInfo shop = shopInfoService.getById(snapshot.getShopId());
            if (shop == null) continue;

            // 按配置筛选
            if (configId != null && !configId.equals(shop.getConfigId())) continue;
            // 按门店筛选
            if (shopId != null && !shopId.equals(snapshot.getShopId())) continue;

            // 获取网咖配置名称
            ShopConfig config = shopConfigService.getById(shop.getConfigId());
            String configName = config != null ? config.getConfigName() : "-";

            Map<String, Object> item = new HashMap<>();
            item.put("id", snapshot.getId());
            item.put("shopId", snapshot.getShopId());
            item.put("configId", shop.getConfigId());
            item.put("configName", configName);
            item.put("snapshotTime", snapshot.getSnapshotTime());
            item.put("totalMachines", snapshot.getTotalMachines());
            item.put("freeMachines", snapshot.getFreeMachines());
            item.put("busyMachines", snapshot.getBusyMachines());
            item.put("occupancyRate", snapshot.getOccupancyRate());
            item.put("remain", snapshot.getRemain());
            item.put("dayRevenue", snapshot.getDayRevenue());
            // 费用摘要：最低费率、最高费率、区域费用列表
            List<AreaFeeSnapshot> feeList = areaFeeSnapshotService.listBySnapshotId(snapshot.getId());
            item.put("areaFeeList", feeList);
            if (!feeList.isEmpty()) {
                java.math.BigDecimal minRate = feeList.stream()
                        .map(AreaFeeSnapshot::getRate)
                        .filter(r -> r != null)
                        .min(java.math.BigDecimal::compareTo).orElse(null);
                java.math.BigDecimal maxRate = feeList.stream()
                        .map(AreaFeeSnapshot::getRate)
                        .filter(r -> r != null)
                        .max(java.math.BigDecimal::compareTo).orElse(null);
                item.put("minRate", minRate);
                item.put("maxRate", maxRate);
            }
            item.put("shop", shop);
            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("获取门店最新状态")
    @GetMapping("/realtime/{shopId}")
    public Result<Map<String, Object>> getRealtimeByShopId(@PathVariable Long shopId) {
        Map<String, Object> result = new HashMap<>();
        
        // 门店信息
        result.put("shop", shopInfoService.getById(shopId));
        
        // 最新快照
        ShopStatusSnapshot snapshot = shopStatusSnapshotService.getLatestByShopId(shopId);
        result.put("snapshot", snapshot);
        
        // 区域状态
        if (snapshot != null) {
            List<AreaStatusSnapshot> areas = areaStatusSnapshotService.listBySnapshotId(snapshot.getId());
            result.put("areas", areas);
            // 区域费用
            List<AreaFeeSnapshot> feeList = areaFeeSnapshotService.listBySnapshotId(snapshot.getId());
            result.put("areaFeeList", feeList);
        }
        
        return Result.success(result);
    }

    @ApiOperation("分页查询门店快照")
    @GetMapping("/snapshot/page")
    public Result<Map<String, Object>> pageSnapshot(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long configId,
            Long shopId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<ShopStatusSnapshot> page = shopStatusSnapshotService.pageList(pageNum, pageSize, shopId, startTime, endTime);
        
        // 转换为带门店信息的列表
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (ShopStatusSnapshot snapshot : page.getRecords()) {
            ShopInfo shop = shopInfoService.getById(snapshot.getShopId());
            if (shop == null) continue;
            
            // 按配置筛选
            if (configId != null && !configId.equals(shop.getConfigId())) continue;
            
            // 获取网咖配置名称
            ShopConfig config = shopConfigService.getById(shop.getConfigId());
            String configName = config != null ? config.getConfigName() : "-";
            
            Map<String, Object> item = new HashMap<>();
            item.put("id", snapshot.getId());
            item.put("shopId", snapshot.getShopId());
            item.put("configId", shop.getConfigId());
            item.put("configName", configName);
            item.put("shopName", shop.getName());
            item.put("totalMachines", snapshot.getTotalMachines());
            item.put("freeMachines", snapshot.getFreeMachines());
            item.put("busyMachines", snapshot.getBusyMachines());
            item.put("occupancyRate", snapshot.getOccupancyRate());
            item.put("snapshotTime", snapshot.getSnapshotTime());
            records.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return Result.success(result);
    }

    @ApiOperation("查询快照详情(包含区域和机器)")
    @GetMapping("/snapshot/{snapshotId}")
    public Result<Map<String, Object>> getSnapshotDetail(@PathVariable Long snapshotId) {
        Map<String, Object> result = new HashMap<>();
        
        ShopStatusSnapshot snapshot = shopStatusSnapshotService.getById(snapshotId);
        result.put("snapshot", snapshot);
        
        if (snapshot != null) {
            // 区域状态
            result.put("areas", areaStatusSnapshotService.listBySnapshotId(snapshotId));
            // 机器状态
            result.put("machines", machineStatusHistoryService.listBySnapshotId(snapshotId));
            // 区域费用
            result.put("areaFeeList", areaFeeSnapshotService.listBySnapshotId(snapshotId));
        }
        
        return Result.success(result);
    }

    @ApiOperation("分页查询机器状态历史")
    @GetMapping("/machine/history")
    public Result<PageResult<MachineStatusHistory>> pageMachineHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long shopId, Long machineId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<MachineStatusHistory> page = machineStatusHistoryService.pageList(pageNum, pageSize, shopId, machineId, startTime, endTime);
        return Result.success(PageResult.of(page));
    }
}
