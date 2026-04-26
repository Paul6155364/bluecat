package com.bluecat.task;

import com.bluecat.service.LaobanApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 银杏管家数据采集调度器
 * 与x管家完全隔离的采集任务
 *
 * @author BlueCat
 * @since 2026-04-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "bluecat.scheduling.enabled", havingValue = "true")
public class YinxingCollectionScheduler {

    private final LaobanApiService laobanApiService;

    /**
     * 每小时执行一次银杏管家数据采集
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void hourlyCollection() {
        log.info("开始执行银杏管家定时数据采集任务");
        try {
            laobanApiService.executeAllYinxingCollection();
            log.info("银杏管家定时数据采集任务完成");
        } catch (Exception e) {
            log.error("银杏管家定时数据采集任务失败", e);
        }
    }

    /**
     * 每30分钟执行一次(可选)
     */
    @Scheduled(cron = "0 35 * * * ?")
    public void halfHourlyCollection() {
        log.info("开始执行银杏管家半小时数据采集任务");
        try {
            laobanApiService.executeAllYinxingCollection();
            log.info("银杏管家半小时数据采集任务完成");
        } catch (Exception e) {
            log.error("银杏管家半小时数据采集任务失败", e);
        }
    }
}
