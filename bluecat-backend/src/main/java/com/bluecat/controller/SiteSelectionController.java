package com.bluecat.controller;

import com.bluecat.common.Result;
import com.bluecat.dto.SiteSelectionRequest;
import com.bluecat.dto.SiteSelectionResult;
import com.bluecat.service.SiteSelectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 新店选址控制器
 *
 * @author BlueCat
 * @since 2026-04-03
 */
@Slf4j
@Api(tags = "新店选址")
@RestController
@RequestMapping("/site-selection")
@RequiredArgsConstructor
public class SiteSelectionController {

    private final SiteSelectionService siteSelectionService;

    /**
     * 分析选址位置
     */
    @ApiOperation("分析选址位置")
    @PostMapping("/analyze")
    public Result<SiteSelectionResult> analyzeSite(@RequestBody SiteSelectionRequest request) {
        try {
            log.info("选址分析请求: 经度={}, 纬度={}, 半径={}米, 开始时间={}, 结束时间={}", 
                    request.getLongitude(), request.getLatitude(), request.getRadius(),
                    request.getStartTime(), request.getEndTime());
            
            if (request.getLongitude() == null || request.getLatitude() == null) {
                return Result.paramError("经纬度不能为空");
            }
            
            if (request.getRadius() == null || request.getRadius() <= 0) {
                return Result.paramError("分析半径必须大于0");
            }
            
            SiteSelectionResult result = siteSelectionService.analyzeSite(request);
            
            log.info("分析完成: 附近门店{}家, 推荐指数{}", 
                    result.getTotalShops(), result.getRecommendationScore());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("选址分析失败", e);
            // 返回具体错误信息，方便调试
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName();
            }
            return Result.error("选址分析失败: " + errorMsg);
        }
    }

    /**
     * 快速分析（使用默认时间范围：最近30天）
     */
    @ApiOperation("快速分析选址")
    @GetMapping("/quick-analyze")
    public Result<SiteSelectionResult> quickAnalyze(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "1000") Integer radius) {
        
        SiteSelectionRequest request = new SiteSelectionRequest();
        request.setLongitude(new java.math.BigDecimal(longitude.toString()));
        request.setLatitude(new java.math.BigDecimal(latitude.toString()));
        request.setRadius(radius);
        
        // 默认最近30天
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        SiteSelectionResult result = siteSelectionService.analyzeSite(request);
        
        return Result.success(result);
    }
}
