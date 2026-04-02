package com.bluecat.common.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Getter
public enum TaskType {

    /**
     * 门店列表采集
     */
    SHOP_LIST("SHOP_LIST", "门店列表采集"),

    /**
     * 门店详情采集
     */
    SHOP_INFO("SHOP_INFO", "门店详情采集"),

    /**
     * 机器状态采集
     */
    MACHINE_STATUS("MACHINE_STATUS", "机器状态采集"),

    /**
     * 全量采集
     */
    FULL_COLLECTION("FULL_COLLECTION", "全量采集");

    private final String code;
    private final String desc;

    TaskType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskType getByCode(String code) {
        for (TaskType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
