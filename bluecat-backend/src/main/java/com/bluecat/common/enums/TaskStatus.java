package com.bluecat.common.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Getter
public enum TaskStatus {

    /**
     * 执行中
     */
    EXECUTING(0, "执行中"),

    /**
     * 成功
     */
    SUCCESS(1, "成功"),

    /**
     * 失败
     */
    FAILED(2, "失败");

    private final Integer code;
    private final String desc;

    TaskStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskStatus getByCode(Integer code) {
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
