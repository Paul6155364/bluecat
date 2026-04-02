package com.bluecat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.entity.MachineInfo;
import com.bluecat.service.MachineInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机器控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "机器管理")
@RestController
@RequestMapping("/machine")
@RequiredArgsConstructor
public class MachineController {

    private final MachineInfoService machineInfoService;

    @ApiOperation("分页查询机器")
    @GetMapping("/page")
    public Result<PageResult<MachineInfo>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long shopId, String areaName, String comName) {
        Page<MachineInfo> page = machineInfoService.pageList(pageNum, pageSize, shopId, areaName, comName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询门店所有机器")
    @GetMapping("/list/{shopId}")
    public Result<List<MachineInfo>> listByShopId(@PathVariable Long shopId) {
        return Result.success(machineInfoService.listByShopId(shopId));
    }

    @ApiOperation("根据ID查询机器")
    @GetMapping("/{id}")
    public Result<MachineInfo> getById(@PathVariable Long id) {
        return Result.success(machineInfoService.getById(id));
    }

    @ApiOperation("新增机器")
    @PostMapping
    public Result<Void> save(@RequestBody MachineInfo machineInfo) {
        machineInfoService.save(machineInfo);
        return Result.success("新增成功");
    }

    @ApiOperation("更新机器")
    @PutMapping
    public Result<Void> update(@RequestBody MachineInfo machineInfo) {
        machineInfoService.updateById(machineInfo);
        return Result.success("更新成功");
    }

    @ApiOperation("删除机器")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        machineInfoService.removeById(id);
        return Result.success("删除成功");
    }
}
