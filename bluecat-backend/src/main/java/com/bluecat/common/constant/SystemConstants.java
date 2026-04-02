package com.bluecat.common.constant;

/**
 * 系统常量
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public class SystemConstants {

    /**
     * 删除标记 - 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 删除标记 - 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 状态 - 正常
     */
    public static final Integer STATUS_NORMAL = 1;

    /**
     * 状态 - 禁用
     */
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 1;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 0;

    /**
     * 默认页码
     */
    public static final Long DEFAULT_PAGE = 1L;

    /**
     * 默认每页大小
     */
    public static final Long DEFAULT_SIZE = 10L;

    /**
     * 最大每页大小
     */
    public static final Long MAX_SIZE = 100L;
}
