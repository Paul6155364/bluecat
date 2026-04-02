package com.bluecat.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验 -- 排除不需要登录的接口
            SaRouter.match("/**")
                    .notMatch(
                            // 排除登录接口
                            "/auth/login",
                            "/auth/register",
                            // 排除静态资源
                            "/static/**",
                            "/favicon.ico",
                            // 排除API文档
                            "/doc.html",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/v2/api-docs/**",
                            // 排除健康检查
                            "/actuator/**"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
