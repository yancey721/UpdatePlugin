package com.dongshiqian.appupdate.exception;

/**
 * APK解析异常
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
public class ApkParseException extends RuntimeException {

    public ApkParseException(String message) {
        super(message);
    }

    public ApkParseException(String message, Throwable cause) {
        super(message, cause);
    }
} 