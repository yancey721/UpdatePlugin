package com.dongshiqian.appupdate.service;

import com.dongshiqian.appupdate.dto.AppVersionDto;
import com.dongshiqian.appupdate.dto.ParsedApkData;
import com.dongshiqian.appupdate.entity.AppInfo;
import com.dongshiqian.appupdate.entity.AppVersion;
import com.dongshiqian.appupdate.exception.BusinessException;
import com.dongshiqian.appupdate.repository.AppInfoRepository;
import com.dongshiqian.appupdate.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * @author dongshiqian
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
} 