package com.dongshiqian.appupdate.exception;

/**
 * 文件存储异常
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
} 