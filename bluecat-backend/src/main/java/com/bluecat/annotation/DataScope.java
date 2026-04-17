package com.bluecat.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 标注在Controller方法上，表示该方法需要进行数据权限过滤
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 网吧配置表的别名
     */
    String configAlias() default "t";

    /**
     * 网吧配置ID字段名
     */
    String configIdField() default "config_id";

    /**
     * 是否启用数据权限过滤
     */
    boolean enabled() default true;
}
