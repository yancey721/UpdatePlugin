package com.yancey.appupdate.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 版本统计信息DTO
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class VersionStatsDto {

    /**
     * 应用总数
     */
    private Long totalApps;

    /**
     * 版本总数
     */
    private Long totalVersions;

    /**
     * 启用版本数
     */
    private Long enabledVersions;

    /**
     * 禁用版本数
     */
    private Long disabledVersions;

    /**
     * 测试版本数
     */
    private Long testVersions;

    /**
     * 强制更新版本数
     */
    private Long forceUpdateVersions;

    /**
     * 最近上传的版本（最近10个）
     */
    private java.util.List<AppVersionDto> recentVersions;

    /**
     * 按状态分组统计
     */
    private Map<String, Long> statusStats;

    /**
     * 文件总大小（字节）
     */
    private Long totalFileSize;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;
} 