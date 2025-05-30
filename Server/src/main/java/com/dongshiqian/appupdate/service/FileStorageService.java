package com.dongshiqian.appupdate.service;

import com.dongshiqian.appupdate.config.AppProperties;
import com.dongshiqian.appupdate.exception.FileStorageException;
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
 * @author dongshiqian
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
     * @return 存储的文件名
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

        // 生成文件名: {appId}-{versionCode}.apk
        String extension = getFileExtension(originalFileName);
        if (!".apk".equalsIgnoreCase(extension)) {
            throw new FileStorageException("只支持APK文件格式");
        }

        String fileName = appId + "-" + versionCode + extension;
        
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("APK文件存储成功: {} -> {}", originalFileName, fileName);
            return fileName;
        } catch (IOException ex) {
            log.error("存储文件失败: {}", fileName, ex);
            throw new FileStorageException("存储文件失败: " + fileName, ex);
        }
    }

    /**
     * 解析APK文件路径
     * 
     * @param storedFileName 存储的文件名
     * @return 文件路径
     */
    public Path resolveApkPath(String storedFileName) {
        return fileStorageLocation.resolve(storedFileName).normalize();
    }

    /**
     * 生成下载URL
     * 
     * @param storedFileName 存储的文件名
     * @return 下载URL
     */
    public String generateDownloadUrl(String storedFileName) {
        return serverBaseUrl + "/api/app/download/" + storedFileName;
    }

    /**
     * 加载文件作为资源
     * 
     * @param fileName 文件名
     * @return 文件资源
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("文件不存在: " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("文件路径格式错误: {}", fileName, ex);
            throw new FileStorageException("文件路径格式错误: " + fileName, ex);
        }
    }

    /**
     * 删除文件
     * 
     * @param fileName 文件名或完整路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath;
            if (fileName.startsWith(fileStorageLocation.toString())) {
                // 如果是完整路径
                filePath = Paths.get(fileName);
            } else {
                // 如果是文件名
                filePath = this.fileStorageLocation.resolve(fileName).normalize();
            }
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            } else {
                log.warn("文件不存在，无需删除: {}", filePath);
            }
            return deleted;
        } catch (IOException ex) {
            log.error("删除文件失败: {}", fileName, ex);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    /**
     * 获取文件大小
     * 
     * @param fileName 文件名
     * @return 文件大小（字节）
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            log.error("获取文件大小失败: {}", fileName, ex);
            throw new FileStorageException("获取文件大小失败: " + fileName, ex);
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
} 