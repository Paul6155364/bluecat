package com.bluecat.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.bluecat.annotation.DataScope;
import com.bluecat.common.DataScopeContext;
import com.bluecat.entity.SysUser;
import com.bluecat.service.SysUserService;
import com.bluecat.service.SysUserConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限切面
 * 拦截标注了@DataScope注解的方法，自动注入数据权限过滤条件
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysUserService sysUserService;
    private final SysUserConfigService sysUserConfigService;

    /**
     * 数据权限过滤
     */
    @Around("@annotation(dataScope)")
    public Object doAround(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        // 如果未启用数据权限过滤，直接执行方法
        if (!dataScope.enabled()) {
            return point.proceed();
        }

        // 检查是否登录
        if (!StpUtil.isLogin()) {
            return point.proceed();
        }

        // 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("数据权限过滤 - 用户ID: {}", userId);

        // 查询用户信息
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            log.warn("数据权限过滤 - 用户不存在: {}", userId);
            return point.proceed();
        }

        // 判断是否超级管理员（data_scope = 2 表示全部数据权限）
        if (user.getDataScope() != null && user.getDataScope() == 2) {
            log.debug("数据权限过滤 - 超级管理员，跳过过滤: userId={}", userId);

            // 设置跳过标记
            DataScopeContext context = new DataScopeContext();
            context.setSkip(true);
            DataScopeContext.set(context);

            try {
                return point.proceed();
            } finally {
                DataScopeContext.clear();
            }
        }

        // 获取用户授权的网吧配置ID列表
        List<Long> configIds = sysUserConfigService.getConfigIdsByUserId(userId);
        log.debug("数据权限过滤 - 用户授权的网吧配置: {}", configIds);

        // 如果用户没有任何网吧配置权限，返回空结果
        if (configIds == null || configIds.isEmpty()) {
            log.warn("数据权限过滤 - 用户无任何网吧配置权限: userId={}", userId);
            // 这里可以返回空结果，或者抛出异常，根据业务需求决定
            // 这里选择继续执行，让SQL查询条件处理
        }

        // 设置数据权限上下文
        DataScopeContext context = new DataScopeContext();
        context.setSkip(false);
        context.setConfigIds(configIds);
        context.setConfigAlias(dataScope.configAlias());
        context.setConfigIdField(dataScope.configIdField());
        DataScopeContext.set(context);

        try {
            // 执行方法
            return point.proceed();
        } finally {
            // 清除上下文
            DataScopeContext.clear();
        }
    }
}
