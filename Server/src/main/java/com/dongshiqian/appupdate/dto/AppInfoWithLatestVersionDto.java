package com.dongshiqian.appupdate.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息及最新版本DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class AppInfoWithLatestVersionDto {

    /**
     * 应用信息ID
     */
    private Long id;

    /**
     * 应用ID（唯一标识）
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
     * 应用创建时间
     */
    private LocalDateTime createTime;

    /**
     * 应用更新时间
     */
    private LocalDateTime updateTime;

    // 最新版本信息
    /**
     * 最新版本ID
     */
    private Long latestVersionId;

    /**
     * 最新版本号（数字）
     */
    private Integer latestVersionCode;

    /**
     * 最新版本名称（字符串）
     */
    private String latestVersionName;

    /**
     * 最新版本文件大小（字节）
     */
    private Long latestFileSize;

    /**
     * 最新版本更新说明
     */
    private String latestUpdateDescription;

    /**
     * 最新版本是否强制更新
     */
    private Boolean latestForceUpdate;

    /**
     * 最新版本状态：0-禁用，1-启用
     */
    private Integer latestStatus;

    /**
     * 最新版本状态描述
     */
    private String latestStatusDescription;

    /**
     * 最新版本创建时间
     */
    private LocalDateTime latestVersionCreateTime;

    /**
     * 版本总数
     */
    private Integer totalVersions;
} 