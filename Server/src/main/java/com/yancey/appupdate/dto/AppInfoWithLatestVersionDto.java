package com.yancey.appupdate.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息及最新版本DTO
 * 
 * @author yancey
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
     * 应用级别是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 应用创建时间
     */
    private LocalDateTime createTime;

    /**
     * 应用更新时间
     */
    private LocalDateTime updateTime;

    // 最新发布版本信息
    /**
     * 最新发布版本ID
     */
    private Long latestVersionId;

    /**
     * 最新发布版本号（数字）
     */
    private Integer latestVersionCode;

    /**
     * 最新发布版本名称（字符串）
     */
    private String latestVersionName;

    /**
     * 最新发布版本文件大小（字节）
     */
    private Long latestFileSize;

    /**
     * 最新发布版本更新说明
     */
    private String latestUpdateDescription;

    /**
     * 最新发布版本是否强制更新（版本级别，保留兼容性）
     */
    private Boolean latestForceUpdate;

    /**
     * 是否为发布版本
     */
    private Boolean latestIsReleased;

    /**
     * 最新发布版本创建时间
     */
    private LocalDateTime latestVersionCreateTime;

    /**
     * 版本总数
     */
    private Integer totalVersions;
} 