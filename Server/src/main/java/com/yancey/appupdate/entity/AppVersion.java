package com.yancey.appupdate.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 应用版本实体
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "app_version")
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的应用ID（packageName）
     */
    @Column(name = "app_id", nullable = false, length = 100)
    private String appId;

    /**
     * 版本号（数字）
     */
    @Column(name = "version_code", nullable = false)
    private Integer versionCode;

    /**
     * 版本名称（字符串）
     */
    @Column(name = "version_name", nullable = false, length = 50)
    private String versionName;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件MD5值
     */
    @Column(name = "md5", length = 32)
    private String md5;

    /**
     * APK文件在服务器上的存储路径
     */
    @Column(name = "apk_path", nullable = false, length = 500)
    private String apkPath;

    /**
     * 下载URL
     */
    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    /**
     * 更新说明
     */
    @Lob
    @Column(name = "update_description")
    private String updateDescription;

    /**
     * 是否强制更新
     */
    @Column(name = "force_update", nullable = false)
    private Boolean forceUpdate = false;

    /**
     * 是否为发布版本
     */
    @Column(name = "is_released", nullable = false)
    private Boolean isReleased = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
} 