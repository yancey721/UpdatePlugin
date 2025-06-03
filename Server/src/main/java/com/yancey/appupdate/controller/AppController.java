package com.yancey.appupdate.controller;

import com.yancey.appupdate.dto.ApiResponse;
import com.yancey.appupdate.dto.CheckUpdateRequestDto;
import com.yancey.appupdate.dto.CheckUpdateResponseDto;
import com.yancey.appupdate.service.AppVersionService;
import com.yancey.appupdate.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 移动端应用控制器
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppController {

    private final AppVersionService appVersionService;
    private final FileStorageService fileStorageService;

    /**
     * 移动端检查更新接口
     * 
     * @param request 检查更新请求
     * @return 更新信息
     */
    @PostMapping("/check-update")
    public ResponseEntity<ApiResponse<CheckUpdateResponseDto>> checkUpdate(
            @RequestBody @Valid CheckUpdateRequestDto request) {
        
        try {
            log.info("移动端检查更新: appId={}, currentVersionCode={}", 
                    request.getAppId(), request.getCurrentVersionCode());

            CheckUpdateResponseDto response = appVersionService.checkUpdate(
                    request.getAppId(), request.getCurrentVersionCode());

            if (response.getHasUpdate()) {
                log.info("检查更新成功 - 发现更新: appId={}, currentVersionCode={} -> newVersionCode={}", 
                        request.getAppId(), request.getCurrentVersionCode(), response.getNewVersionCode());
                
                return ResponseEntity.ok(ApiResponse.success("发现新版本", response));
            } else {
                log.info("检查更新成功 - 无更新: appId={}, currentVersionCode={}", 
                        request.getAppId(), request.getCurrentVersionCode());
                
                return ResponseEntity.ok(ApiResponse.success("当前已是最新版本", response));
            }
            
        } catch (Exception e) {
            log.error("检查更新失败: appId={}, currentVersionCode={}, error={}", 
                    request.getAppId(), request.getCurrentVersionCode(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    /**
     * APK文件下载接口
     * 
     * @param fileName APK文件名
     * @param httpRequest HTTP请求
     * @return 文件流
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadApk(
            @PathVariable String fileName,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("开始下载APK: fileName={}", fileName);

            // 1. 加载文件作为Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            if (!resource.exists()) {
                log.warn("APK文件不存在: fileName={}", fileName);
                return ResponseEntity.notFound().build();
            }

            // 2. 确定文件的内容类型
            String contentType = null;
            try {
                Path filePath = fileStorageService.resolveApkPath(fileName);
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                log.warn("无法确定文件类型: fileName={}, error={}", fileName, ex.getMessage());
            }

            // 如果无法确定文件类型，则使用默认值
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/vnd.android.package-archive";
            }

            // 3. 设置响应头
            String headerValue = "attachment; filename=\"" + fileName + "\"";
            
            log.info("APK文件下载成功: fileName={}, contentType={}, fileSize={}", 
                    fileName, contentType, resource.contentLength());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(resource);
            
        } catch (Exception e) {
            log.error("APK文件下载失败: fileName={}, error={}", fileName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

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
        
        log.warn("移动端API参数验证失败: {}", errorMessage);
        
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(errorMessage));
    }

    /**
     * 处理约束验证异常
     */
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            javax.validation.ConstraintViolationException ex) {
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("参数验证失败");
        
        log.warn("移动端API约束验证失败: {}", errorMessage);
        
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(errorMessage));
    }
} 