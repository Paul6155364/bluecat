package com.bluecat.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限上下文
 * 用于在ThreadLocal中传递数据权限信息
 *
 * @author BlueCat
 * @since 2026-04-17
 */
@Data
public class DataScopeContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否跳过数据权限过滤
     */
    private Boolean skip = false;

    /**
     * 授权的网吧配置ID列表
     */
    private List<Long> configIds;

    /**
     * 网吧配置表别名
     */
    private String configAlias;

    /**
     * 网吧配置ID字段名
     */
    private String configIdField;

    /**
     * ThreadLocal
     */
    private static final ThreadLocal<DataScopeContext> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置数据权限上下文
     */
    public static void set(DataScopeContext context) {
        THREAD_LOCAL.set(context);
    }

    /**
     * 获取数据权限上下文
     */
    public static DataScopeContext get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 清除数据权限上下文
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
