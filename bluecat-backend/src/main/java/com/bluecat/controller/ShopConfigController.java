package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.entity.ShopConfig;
import com.bluecat.service.LaobanApiService;
import com.bluecat.service.ShopConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 网吧配置控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "网吧配置管理")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ShopConfigController {

    private final ShopConfigService shopConfigService;
    private final LaobanApiService laobanApiService;

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult<ShopConfig>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String configName, Integer status) {
        Page<ShopConfig> page = shopConfigService.pageList(pageNum, pageSize, configName, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询所有启用的配置")
    @GetMapping("/list/enabled")
    public Result<?> listEnabled() {
        return Result.success(shopConfigService.listEnabled());
    }

    @ApiOperation("根据ID查询")
    @GetMapping("/{id}")
    public Result<ShopConfig> getById(@PathVariable Long id) {
        return Result.success(shopConfigService.getById(id));
    }

    @ApiOperation("测试Token是否有效")
    @PostMapping("/test-token/{id}")
    public Result<Map<String, Object>> testToken(@PathVariable Long id) {
        ShopConfig config = shopConfigService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        Map<String, Object> result = laobanApiService.testToken(config);
        if (result != null && result.get("code") != null) {
            Integer code = (Integer) result.get("code");
            if (code == 0) {
                return Result.success("Token有效", result);
            } else {
                return Result.error(401, "Token已失效或无效: " + result.get("msg"));
            }
        }
        return Result.error("Token测试失败，请检查网络或配置");
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh-token/{id}")
    public Result<Void> refreshToken(@PathVariable Long id) {
        ShopConfig config = shopConfigService.getById(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        if (config.getJwtToken() == null || config.getJwtToken().isEmpty()) {
            return Result.error("当前配置没有Token，无法刷新");
        }

        String newToken = laobanApiService.refreshToken(config);
        if (newToken != null && !newToken.isEmpty()) {
            // 更新数据库中的Token和过期时间（新Token有效期2小时）
            config.setJwtToken(newToken);
            config.setTokenExpireTime(java.time.LocalDateTime.now().plusHours(2));
            shopConfigService.updateById(config);
            return Result.success("Token刷新成功，新Token有效期2小时");
        } else {
            return Result.error("Token刷新失败，原Token可能已过期，请重新获取");
        }
    }

    @ApiOperation("新增配置")
    @PostMapping
    public Result<Void> save(@RequestBody ShopConfig config) {
        // 检查snbid是否已存在
        if (shopConfigService.getBySnbid(config.getSnbid()) != null) {
            return Result.fail("该snbid已存在");
        }
        config.setCreateBy(StpUtil.getLoginIdAsString());
        // 自动设置Token过期时间为当前时间+60分钟
        if (config.getTokenExpireTime() == null && config.getJwtToken() != null && !config.getJwtToken().isEmpty()) {
            config.setTokenExpireTime(java.time.LocalDateTime.now().plusMinutes(60));
        }
        shopConfigService.save(config);
        return Result.success("新增成功");
    }

    @ApiOperation("更新配置")
    @PutMapping
    public Result<Void> update(@RequestBody ShopConfig config) {
        ShopConfig exist = shopConfigService.getBySnbid(config.getSnbid());
        if (exist != null && !exist.getId().equals(config.getId())) {
            return Result.fail("该snbid已存在");
        }
        shopConfigService.updateById(config);
        return Result.success("更新成功");
    }

    @ApiOperation("删除配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopConfigService.removeById(id);
        return Result.success("删除成功");
    }

    @ApiOperation("启用/禁用配置")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ShopConfig config = new ShopConfig();
        config.setId(id);
        config.setStatus(status);
        shopConfigService.updateById(config);
        return Result.success(status == 1 ? "启用成功" : "禁用成功");
    }
}
