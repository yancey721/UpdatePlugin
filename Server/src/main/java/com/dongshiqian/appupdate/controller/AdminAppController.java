package com.dongshiqian.appupdate.controller;

import com.dongshiqian.appupdate.dto.ApiResponse;
import com.dongshiqian.appupdate.dto.AppVersionDto;
import com.dongshiqian.appupdate.entity.AppVersion;
import com.dongshiqian.appupdate.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

/**
 * 管理端应用控制器
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/app")
@RequiredArgsConstructor
public class AdminAppController {

    private final AppVersionService appVersionService;

    /**
     * 上传APK文件并创建版本
     * 
     * @param apkFile APK文件
     * @param appId 应用ID
     * @param updateDescription 更新说明
     * @param forceUpdate 是否强制更新
     * @return 创建的版本信息
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<AppVersionDto>> uploadAppVersion(
            @RequestParam("apkFile") MultipartFile apkFile,
            @RequestParam("appId") @NotBlank String appId,
            @RequestParam(value = "updateDescription", required = false) String updateDescription,
            @RequestParam(value = "forceUpdate", defaultValue = "false") Boolean forceUpdate) {
        
        try {
            log.info("开始上传APK: appId={}, fileName={}, size={}", 
                    appId, apkFile.getOriginalFilename(), apkFile.getSize());

            AppVersion savedVersion = appVersionService.createAppVersion(apkFile, appId, updateDescription, forceUpdate);
            AppVersionDto versionDto = appVersionService.convertToDto(savedVersion);

            log.info("APK上传成功: appId={}, versionCode={}, versionName={}", 
                    appId, savedVersion.getVersionCode(), savedVersion.getVersionName());

            return ResponseEntity.ok(ApiResponse.success("APK上传成功", versionDto));
            
        } catch (Exception e) {
            log.error("APK上传失败: appId={}, error={}", appId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }
} 