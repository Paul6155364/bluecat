package com.bluecat.service;

import com.bluecat.dto.SiteSelectionRequest;
import com.bluecat.dto.SiteSelectionResult;

/**
 * 新店选址服务
 *
 * @author BlueCat
 * @since 2026-04-03
 */
public interface SiteSelectionService {

    /**
     * 分析选址位置
     *
     * @param request 选址请求
     * @return 分析结果
     */
    SiteSelectionResult analyzeSite(SiteSelectionRequest request);
}
