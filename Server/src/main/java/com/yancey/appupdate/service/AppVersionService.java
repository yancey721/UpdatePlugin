package com.yancey.appupdate.service;

import com.yancey.appupdate.dto.AppInfoDto;
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
import java.util.List;
import java.util.Optional;

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
        return appInfoRepository.findById(appId)
                .orElseGet(() -> {
                    AppInfo newAppInfo = new AppInfo();
                    newAppInfo.setAppId(appId);
                    newAppInfo.setAppName(StringUtils.hasText(parsedData.getAppName()) ? parsedData.getAppName() : appId);
                    newAppInfo.setAppDescription("通过APK上传自动创建");
                    newAppInfo.setForceUpdate(false);
                    
                    AppInfo savedAppInfo = appInfoRepository.save(newAppInfo);
                    log.info("创建新应用: {} - {}", savedAppInfo.getAppId(), savedAppInfo.getAppName());
                    return savedAppInfo;
                });
    }

    /**
     * 检查版本是否已存在
     */
    private void checkVersionExists(AppInfo appInfo, Integer versionCode) {
        if (appVersionRepository.existsByAppIdAndVersionCode(appInfo.getAppId(), versionCode)) {
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
        appVersion.setAppId(appInfo.getAppId());  // 直接设置appId字符串
        appVersion.setVersionCode(parsedData.getVersionCodeAsInt());
        appVersion.setVersionName(parsedData.getVersionName());
        appVersion.setFileSize(parsedData.getFileSize());
        appVersion.setMd5(parsedData.getMd5());
        appVersion.setApkPath(fileName);
        appVersion.setDownloadUrl(downloadUrl);
        appVersion.setUpdateDescription(updateDescription);
        appVersion.setForceUpdate(forceUpdate != null ? forceUpdate : false);
        appVersion.setIsReleased(false);
        
        return appVersion;
    }

    /**
     * 转换为DTO
     */
    public AppVersionDto convertToDto(AppVersion appVersion) {
        // 获取应用信息
        AppInfo appInfo = appInfoRepository.findById(appVersion.getAppId())
                .orElseThrow(() -> new BusinessException("找不到应用信息: " + appVersion.getAppId()));
        
        AppVersionDto dto = new AppVersionDto();
        dto.setId(appVersion.getId());
        dto.setAppId(appVersion.getAppId());
        dto.setAppName(appInfo.getAppName());
        dto.setPackageName(appVersion.getAppId()); // packageName就是appId
        dto.setVersionCode(appVersion.getVersionCode());
        dto.setVersionName(appVersion.getVersionName());
        dto.setFileSize(appVersion.getFileSize());
        dto.setMd5(appVersion.getMd5());
        dto.setApkPath(appVersion.getApkPath());
        dto.setDownloadUrl(appVersion.getDownloadUrl());
        dto.setUpdateDescription(appVersion.getUpdateDescription());
        dto.setForceUpdate(appVersion.getForceUpdate());
        dto.setIsReleased(appVersion.getIsReleased());
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
     * 转换为AppInfoWithLatestVersionDto
     */
    private AppInfoWithLatestVersionDto convertToAppInfoWithLatestVersionDto(AppInfo appInfo) {
        AppInfoWithLatestVersionDto dto = new AppInfoWithLatestVersionDto();
        dto.setId(null); // 不再使用数字ID
        dto.setAppId(appInfo.getAppId());
        dto.setAppName(appInfo.getAppName());
        dto.setPackageName(appInfo.getAppId()); // packageName就是appId
        dto.setForceUpdate(appInfo.getForceUpdate());
        dto.setCreateTime(appInfo.getCreateTime());
        dto.setUpdateTime(appInfo.getUpdateTime());
        dto.setTotalVersions((int) appVersionRepository.countByAppId(appInfo.getAppId()));

        // 查找最新的发布版本
        Optional<AppVersion> latestVersion = appVersionRepository.findByAppIdAndIsReleasedTrue(appInfo.getAppId());
        if (latestVersion.isPresent()) {
            AppVersion version = latestVersion.get();
            dto.setLatestVersionId(version.getId());
            dto.setLatestVersionCode(version.getVersionCode());
            dto.setLatestVersionName(version.getVersionName());
            dto.setLatestFileSize(version.getFileSize());
            dto.setLatestUpdateDescription(version.getUpdateDescription());
            dto.setLatestForceUpdate(version.getForceUpdate());
            dto.setLatestIsReleased(version.getIsReleased());
            dto.setLatestVersionCreateTime(version.getCreateTime());
        }
        
        return dto;
    }

    /**
     * 查询指定应用的版本列表
     */
    public Page<AppVersionDto> getAppVersions(String appId, Pageable pageable) {
        try {
            log.info("查询应用版本列表: appId={}", appId);
        
            // 验证应用是否存在
            AppInfo appInfo = appInfoRepository.findById(appId)
                    .orElseThrow(() -> new BusinessException("应用不存在: " + appId));
        
            Page<AppVersion> versionsPage = appVersionRepository.findByAppIdOrderByVersionCodeDesc(appId, pageable);
            
            Page<AppVersionDto> dtoPage = versionsPage.map(this::convertToDto);
        
            log.info("查询应用版本列表成功: appId={}, 总数={}", appId, dtoPage.getTotalElements());
            return dtoPage;

        } catch (Exception e) {
            log.error("查询应用版本列表失败: appId={}, error={}", appId, e.getMessage(), e);
            throw new BusinessException("查询应用版本列表失败: " + e.getMessage());
        }
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
     * 更新版本状态（废弃方法，使用发布版本管理替代）
     * 
     * @param versionId 版本ID
     * @param statusRequest 状态更新请求
     * @return 更新后的版本信息
     * @deprecated 使用 setReleaseVersion 方法替代
     */
    @Deprecated
    @Transactional
    public AppVersionDto updateVersionStatus(Long versionId, com.yancey.appupdate.dto.UpdateStatusRequestDto statusRequest) {
        log.warn("使用了废弃的updateVersionStatus方法，建议使用setReleaseVersion替代: versionId={}", versionId);
        
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException("版本不存在: " + versionId));
        
        // 不再支持status字段，直接返回当前版本信息
        // version.setStatus(statusRequest.getStatus());
        version.setUpdateTime(LocalDateTime.now());
        
        AppVersion savedVersion = appVersionRepository.save(version);
        
        log.warn("updateVersionStatus方法已废弃，请使用发布版本管理: versionId={}", versionId);
        
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
        
        // 发布版本统计（替代原有的状态统计）
        stats.setEnabledVersions(appVersionRepository.countByIsReleasedTrue());
        stats.setDisabledVersions(appVersionRepository.count() - appVersionRepository.countByIsReleasedTrue());
        stats.setTestVersions(0L); // 不再区分测试版本
        
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
        
        stats.setStatisticsTime(LocalDateTime.now());
        
        log.info("获取版本统计信息成功: totalApps={}, totalVersions={}", stats.getTotalApps(), stats.getTotalVersions());
        
        return stats;
    }

    /**
     * 检查更新
     */
    public com.yancey.appupdate.dto.CheckUpdateResponseDto checkUpdate(String appId, Integer currentVersionCode) {
        try {
        log.info("检查更新: appId={}, currentVersionCode={}", appId, currentVersionCode);
        
            // 验证应用是否存在
            AppInfo appInfo = appInfoRepository.findById(appId)
                    .orElseThrow(() -> new BusinessException("应用不存在: " + appId));
            
            // 查找当前发布版本
            Optional<AppVersion> releaseVersionOpt = appVersionRepository.findByAppIdAndIsReleasedTrue(appId);
            
            if (!releaseVersionOpt.isPresent()) {
                log.info("应用暂无发布版本: appId={}", appId);
                return createNoUpdateResponse();
            }
            
            AppVersion releaseVersion = releaseVersionOpt.get();
            
            // 判断是否需要更新
            if (releaseVersion.getVersionCode() <= currentVersionCode) {
                log.info("当前版本已是最新: appId={}, currentVersionCode={}, releaseVersionCode={}", 
                        appId, currentVersionCode, releaseVersion.getVersionCode());
                return createNoUpdateResponse();
            }

            // 需要更新
            log.info("发现新版本: appId={}, currentVersionCode={}, newVersionCode={}", 
                    appId, currentVersionCode, releaseVersion.getVersionCode());
            
            return createUpdateResponse(releaseVersion, appInfo.getForceUpdate());

        } catch (Exception e) {
            log.error("检查更新失败: appId={}, currentVersionCode={}, error={}", appId, currentVersionCode, e.getMessage(), e);
            throw new BusinessException("检查更新失败: " + e.getMessage());
        }
    }

    // ===========================================
    // 发布版本管理相关方法（新增）
    // ===========================================

    /**
     * 设置应用的发布版本
     * 
     * @param appId 应用ID
     * @param versionId 要设为发布版本的版本ID
     * @return 设置后的版本信息
     */
    @Transactional
    public AppVersionDto setReleaseVersion(String appId, Long versionId) {
        log.info("设置发布版本: appId={}, versionId={}", appId, versionId);
        
        // 1. 验证应用和版本存在性
        AppInfo appInfo = appInfoRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("应用不存在: " + appId));
        
        AppVersion targetVersion = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionId));
        
        // 2. 验证版本属于该应用
        if (!targetVersion.getAppId().equals(appInfo.getAppId())) {
            throw new IllegalArgumentException("版本不属于指定应用");
        }
        
        // 3. 原子操作：先清除所有发布状态，再设置新的发布版本
        int updatedCount = appVersionRepository.updateAllVersionsToNonReleased(appId);
        log.info("清除应用所有发布状态: appId={}, updatedCount={}", appId, updatedCount);
        
        targetVersion.setIsReleased(true);
        targetVersion.setUpdateTime(LocalDateTime.now());
        AppVersion savedVersion = appVersionRepository.save(targetVersion);
        
        log.info("设置发布版本成功: appId={}, versionId={}, versionCode={}", 
                appId, versionId, savedVersion.getVersionCode());
        
        return convertToDto(savedVersion);
    }

    /**
     * 更新应用强制更新设置
     * 
     * @param appId 应用ID
     * @param forceUpdate 是否强制更新
     * @return 更新后的应用信息
     */
    @Transactional  
    public AppInfoDto updateAppForceUpdate(String appId, Boolean forceUpdate) {
        log.info("更新应用强制更新设置: appId={}, forceUpdate={}", appId, forceUpdate);
        
        AppInfo appInfo = appInfoRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("应用不存在: " + appId));
        
        appInfo.setForceUpdate(forceUpdate);
        appInfo.setUpdateTime(LocalDateTime.now());
        AppInfo savedAppInfo = appInfoRepository.save(appInfo);
        
        log.info("更新应用强制更新设置成功: appId={}, forceUpdate={}", appId, forceUpdate);
        
        return convertToAppInfoDto(savedAppInfo);
    }

    /**
     * 获取当前发布版本
     * 
     * @param appId 应用ID
     * @return 当前发布版本，如果没有则返回null
     */
    public AppVersionDto getCurrentReleaseVersion(String appId) {
        log.info("获取当前发布版本: appId={}", appId);
        
        Optional<AppVersion> releaseVersion = appVersionRepository.findByAppIdAndIsReleasedTrue(appId);
        
        if (releaseVersion.isPresent()) {
            log.info("找到发布版本: appId={}, versionCode={}", appId, releaseVersion.get().getVersionCode());
            return convertToDto(releaseVersion.get());
        } else {
            log.info("未找到发布版本: appId={}", appId);
            return null;
        }
    }

    /**
     * 验证发布版本数据一致性
     * 
     * @param appId 应用ID
     * @return 发布版本列表（应该只有一个）
     */
    public List<AppVersion> validateReleaseVersionConsistency(String appId) {
        log.info("验证发布版本数据一致性: appId={}", appId);
        
        List<AppVersion> releasedVersions = appVersionRepository.findByAppIdAndIsReleasedTrueOrderByVersionCodeDesc(appId);
        
        if (releasedVersions.size() > 1) {
            log.warn("发现多个发布版本，数据不一致: appId={}, count={}", appId, releasedVersions.size());
        }
        
        return releasedVersions;
    }

    /**
     * 转换AppInfo为AppInfoDto
     */
    public AppInfoDto convertToAppInfoDto(AppInfo appInfo) {
        AppInfoDto dto = new AppInfoDto();
        dto.setId(null); // 不再使用数字ID
        dto.setAppId(appInfo.getAppId());
        dto.setAppName(appInfo.getAppName());
        dto.setPackageName(appInfo.getAppId()); // packageName就是appId
        dto.setForceUpdate(appInfo.getForceUpdate());
        dto.setCreateTime(appInfo.getCreateTime());
        dto.setUpdateTime(appInfo.getUpdateTime());
        return dto;
    }

    /**
     * 创建新应用
     * 
     * @param packageName 包名（作为应用唯一标识）
     * @param appName 应用名称
     * @param appDescription 应用描述
     * @param forceUpdate 是否强制更新
     * @return 创建的应用信息
     */
    @Transactional
    public AppInfo createApp(String packageName, String appName, String appDescription, Boolean forceUpdate) {
        try {
            // 1. 验证包名是否已存在
            if (appInfoRepository.existsById(packageName)) {
                throw new BusinessException("包名 " + packageName + " 已存在，请使用不同的包名");
            }

            // 2. 创建AppInfo实体
            AppInfo appInfo = new AppInfo();
            appInfo.setAppId(packageName);  // 使用packageName作为appId
            appInfo.setAppName(appName);
            appInfo.setAppDescription(appDescription);
            appInfo.setForceUpdate(forceUpdate != null ? forceUpdate : false);

            // 3. 保存到数据库
            AppInfo savedAppInfo = appInfoRepository.save(appInfo);
            log.info("应用创建成功: appId={}, appName={}", savedAppInfo.getAppId(), savedAppInfo.getAppName());

            return savedAppInfo;

        } catch (Exception e) {
            log.error("创建应用失败: {}", e.getMessage(), e);
            throw new BusinessException("创建应用失败: " + e.getMessage());
        }
    }

    // 添加缺失的辅助方法
    private com.yancey.appupdate.dto.CheckUpdateResponseDto createNoUpdateResponse() {
        return com.yancey.appupdate.dto.CheckUpdateResponseDto.noUpdate();
    }

    private com.yancey.appupdate.dto.CheckUpdateResponseDto createUpdateResponse(AppVersion releaseVersion, Boolean forceUpdate) {
        return com.yancey.appupdate.dto.CheckUpdateResponseDto.hasUpdate(
                releaseVersion.getVersionName(),
                releaseVersion.getVersionCode(),
                releaseVersion.getUpdateDescription(),
                forceUpdate, // 使用应用级别的强制更新设置
                releaseVersion.getDownloadUrl(),
                releaseVersion.getMd5(),
                releaseVersion.getFileSize()
        );
    }
} 