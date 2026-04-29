package com.bluecat.task;

import com.bluecat.service.LaobanApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 网鱼网咖数据采集调度器
 *
 * @author BlueCat
 * @since 2026-04-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WangyuCollectionScheduler {

    private final LaobanApiService laobanApiService;

    /**
     * 每小时执行一次网鱼网咖数据采集
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void hourlyCollection() {
        log.info("开始执行网鱼网咖定时数据采集任务");
        try {
            laobanApiService.executeAllWangyuCollection();
            log.info("网鱼网咖定时数据采集任务完成");
        } catch (Exception e) {
            log.error("网鱼网咖定时数据采集任务失败", e);
        }
    }

    /**
     * 每30分钟执行一次(可选)
     */
    @Scheduled(cron = "0 40 * * * ?")
    public void halfHourlyCollection() {
        log.info("开始执行网鱼网咖半小时数据采集任务");
        try {
            laobanApiService.executeAllWangyuCollection();
            log.info("网鱼网咖半小时数据采集任务完成");
        } catch (Exception e) {
            log.error("网鱼网咖半小时数据采集任务失败", e);
        }
    }
}
