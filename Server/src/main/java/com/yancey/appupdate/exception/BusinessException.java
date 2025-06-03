package com.yancey.appupdate.exception;

/**
 * 业务异常
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 