package com.bluecat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 用于异步执行采集任务和API日志写入
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 数据采集线程池
     * 用于异步执行采集任务
     */
    @Bean("collectionExecutor")
    public Executor collectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);           // 核心线程数
        executor.setMaxPoolSize(10);           // 最大线程数
        executor.setQueueCapacity(100);        // 队列容量
        executor.setKeepAliveSeconds(60);      // 线程空闲时间
        executor.setThreadNamePrefix("collection-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略：调用者执行
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(60);             // 最多等待60秒
        executor.initialize();
        log.info("数据采集线程池初始化完成: corePoolSize=3, maxPoolSize=10");
        return executor;
    }

    /**
     * API日志写入线程池
     * 用于异步保存API调用日志，不阻塞主流程
     */
    @Bean("apiLogExecutor")
    public Executor apiLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // 核心线程数
        executor.setMaxPoolSize(5);            // 最大线程数
        executor.setQueueCapacity(500);        // 队列容量（日志较多）
        executor.setKeepAliveSeconds(60);      // 线程空闲时间
        executor.setThreadNamePrefix("api-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略：丢弃最旧的任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("API日志线程池初始化完成: corePoolSize=2, maxPoolSize=5");
        return executor;
    }
}
