package com.dongshiqian.appupdate.dto;

import lombok.Data;

/**
 * 移动端检查更新响应DTO
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
public class CheckUpdateResponseDto {

    /**
     * 是否有更新
     */
    private Boolean hasUpdate;

    /**
     * 新版本名称
     */
    private String newVersionName;

    /**
     * 新版本号
     */
    private Integer newVersionCode;

    /**
     * 更新说明
     */
    private String updateDescription;

    /**
     * 是否强制更新
     */
    private Boolean forceUpdate;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 创建无更新的响应
     */
    public static CheckUpdateResponseDto noUpdate() {
        CheckUpdateResponseDto response = new CheckUpdateResponseDto();
        response.setHasUpdate(false);
        return response;
    }

    /**
     * 创建有更新的响应
     */
    public static CheckUpdateResponseDto hasUpdate(String versionName, Integer versionCode, 
                                                  String updateDescription, Boolean forceUpdate,
                                                  String downloadUrl, String md5, Long fileSize) {
        CheckUpdateResponseDto response = new CheckUpdateResponseDto();
        response.setHasUpdate(true);
        response.setNewVersionName(versionName);
        response.setNewVersionCode(versionCode);
        response.setUpdateDescription(updateDescription);
        response.setForceUpdate(forceUpdate);
        response.setDownloadUrl(downloadUrl);
        response.setMd5(md5);
        response.setFileSize(fileSize);
        return response;
    }
} 