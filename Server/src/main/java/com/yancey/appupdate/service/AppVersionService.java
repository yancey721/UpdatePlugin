package com.yancey.appupdate.service;

import com.yancey.appupdate.dto.AppInfoWithLatestVersionDto;
import com.yancey.appupdate.dto.AppVersionDto;
import com.yancey.appupdate.dto.ParsedApkData;
import com.yancey.appupdate.entity.AppInfo;
import com.yancey.appupdate.entity.AppVersion;
import com.yancey.appupdate.exception.BusinessException;
import com.yancey.appupdate.repository.AppInfoRepository;
import com.yancey.appupdate.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 应用版本服务
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppInfoRepository appInfoRepository;
    private final AppVersionRepository appVersionRepository;
    private final FileStorageService fileStorageService;
    private final ApkParserService apkParserService;

    /**
     * 创建应用版本
     * 
     * @param apkFile APK文件
     * @param appId 应用ID
     * @param updateDescription 更新说明
     * @param forceUpdate 是否强制更新
     * @return 创建的应用版本
     */
    @Transactional
    public AppVersion createAppVersion(MultipartFile apkFile, String appId, String updateDescription, Boolean forceUpdate) {
        try {
            // 1. 验证参数
            validateCreateParams(apkFile, appId);

            // 2. 存储APK文件（临时存储，用于解析）
            String tempFileName = fileStorageService.storeApkFile(apkFile, appId, "temp");
            File tempApkFile = fileStorageService.resolveApkPath(tempFileName).toFile();

            try {
                // 3. 解析APK文件
                ParsedApkData parsedData = apkParserService.parseApk(tempApkFile);
                log.info("APK解析完成: {}", parsedData);

                // 4. 查找或创建AppInfo
                AppInfo appInfo = findOrCreateAppInfo(appId, parsedData);

                // 5. 检查版本是否已存在
                checkVersionExists(appInfo, parsedData.getVersionCodeAsInt());

                // 6. 重新存储APK文件（使用正确的版本号）
                String finalFileName = fileStorageService.storeApkFile(apkFile, appId, parsedData.getVersionCode());
                String downloadUrl = fileStorageService.generateDownloadUrl(finalFileName);

                // 7. 创建AppVersion实体
                AppVersion appVersion = createAppVersionEntity(appInfo, parsedData, finalFileName, downloadUrl, updateDescription, forceUpdate);

                // 8. 保存到数据库
                AppVersion savedVersion = appVersionRepository.save(appVersion);
                log.info("应用版本创建成功: {} - {}", appInfo.getAppName(), savedVersion.getVersionName());

                return savedVersion;

            } finally {
                // 清理临时文件
                fileStorageService.deleteFile(tempFileName);
            }

        } catch (IOException e) {
            log.error("创建应用版本失败: {}", e.getMessage(), e);
            throw new BusinessException("APK文件处理失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("创建应用版本失败: {}", e.getMessage(), e);
            throw new BusinessException("创建应用版本失败: " + e.getMessage());
        }
    }

    /**
     * 验证创建参数
     */
    private void validateCreateParams(MultipartFile apkFile, String appId) {
        if (apkFile == null || apkFile.isEmpty()) {
            throw new BusinessException("APK文件不能为空");
        }

        if (!StringUtils.hasText(appId)) {
            throw new BusinessException("应用ID不能为空");
        }

        if (appId.length() > 100) {
            throw new BusinessException("应用ID长度不能超过100个字符");
        }

        // 验证文件类型
        String originalFilename = apkFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".apk")) {
            throw new BusinessException("只支持APK文件格式");
        }
    }

    /**
     * 查找或创建AppInfo
     */
    private AppInfo findOrCreateAppInfo(String appId, ParsedApkData parsedData) {
        return appInfoRepository.findByAppId(appId)
                .orElseGet(() -> {
                    AppInfo newAppInfo = new AppInfo();
                    newAppInfo.setAppId(appId);
                    newAppInfo.setAppName(StringUtils.hasText(parsedData.getAppName()) ? parsedData.getAppName() : appId);
                    newAppInfo.setPackageName(parsedData.getPackageName());
                    newAppInfo.setCreateTime(LocalDateTime.now());
                    newAppInfo.setUpdateTime(LocalDateTime.now());
                    
                    AppInfo savedAppInfo = appInfoRepository.save(newAppInfo);
                    log.info("创建新应用: {} - {}", savedAppInfo.getAppId(), savedAppInfo.getAppName());
                    return savedAppInfo;
                });
    }

    /**
     * 检查版本是否已存在
     */
    private void checkVersionExists(AppInfo appInfo, Integer versionCode) {
        if (appVersionRepository.existsByAppInfoAndVersionCode(appInfo, versionCode)) {
            throw new BusinessException(String.format("版本 %d 已存在，请使用不同的版本号", versionCode));
        }
    }

    /**
     * 创建AppVersion实体
     */
    private AppVersion createAppVersionEntity(AppInfo appInfo, ParsedApkData parsedData, 
                                            String fileName, String downloadUrl, 
                                            String updateDescription, Boolean forceUpdate) {
        AppVersion appVersion = new AppVersion();
        appVersion.setAppInfo(appInfo);
        appVersion.setVersionCode(parsedData.getVersionCodeAsInt());
        appVersion.setVersionName(parsedData.getVersionName());
        appVersion.setFileSize(parsedData.getFileSize());
        appVersion.setMd5(parsedData.getMd5());
        appVersion.setApkPath(fileName);
        appVersion.setDownloadUrl(downloadUrl);
        appVersion.setUpdateDescription(updateDescription);
        appVersion.setForceUpdate(forceUpdate != null ? forceUpdate : false);
        appVersion.setStatus(AppVersion.Status.ENABLED.getCode());
        appVersion.setCreateTime(LocalDateTime.now());
        appVersion.setUpdateTime(LocalDateTime.now());
        
        return appVersion;
    }

    /**
     * 转换为DTO
     */
    public AppVersionDto convertToDto(AppVersion appVersion) {
        AppVersionDto dto = new AppVersionDto();
        dto.setId(appVersion.getId());
        dto.setAppId(appVersion.getAppInfo().getAppId());
        dto.setAppName(appVersion.getAppInfo().getAppName());
        dto.setPackageName(appVersion.getAppInfo().getPackageName());
        dto.setVersionCode(appVersion.getVersionCode());
        dto.setVersionName(appVersion.getVersionName());
        dto.setFileSize(appVersion.getFileSize());
        dto.setMd5(appVersion.getMd5());
        dto.setApkPath(appVersion.getApkPath());
        dto.setDownloadUrl(appVersion.getDownloadUrl());
        dto.setUpdateDescription(appVersion.getUpdateDescription());
        dto.setForceUpdate(appVersion.getForceUpdate());
        dto.setStatus(appVersion.getStatus());
        dto.setStatusDescription(AppVersion.Status.fromCode(appVersion.getStatus()).getDescription());
        dto.setCreateTime(appVersion.getCreateTime());
        dto.setUpdateTime(appVersion.getUpdateTime());
        
        return dto;
    }

    /**
     * 查询应用列表（支持分页和搜索）
     * 
     * @param appNameQuery 应用名称查询条件（可选）
     * @param pageable 分页参数
     * @return 应用信息及最新版本的分页列表
     */
    public Page<AppInfoWithLatestVersionDto> getAppsWithLatestVersion(String appNameQuery, Pageable pageable) {
        Page<AppInfo> appInfoPage;
        
        // 根据是否有搜索条件选择不同的查询方法
        if (StringUtils.hasText(appNameQuery)) {
            log.info("查询应用列表，搜索条件: {}, 分页: {}", appNameQuery, pageable);
            appInfoPage = appInfoRepository.findByAppNameContainingWithLatestVersion(appNameQuery, pageable);
        } else {
            log.info("查询所有应用列表，分页: {}", pageable);
            appInfoPage = appInfoRepository.findAllWithLatestVersion(pageable);
        }
        
        // 转换为DTO
        return appInfoPage.map(this::convertToAppInfoWithLatestVersionDto);
    }

    /**
     * 转换AppInfo为AppInfoWithLatestVersionDto
     * 
     * @param appInfo 应用信息实体
     * @return 应用信息及最新版本DTO
     */
    private AppInfoWithLatestVersionDto convertToAppInfoWithLatestVersionDto(AppInfo appInfo) {
        AppInfoWithLatestVersionDto dto = new AppInfoWithLatestVersionDto();
        
        // 设置应用基本信息
        dto.setId(appInfo.getId());
        dto.setAppId(appInfo.getAppId());
        dto.setAppName(appInfo.getAppName());
        dto.setPackageName(appInfo.getPackageName());
        dto.setCreateTime(appInfo.getCreateTime());
        dto.setUpdateTime(appInfo.getUpdateTime());
        
        // 获取版本总数
        long totalVersionsLong = appVersionRepository.countByAppInfo(appInfo);
        dto.setTotalVersions((int) totalVersionsLong);
        
        // 获取最新启用的版本信息
        appVersionRepository.findTopByAppInfoAndStatusOrderByVersionCodeDesc(appInfo, AppVersion.Status.ENABLED.getCode())
            .ifPresent(latestVersion -> {
                dto.setLatestVersionId(latestVersion.getId());
                dto.setLatestVersionCode(latestVersion.getVersionCode());
                dto.setLatestVersionName(latestVersion.getVersionName());
                dto.setLatestFileSize(latestVersion.getFileSize());
                dto.setLatestUpdateDescription(latestVersion.getUpdateDescription());
                dto.setLatestForceUpdate(latestVersion.getForceUpdate());
                dto.setLatestStatus(latestVersion.getStatus());
                dto.setLatestStatusDescription(AppVersion.Status.fromCode(latestVersion.getStatus()).getDescription());
                dto.setLatestVersionCreateTime(latestVersion.getCreateTime());
            });
        
        return dto;
    }

    /**
     * 查询指定应用的版本列表（支持分页）
     * 
     * @param appId 应用ID
     * @param pageable 分页参数
     * @return 应用版本的分页列表
     */
    public Page<AppVersionDto> getAppVersions(String appId, Pageable pageable) {
        log.info("查询应用版本列表: appId={}, 分页: {}", appId, pageable);
        
        // 根据appId查找AppInfo
        AppInfo appInfo = appInfoRepository.findByAppId(appId)
                .orElseThrow(() -> new BusinessException("应用ID不存在: " + appId));
        
        // 查询该应用的所有版本（按版本号倒序）
        Page<AppVersion> versionsPage = appVersionRepository.findByAppInfoOrderByVersionCodeDesc(appInfo, pageable);
        
        log.info("查询应用版本列表成功: appId={}, 总数={}, 当前页={}, 每页大小={}", 
                appId, versionsPage.getTotalElements(), versionsPage.getNumber(), versionsPage.getSize());
        
        // 转换为DTO
        return versionsPage.map(this::convertToDto);
    }

    /**
     * 修改应用版本信息
     * 
     * @param versionId 版本ID
     * @param updateRequest 更新请求
     * @return 更新后的版本信息
     */
    @Transactional
    public AppVersionDto updateAppVersion(Long versionId, com.yancey.appupdate.dto.UpdateVersionRequestDto updateRequest) {
        log.info("修改应用版本信息: versionId={}, request={}", versionId, updateRequest);
        
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException("版本不存在: " + versionId));
        
        // 更新字段
        if (updateRequest.getUpdateDescription() != null) {
            version.setUpdateDescription(updateRequest.getUpdateDescription());
        }
        if (updateRequest.getForceUpdate() != null) {
            version.setForceUpdate(updateRequest.getForceUpdate());
        }
        if (updateRequest.getStatus() != null) {
            version.setStatus(updateRequest.getStatus());
        }
        
        version.setUpdateTime(LocalDateTime.now());
        
        AppVersion savedVersion = appVersionRepository.save(version);
        
        log.info("修改应用版本信息成功: versionId={}, versionCode={}", versionId, savedVersion.getVersionCode());
        
        return convertToDto(savedVersion);
    }

    /**
     * 删除应用版本
     * 
     * @param versionId 版本ID
     * @param forceDelete 是否强制删除文件
     */
    @Transactional
    public void deleteAppVersion(Long versionId, Boolean forceDelete) {
        log.info("删除应用版本: versionId={}, forceDelete={}", versionId, forceDelete);
        
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException("版本不存在: " + versionId));
        
        // 删除文件（如果需要）
        if (forceDelete == null || forceDelete) {
            try {
                fileStorageService.deleteFile(version.getApkPath());
                log.info("删除APK文件成功: {}", version.getApkPath());
            } catch (Exception e) {
                log.warn("删除APK文件失败: {}, error={}", version.getApkPath(), e.getMessage());
            }
        }
        
        // 删除数据库记录
        appVersionRepository.delete(version);
        
        log.info("删除应用版本成功: versionId={}, versionCode={}", versionId, version.getVersionCode());
    }

    /**
     * 批量删除应用版本
     * 
     * @param batchDeleteRequest 批量删除请求
     * @return 删除成功的版本ID列表
     */
    @Transactional
    public java.util.List<Long> batchDeleteVersions(com.yancey.appupdate.dto.BatchDeleteRequestDto batchDeleteRequest) {
        log.info("批量删除应用版本: request={}", batchDeleteRequest);
        
        java.util.List<Long> successIds = new java.util.ArrayList<>();
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        for (Long versionId : batchDeleteRequest.getVersionIds()) {
            try {
                deleteAppVersion(versionId, batchDeleteRequest.getForceDelete());
                successIds.add(versionId);
            } catch (Exception e) {
                errors.add("版本ID " + versionId + ": " + e.getMessage());
                log.error("删除版本失败: versionId={}, error={}", versionId, e.getMessage());
            }
        }
        
        log.info("批量删除应用版本完成: 成功={}, 失败={}", successIds.size(), errors.size());
        
        if (!errors.isEmpty() && successIds.isEmpty()) {
            throw new BusinessException("批量删除失败: " + String.join(", ", errors));
        } else if (!errors.isEmpty()) {
            log.warn("部分删除失败: {}", String.join(", ", errors));
        }
        
        return successIds;
    }

    /**
     * 更新版本状态
     * 
     * @param versionId 版本ID
     * @param statusRequest 状态更新请求
     * @return 更新后的版本信息
     */
    @Transactional
    public AppVersionDto updateVersionStatus(Long versionId, com.yancey.appupdate.dto.UpdateStatusRequestDto statusRequest) {
        log.info("更新版本状态: versionId={}, newStatus={}", versionId, statusRequest.getStatus());
        
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException("版本不存在: " + versionId));
        
        version.setStatus(statusRequest.getStatus());
        version.setUpdateTime(LocalDateTime.now());
        
        AppVersion savedVersion = appVersionRepository.save(version);
        
        log.info("更新版本状态成功: versionId={}, status={}", versionId, savedVersion.getStatus());
        
        return convertToDto(savedVersion);
    }

    /**
     * 获取版本统计信息
     * 
     * @return 统计信息
     */
    public com.yancey.appupdate.dto.VersionStatsDto getVersionStats() {
        log.info("获取版本统计信息");
        
        com.yancey.appupdate.dto.VersionStatsDto stats = new com.yancey.appupdate.dto.VersionStatsDto();
        
        // 应用总数
        stats.setTotalApps(appInfoRepository.count());
        
        // 版本总数
        stats.setTotalVersions(appVersionRepository.count());
        
        // 按状态统计
        stats.setEnabledVersions(appVersionRepository.countByStatus(1));
        stats.setDisabledVersions(appVersionRepository.countByStatus(0));
        stats.setTestVersions(appVersionRepository.countByStatus(2));
        
        // 强制更新版本数
        stats.setForceUpdateVersions(appVersionRepository.countByForceUpdateTrue());
        
        // 文件总大小
        Long totalSize = appVersionRepository.sumFileSize();
        stats.setTotalFileSize(totalSize != null ? totalSize : 0L);
        
        // 最近版本（最近10个）
        java.util.List<AppVersion> recentVersions = appVersionRepository.findTop10ByOrderByCreateTimeDesc();
        stats.setRecentVersions(recentVersions.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList()));
        
        // 状态统计Map
        java.util.Map<String, Long> statusStats = new java.util.HashMap<>();
        statusStats.put("启用", stats.getEnabledVersions());
        statusStats.put("禁用", stats.getDisabledVersions());
        statusStats.put("测试", stats.getTestVersions());
        stats.setStatusStats(statusStats);
        
        stats.setStatisticsTime(LocalDateTime.now());
        
        log.info("获取版本统计信息成功: totalApps={}, totalVersions={}", stats.getTotalApps(), stats.getTotalVersions());
        
        return stats;
    }

    /**
     * 移动端检查更新
     * 
     * @param appId 应用ID
     * @param currentVersionCode 当前版本号
     * @return 更新检查结果
     */
    public com.yancey.appupdate.dto.CheckUpdateResponseDto checkUpdate(String appId, Integer currentVersionCode) {
        log.info("检查更新: appId={}, currentVersionCode={}", appId, currentVersionCode);
        
        try {
            // 1. 查找应用信息
            AppInfo appInfo = appInfoRepository.findByAppId(appId)
                    .orElseThrow(() -> new BusinessException("应用ID不存在: " + appId));
            
            // 2. 查找最新的启用版本，且版本号大于当前版本
            java.util.Optional<AppVersion> latestVersionOpt = appVersionRepository
                    .findTopByAppInfoAndVersionCodeGreaterThanAndStatusOrderByVersionCodeDesc(
                            appInfo, currentVersionCode, AppVersion.Status.ENABLED.getCode());
            
            if (latestVersionOpt.isEmpty()) {
                log.info("无更新可用: appId={}, currentVersionCode={}", appId, currentVersionCode);
                return com.yancey.appupdate.dto.CheckUpdateResponseDto.noUpdate();
            }
            
            // 3. 构建更新响应
            AppVersion latestVersion = latestVersionOpt.get();
            com.yancey.appupdate.dto.CheckUpdateResponseDto response = 
                    com.yancey.appupdate.dto.CheckUpdateResponseDto.hasUpdate(
                            latestVersion.getVersionName(),
                            latestVersion.getVersionCode(),
                            latestVersion.getUpdateDescription(),
                            latestVersion.getForceUpdate(),
                            latestVersion.getDownloadUrl(),
                            latestVersion.getMd5(),
                            latestVersion.getFileSize()
                    );
            
            log.info("发现更新: appId={}, currentVersionCode={} -> newVersionCode={}, forceUpdate={}", 
                    appId, currentVersionCode, latestVersion.getVersionCode(), latestVersion.getForceUpdate());
            
            return response;
            
        } catch (BusinessException e) {
            log.error("检查更新失败: appId={}, currentVersionCode={}, error={}", 
                    appId, currentVersionCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("检查更新失败: appId={}, currentVersionCode={}, error={}", 
                    appId, currentVersionCode, e.getMessage(), e);
            throw new BusinessException("检查更新失败: " + e.getMessage());
        }
    }
} 