package com.bluecat.common.enums;

import lombok.Getter;

/**
 * 机器状态枚举
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Getter
public enum MachineStatus {

    /**
     * 占用
     */
    BUSY(0, "占用"),

    /**
     * 空闲
     */
    FREE(1, "空闲");

    private final Integer code;
    private final String desc;

    MachineStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MachineStatus getByCode(Integer code) {
        for (MachineStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
