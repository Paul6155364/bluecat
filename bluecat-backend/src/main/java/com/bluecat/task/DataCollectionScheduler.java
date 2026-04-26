package com.bluecat.task;

import com.bluecat.service.LaobanApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据采集定时任务
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "bluecat.scheduling.enabled", havingValue = "true")
public class DataCollectionScheduler {

    private final LaobanApiService laobanApiService;

    /**
     * 每小时执行一次数据采集
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyCollection() {
        log.info("开始执行定时数据采集任务");
        try {
            laobanApiService.executeAllCollection();
            log.info("定时数据采集任务完成");
        } catch (Exception e) {
            log.error("定时数据采集任务失败", e);
        }
    }

    /**
     * 每30分钟执行一次(可选)
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void halfHourlyCollection() {
        log.info("开始执行半小时数据采集任务");
        try {
            laobanApiService.executeAllCollection();
            log.info("半小时数据采集任务完成");
        } catch (Exception e) {
            log.error("半小时数据采集任务失败", e);
        }
    }
}
