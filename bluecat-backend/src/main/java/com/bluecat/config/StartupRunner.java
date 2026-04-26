package com.bluecat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 项目启动后输出接口地址
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Component
public class StartupRunner implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void run(String... args) {

        log.info("\n----------------------------------------------------------\n" +
                "\t项目启动成功！\n" +
                "\t接口文档地址:\n" +
                "\tKnife4j文档: \thttp://127.0.0.1:{}{}/doc.html\n" +
                "\tSwagger UI: \thttp://127.0.0.1:{}{}/swagger-ui/index.html\n" +
                "\tOpenAPI JSON: \thttp://127.0.0.1:{}{}/v3/api-docs\n" +
                "----------------------------------------------------------",
                port, contextPath, port, contextPath, port, contextPath);
    }
}
