package com.dongshiqian.appupdate.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用版本DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class AppVersionDto {

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 版本号（数字）
     */
    private Integer versionCode;

    /**
     * 版本名称（字符串）
     */
    private String versionName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * APK文件在服务器上的存储路径
     */
    private String apkPath;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 更新说明
     */
    private String updateDescription;

    /**
     * 是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 