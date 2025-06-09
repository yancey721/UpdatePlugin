package com.yancey.appupdate.service;

import com.yancey.appupdate.config.AppProperties;
import com.yancey.appupdate.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 文件存储服务
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final String serverBaseUrl;

    @Autowired
    public FileStorageService(AppProperties appProperties) {
        this.fileStorageLocation = Paths.get(appProperties.getNormalizedUploadPath())
                .toAbsolutePath().normalize();
        this.serverBaseUrl = appProperties.getNormalizedServerBaseUrl();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("文件存储目录初始化成功: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("无法创建文件存储目录: {}", this.fileStorageLocation, ex);
            throw new FileStorageException("无法创建文件存储目录", ex);
        }
    }

    /**
     * 存储APK文件
     * 
     * @param file APK文件
     * @param appId 应用ID
     * @param versionCode 版本号
     * @return 存储的文件路径（相对于根目录）
     */
    public String storeApkFile(MultipartFile file, String appId, String versionCode) {
        // 验证文件
        if (file.isEmpty()) {
            throw new FileStorageException("无法存储空文件");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new FileStorageException("文件名包含无效字符: " + originalFileName);
        }

        // 验证文件扩展名
        String extension = getFileExtension(originalFileName);
        if (!".apk".equalsIgnoreCase(extension)) {
            throw new FileStorageException("只支持APK文件格式");
        }

        // 清理appId，确保可以作为文件夹名使用
        String cleanAppId = sanitizeForFilename(appId);
        
        // 按应用创建子目录: uploads/{cleanAppId}/
        Path appDirectory = this.fileStorageLocation.resolve(cleanAppId);
        
        // 生成文件名: {appId}-{versionCode}.apk
        String fileName = cleanAppId + "-" + versionCode + extension;
        
        // 文件相对路径: {cleanAppId}/{fileName}
        String relativePath = cleanAppId + "/" + fileName;
        
        try {
            // 创建应用专属目录
            Files.createDirectories(appDirectory);
            log.debug("应用目录创建/确认: {}", appDirectory);
            
            // 存储文件
            Path targetLocation = appDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("APK文件存储成功: {} -> {} (路径: {})", originalFileName, fileName, relativePath);
            return relativePath;
        } catch (IOException ex) {
            log.error("存储文件失败: appId={}, fileName={}, error={}", appId, fileName, ex.getMessage(), ex);
            throw new FileStorageException("存储文件失败: " + fileName, ex);
        }
    }

    /**
     * 解析APK文件路径
     * 
     * @param storedFilePath 存储的文件路径（相对路径）
     * @return 文件路径
     */
    public Path resolveApkPath(String storedFilePath) {
        return fileStorageLocation.resolve(storedFilePath).normalize();
    }

    /**
     * 生成下载URL
     * 
     * @param storedFilePath 存储的文件路径（相对路径）
     * @return 下载URL
     */
    public String generateDownloadUrl(String storedFilePath) {
        // 将路径中的分隔符替换为URL分隔符
        String urlPath = storedFilePath.replace("\\", "/");
        return serverBaseUrl + "/api/app/download/" + urlPath;
    }

    /**
     * 加载文件作为资源
     * 
     * @param filePath 文件路径（可以是文件名或相对路径）
     * @return 文件资源
     */
    public Resource loadFileAsResource(String filePath) {
        try {
            Path resolvedPath = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(resolvedPath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("文件不存在: " + filePath);
            }
        } catch (MalformedURLException ex) {
            log.error("文件路径格式错误: {}", filePath, ex);
            throw new FileStorageException("文件路径格式错误: " + filePath, ex);
        }
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件路径（相对路径或完整路径）
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            Path resolvedPath;
            if (filePath.startsWith(fileStorageLocation.toString())) {
                // 如果是完整路径
                resolvedPath = Paths.get(filePath);
            } else {
                // 如果是相对路径
                resolvedPath = this.fileStorageLocation.resolve(filePath).normalize();
            }
            
            boolean deleted = Files.deleteIfExists(resolvedPath);
            if (deleted) {
                log.info("文件删除成功: {}", resolvedPath);
            } else {
                log.warn("文件不存在，无需删除: {}", resolvedPath);
            }
            return deleted;
        } catch (IOException ex) {
            log.error("删除文件失败: {}", filePath, ex);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径（相对路径）
     * @return 是否存在
     */
    public boolean fileExists(String filePath) {
        Path resolvedPath = this.fileStorageLocation.resolve(filePath).normalize();
        return Files.exists(resolvedPath);
    }

    /**
     * 获取文件大小
     * 
     * @param filePath 文件路径（相对路径）
     * @return 文件大小（字节）
     */
    public long getFileSize(String filePath) {
        try {
            Path resolvedPath = this.fileStorageLocation.resolve(filePath).normalize();
            return Files.size(resolvedPath);
        } catch (IOException ex) {
            log.error("获取文件大小失败: {}", filePath, ex);
            throw new FileStorageException("获取文件大小失败: " + filePath, ex);
        }
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }

    /**
     * 获取存储根目录
     * 
     * @return 存储根目录路径
     */
    public Path getStorageLocation() {
        return fileStorageLocation;
    }

    /**
     * 清理文件名，确保可以作为文件/文件夹名使用
     * 
     * @param input 输入字符串
     * @return 清理后的文件名
     */
    private String sanitizeForFilename(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "unknown";
        }
        
        // 清理包名中的特殊字符，只保留字母、数字、点、下划线、连字符
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
} 