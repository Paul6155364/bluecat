package com.bluecat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.entity.ShopArea;
import com.bluecat.entity.ShopInfo;
import com.bluecat.service.ShopAreaService;
import com.bluecat.service.ShopInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "门店管理")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopInfoService shopInfoService;
    private final ShopAreaService shopAreaService;

    @ApiOperation("分页查询门店")
    @GetMapping("/page")
    public Result<PageResult<ShopInfo>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Long configId, String name) {
        Page<ShopInfo> page = shopInfoService.pageList(pageNum, pageSize, configId, name);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询所有门店")
    @GetMapping("/list")
    public Result<List<ShopInfo>> list(Long configId) {
        return Result.success(shopInfoService.listByConfigId(configId));
    }

    @ApiOperation("根据ID查询门店")
    @GetMapping("/{id}")
    public Result<ShopInfo> getById(@PathVariable Long id) {
        return Result.success(shopInfoService.getById(id));
    }

    @ApiOperation("查询门店区域列表")
    @GetMapping("/area/{shopId}")
    public Result<List<ShopArea>> listAreas(@PathVariable Long shopId) {
        return Result.success(shopAreaService.listByShopId(shopId));
    }

    @ApiOperation("新增门店")
    @PostMapping
    public Result<Void> save(@RequestBody ShopInfo shopInfo) {
        shopInfoService.save(shopInfo);
        return Result.success("新增成功");
    }

    @ApiOperation("更新门店")
    @PutMapping
    public Result<Void> update(@RequestBody ShopInfo shopInfo) {
        shopInfoService.updateById(shopInfo);
        return Result.success("更新成功");
    }

    @ApiOperation("删除门店")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopInfoService.removeById(id);
        return Result.success("删除成功");
    }
}
