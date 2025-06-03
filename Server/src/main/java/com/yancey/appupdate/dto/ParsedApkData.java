package com.yancey.appupdate.dto;

import lombok.Data;

/**
 * 解析后的APK数据
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class ParsedApkData {

    /**
     * 包名
     */
    private String packageName;

    /**
     * 版本号（数字）
     */
    private String versionCode;

    /**
     * 版本名称（字符串）
     */
    private String versionName;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MD5值
     */
    private String md5;

    /**
     * 应用图标（可选）
     */
    private byte[] icon;

    /**
     * 获取整数类型的版本号
     * 
     * @return 版本号
     */
    public Integer getVersionCodeAsInt() {
        try {
            return Integer.parseInt(versionCode);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
} 