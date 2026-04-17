package com.bluecat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
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
// @EnableAsync 已移至 ThreadPoolConfig
public class BlueCatApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BlueCatApplication.class, args);
        Environment env = context.getEnvironment();
        
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        System.out.println("\n==========================================");
        System.out.println("   BlueCat 网吧管理系统启动成功!");
        System.out.println("==========================================");
        System.out.println("   Swagger文档地址:");
        System.out.println("   Knife4j:  http://localhost:" + port + contextPath + "/doc.html");
        System.out.println("   Swagger:  http://localhost:" + port + contextPath + "/swagger-ui.html");
        System.out.println("   API Docs: http://localhost:" + port + contextPath + "/v2/api-docs");
        System.out.println("==========================================\n");
    }
}
