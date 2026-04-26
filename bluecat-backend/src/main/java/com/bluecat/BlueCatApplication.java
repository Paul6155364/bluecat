package com.bluecat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BlueCat 网吧管理系统启动类
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@SpringBootApplication
@MapperScan("com.bluecat.mapper")
@EnableScheduling
public class BlueCatApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlueCatApplication.class, args);
    }
}
