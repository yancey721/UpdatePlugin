package com.yancey.appupdate.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用信息实体
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "app_info")
public class AppInfo {

    /**
     * 应用ID（主键，使用packageName作为唯一标识）
     */
    @Id
    @Column(name = "app_id", length = 100)
    private String appId;

    /**
     * 应用名称
     */
    @Column(name = "app_name", nullable = false, length = 200)
    private String appName;

    /**
     * 应用描述
     */
    @Lob
    @Column(name = "app_description")
    private String appDescription;

    /**
     * 是否强制更新
     */
    @Column(name = "force_update", nullable = false)
    private Boolean forceUpdate = false;

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

    /**
     * 关联的版本列表
     */
    @OneToMany(mappedBy = "appId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppVersion> versions;
} 