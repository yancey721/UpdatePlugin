package com.yancey.appupdate.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息DTO
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-06-04
 */
@Data
public class AppInfoDto {

    /**
     * 主键ID
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
     * 是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 