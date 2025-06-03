package com.yancey.appupdate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;

/**
 * 应用配置验证器
 * 在应用启动时验证配置的正确性
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppConfigValidator implements CommandLineRunner {

    private final AppProperties appProperties;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 开始验证应用配置 ===");
        
        validateUploadPath();
        validateDatabasePath();
        validateServerBaseUrl();
        validateAdminApiKey();
        
        log.info("=== 应用配置验证完成 ===");
        logCurrentConfiguration();
    }

    /**
     * 验证上传路径配置
     */
    private void validateUploadPath() {
        String uploadPath = appProperties.getUploadPath();
        log.info("APK上传路径: {}", new File(uploadPath).getAbsolutePath());
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            log.warn("上传目录不存在，将尝试创建: {}", uploadDir.getAbsolutePath());
        } else if (!uploadDir.canWrite()) {
            log.error("上传目录没有写权限: {}", uploadDir.getAbsolutePath());
            throw new IllegalStateException("上传目录没有写权限: " + uploadDir.getAbsolutePath());
        }
    }

    /**
     * 验证数据库路径配置
     */
    private void validateDatabasePath() {
        String dbPath = appProperties.getDbPath();
        log.info("数据库文件路径: {}", new File(dbPath).getAbsolutePath());
        
        File dbDir = new File(dbPath);
        if (!dbDir.exists()) {
            log.warn("数据库目录不存在，将尝试创建: {}", dbDir.getAbsolutePath());
        }
    }

    /**
     * 验证服务器基础URL配置
     */
    private void validateServerBaseUrl() {
        String serverBaseUrl = appProperties.getServerBaseUrl();
        log.info("服务器基础URL: {}", serverBaseUrl);
        
        if (!StringUtils.hasText(serverBaseUrl)) {
            log.error("服务器基础URL不能为空");
            throw new IllegalStateException("服务器基础URL不能为空");
        }
        
        try {
            new URL(serverBaseUrl);
        } catch (Exception e) {
            log.error("服务器基础URL格式不正确: {}", serverBaseUrl, e);
            throw new IllegalStateException("服务器基础URL格式不正确: " + serverBaseUrl, e);
        }
    }

    /**
     * 验证管理API密钥配置
     */
    private void validateAdminApiKey() {
        String apiKey = appProperties.getAdmin().getApiKey();
        
        if (!StringUtils.hasText(apiKey) || "your-secret-api-key".equals(apiKey)) {
            log.warn("管理API密钥使用默认值，建议在生产环境中修改");
            log.warn("可以通过环境变量 ADMIN_API_KEY 或配置文件设置自定义密钥");
        } else {
            log.info("管理API密钥已配置 (长度: {} 字符)", apiKey.length());
        }
    }

    /**
     * 输出当前配置信息
     */
    private void logCurrentConfiguration() {
        log.info("");
        log.info("=== 当前应用配置 ===");
        log.info("APK上传路径: {}", new File(appProperties.getUploadPath()).getAbsolutePath());
        log.info("数据库文件路径: {}", new File(appProperties.getDbPath()).getAbsolutePath());
        log.info("服务器基础URL: {}", appProperties.getServerBaseUrl());
        log.info("管理API密钥长度: {} 字符", appProperties.getAdmin().getApiKey().length());
        log.info("==================");
        log.info("");
    }
} 