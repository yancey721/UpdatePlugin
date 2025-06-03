package com.dongshiqian.appupdate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 应用配置属性
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * APK文件上传存储路径
     */
    private String uploadPath = "../apk_uploads/";

    /**
     * H2数据库文件路径
     */
    private String dbPath = "./database/";

    /**
     * 服务器基础URL，用于生成下载链接
     */
    private String serverBaseUrl = "http://localhost:8080";

    /**
     * 管理端配置
     */
    private Admin admin = new Admin();

    /**
     * 管理端配置内部类
     */
    @Data
    public static class Admin {
        /**
         * 管理端API密钥
         */
        private String apiKey = "your-secret-api-key";
    }

    /**
     * 初始化后创建必要的目录
     */
    @PostConstruct
    public void init() {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + uploadDir.getAbsolutePath());
            }
        }

        // 创建数据库目录
        File dbDir = new File(dbPath);
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("Created database directory: " + dbDir.getAbsolutePath());
            }
        }
    }

    /**
     * 获取标准化的上传路径（确保以/结尾）
     */
    public String getNormalizedUploadPath() {
        return uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
    }

    /**
     * 获取标准化的服务器基础URL（确保不以/结尾）
     */
    public String getNormalizedServerBaseUrl() {
        return serverBaseUrl.endsWith("/") ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1) : serverBaseUrl;
    }
} 