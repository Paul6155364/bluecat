package com.bluecat.config;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    /**
     * 抛出业务异常
     */
    public static void throwException(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常
     */
    public static void throwException(Integer code, String message) {
        throw new BusinessException(code, message);
    }
}
