package com.bluecat.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.DataScopeContext;

import java.util.List;

/**
 * 数据权限工具类
 *
 * @author BlueCat
 * @since 2026-04-17
 */
public class DataScopeUtil {

    /**
     * 为查询条件添加数据权限过滤（直接按config_id过滤）
     *
     * @param wrapper 查询条件
     * @param configIdGetter 网吧配置ID的getter方法引用 (如：ShopInfo::getConfigId)
     * @param <T> 实体类型
     * @param <R> 网吧配置ID类型
     */
    public static <T, R> void addDataScopeFilter(LambdaQueryWrapper<T> wrapper, com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, R> configIdGetter) {
        DataScopeContext context = DataScopeContext.get();

        // 如果没有上下文或者跳过过滤，直接返回
        if (context == null || context.getSkip()) {
            return;
        }

        // 获取授权的网吧配置ID列表
        List<Long> configIds = context.getConfigIds();

        // 如果没有授权网吧配置，添加一个不可能满足的条件（返回空结果）
        if (configIds == null || configIds.isEmpty()) {
            wrapper.apply("1 = 0"); // 永远为false的条件
            return;
        }

        // 添加IN条件
        wrapper.in(configIdGetter, configIds);
    }

    /**
     * 为查询条件添加数据权限过滤（通过shop_id关联过滤）
     * 用于没有config_id字段，但有shop_id字段的表
     *
     * @param wrapper 查询条件
     * @param shopIdGetter 门店ID的getter方法引用 (如：MachineInfo::getShopId)
     * @param <T> 实体类型
     * @param <R> 门店ID类型
     */
    public static <T, R> void addDataScopeFilterByShopId(LambdaQueryWrapper<T> wrapper, com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, R> shopIdGetter) {
        DataScopeContext context = DataScopeContext.get();

        // 如果没有上下文或者跳过过滤，直接返回
        if (context == null || context.getSkip()) {
            return;
        }

        // 获取授权的网吧配置ID列表
        List<Long> configIds = context.getConfigIds();

        // 如果没有授权网吧配置，添加一个不可能满足的条件（返回空结果）
        if (configIds == null || configIds.isEmpty()) {
            wrapper.apply("1 = 0"); // 永远为false的条件
            return;
        }

        // 通过子查询关联：shop_id IN (SELECT id FROM shop_info WHERE config_id IN (...))
        // 获取字段名
        String column = getColumnName(shopIdGetter);
        wrapper.apply(column + " IN (SELECT id FROM shop_info WHERE config_id IN ({0}))", 
                configIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        
        // 使用 inSql 方式
        String configIdsStr = configIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        wrapper.inSql(shopIdGetter, 
                "SELECT id FROM shop_info WHERE config_id IN (" + configIdsStr + ")");
    }

    /**
     * 获取Lambda表达式对应的数据库列名
     */
    @SuppressWarnings("unchecked")
    private static <T, R> String getColumnName(com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, R> getter) {
        // 简化处理，直接返回默认列名
        return "shop_id";
    }

    /**
     * 判断是否有数据权限
     *
     * @return true=有权限，false=无权限
     */
    public static boolean hasDataScope() {
        DataScopeContext context = DataScopeContext.get();
        if (context == null) {
            return true; // 没有上下文，默认有权限
        }
        if (context.getSkip()) {
            return true; // 跳过过滤，有权限
        }
        List<Long> configIds = context.getConfigIds();
        return configIds != null && !configIds.isEmpty();
    }

    /**
     * 获取授权的网吧配置ID列表
     *
     * @return 网吧配置ID列表
     */
    public static List<Long> getConfigIds() {
        DataScopeContext context = DataScopeContext.get();
        if (context == null || context.getSkip()) {
            return null; // 没有上下文或跳过过滤，返回null表示全部
        }
        return context.getConfigIds();
    }

    /**
     * 获取授权的配置ID列表的SQL IN字符串
     * 用于构建原生SQL查询
     *
     * @return 如 "1,2,3" 或 null表示全部
     */
    public static String getConfigIdsSql() {
        List<Long> configIds = getConfigIds();
        if (configIds == null) {
            return null;
        }
        if (configIds.isEmpty()) {
            return null; // 空列表返回null，调用方应处理为无权限
        }
        return configIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }
}
