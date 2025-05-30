package com.dongshiqian.appupdate.controller;

import com.dongshiqian.appupdate.dto.ApiResponse;
import com.dongshiqian.appupdate.dto.AppInfoWithLatestVersionDto;
import com.dongshiqian.appupdate.dto.AppVersionDto;
import com.dongshiqian.appupdate.entity.AppVersion;
import com.dongshiqian.appupdate.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
     * 处理参数验证异常
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("参数验证失败");
        
        log.warn("参数验证失败: {}", errorMessage);
        
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(errorMessage));
    }

    /**
     * 处理javax.validation.ConstraintViolationException异常
     */
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            javax.validation.ConstraintViolationException ex) {
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("参数验证失败");
        
        log.warn("约束验证失败: {}", errorMessage);
        
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(errorMessage));
    }

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

    /**
     * 查询应用列表
     * 
     * @param appNameQuery 应用名称查询条件（可选）
     * @param pageable 分页参数
     * @return 应用信息及最新版本的分页列表
     */
    @GetMapping("/apps")
    public ResponseEntity<ApiResponse<Page<AppInfoWithLatestVersionDto>>> getApps(
            @RequestParam(value = "appNameQuery", required = false) String appNameQuery,
            @PageableDefault(size = 10, sort = "createTime") Pageable pageable) {
        
        try {
            log.info("查询应用列表: appNameQuery={}, pageable={}", appNameQuery, pageable);
            
            Page<AppInfoWithLatestVersionDto> appsPage = appVersionService.getAppsWithLatestVersion(appNameQuery, pageable);
            
            log.info("查询应用列表成功: 总数={}, 当前页={}, 每页大小={}", 
                    appsPage.getTotalElements(), appsPage.getNumber(), appsPage.getSize());
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", appsPage));
            
        } catch (Exception e) {
            log.error("查询应用列表失败: appNameQuery={}, error={}", appNameQuery, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 查询指定应用的版本列表
     * 
     * @param appId 应用ID
     * @param pageable 分页参数
     * @return 应用版本的分页列表
     */
    @GetMapping("/app/{appId}/versions")
    public ResponseEntity<ApiResponse<Page<AppVersionDto>>> getAppVersions(
            @PathVariable String appId,
            @PageableDefault(size = 10, sort = "versionCode", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        try {
            log.info("查询应用版本列表: appId={}, pageable={}", appId, pageable);
            
            Page<AppVersionDto> versionsPage = appVersionService.getAppVersions(appId, pageable);
            
            log.info("查询应用版本列表成功: appId={}, 总数={}, 当前页={}, 每页大小={}", 
                    appId, versionsPage.getTotalElements(), versionsPage.getNumber(), versionsPage.getSize());
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", versionsPage));
            
        } catch (Exception e) {
            log.error("查询应用版本列表失败: appId={}, error={}", appId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 修改应用版本信息
     * 
     * @param versionId 版本ID
     * @param updateRequest 更新请求
     * @return 更新后的版本信息
     */
    @PutMapping("/version/{versionId}")
    public ResponseEntity<ApiResponse<AppVersionDto>> updateAppVersion(
            @PathVariable Long versionId,
            @RequestBody @javax.validation.Valid com.dongshiqian.appupdate.dto.UpdateVersionRequestDto updateRequest) {
        
        try {
            log.info("修改应用版本信息: versionId={}, request={}", versionId, updateRequest);
            
            AppVersionDto updatedVersion = appVersionService.updateAppVersion(versionId, updateRequest);
            
            log.info("修改应用版本信息成功: versionId={}, versionCode={}", versionId, updatedVersion.getVersionCode());
            
            return ResponseEntity.ok(ApiResponse.success("修改成功", updatedVersion));
            
        } catch (Exception e) {
            log.error("修改应用版本信息失败: versionId={}, error={}", versionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 删除应用版本
     * 
     * @param versionId 版本ID
     * @param forceDelete 是否强制删除文件
     * @return 删除结果
     */
    @DeleteMapping("/version/{versionId}")
    public ResponseEntity<ApiResponse<Void>> deleteAppVersion(
            @PathVariable Long versionId,
            @RequestParam(value = "forceDelete", defaultValue = "true") Boolean forceDelete) {
        
        try {
            log.info("删除应用版本: versionId={}, forceDelete={}", versionId, forceDelete);
            
            appVersionService.deleteAppVersion(versionId, forceDelete);
            
            log.info("删除应用版本成功: versionId={}", versionId);
            
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (Exception e) {
            log.error("删除应用版本失败: versionId={}, error={}", versionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 批量删除应用版本
     * 
     * @param batchDeleteRequest 批量删除请求
     * @return 删除结果
     */
    @DeleteMapping("/versions")
    public ResponseEntity<ApiResponse<java.util.List<Long>>> batchDeleteVersions(
            @RequestBody @javax.validation.Valid com.dongshiqian.appupdate.dto.BatchDeleteRequestDto batchDeleteRequest) {
        
        try {
            log.info("批量删除应用版本: request={}", batchDeleteRequest);
            
            java.util.List<Long> successIds = appVersionService.batchDeleteVersions(batchDeleteRequest);
            
            log.info("批量删除应用版本成功: 成功数量={}", successIds.size());
            
            return ResponseEntity.ok(ApiResponse.success("批量删除成功", successIds));
            
        } catch (Exception e) {
            log.error("批量删除应用版本失败: error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 更新版本状态
     * 
     * @param versionId 版本ID
     * @param statusRequest 状态更新请求
     * @return 更新后的版本信息
     */
    @PutMapping("/version/{versionId}/status")
    public ResponseEntity<ApiResponse<AppVersionDto>> updateVersionStatus(
            @PathVariable Long versionId,
            @RequestBody @javax.validation.Valid com.dongshiqian.appupdate.dto.UpdateStatusRequestDto statusRequest) {
        
        try {
            log.info("更新版本状态: versionId={}, newStatus={}", versionId, statusRequest.getStatus());
            
            AppVersionDto updatedVersion = appVersionService.updateVersionStatus(versionId, statusRequest);
            
            log.info("更新版本状态成功: versionId={}, status={}", versionId, updatedVersion.getStatus());
            
            return ResponseEntity.ok(ApiResponse.success("状态更新成功", updatedVersion));
            
        } catch (Exception e) {
            log.error("更新版本状态失败: versionId={}, error={}", versionId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * 获取版本统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<com.dongshiqian.appupdate.dto.VersionStatsDto>> getVersionStats() {
        
        try {
            log.info("获取版本统计信息");
            
            com.dongshiqian.appupdate.dto.VersionStatsDto stats = appVersionService.getVersionStats();
            
            log.info("获取版本统计信息成功: totalApps={}, totalVersions={}", stats.getTotalApps(), stats.getTotalVersions());
            
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", stats));
            
        } catch (Exception e) {
            log.error("获取版本统计信息失败: error={}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }
} 