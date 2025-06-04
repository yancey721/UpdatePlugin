package com.yancey.appupdate.repository;

import com.yancey.appupdate.entity.AppInfo;
import com.yancey.appupdate.entity.AppVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用版本Repository
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    // ===========================================
    // 发布版本管理相关方法（新增）
    // ===========================================

    /**
     * 查找应用的当前发布版本
     * 
     * @param appId 应用ID
     * @return 发布版本
     */
    Optional<AppVersion> findByAppInfo_AppIdAndIsReleasedTrue(String appId);

    /**
     * 批量更新应用的所有版本为非发布状态
     * 
     * @param appId 应用ID
     * @return 更新的记录数
     */
    @Modifying
    @Query(value = "UPDATE app_version SET is_released = false WHERE app_info_id = (SELECT id FROM app_info WHERE app_id = :appId)", nativeQuery = true)
    int updateAllVersionsToNonReleased(@Param("appId") String appId);

    /**
     * 查找应用的所有发布版本（用于验证数据一致性）
     * 
     * @param appId 应用ID
     * @return 发布版本列表
     */
    List<AppVersion> findByAppInfo_AppIdAndIsReleasedTrueOrderByVersionCodeDesc(String appId);

    /**
     * 统计发布版本数量
     * 
     * @return 发布版本总数
     */
    long countByIsReleasedTrue();

    // ===========================================
    // 原有方法（保持兼容性）
    // ===========================================

    /**
     * 根据应用信息和版本号查找版本
     * 
     * @param appInfo 应用信息
     * @param versionCode 版本号
     * @return 应用版本
     */
    Optional<AppVersion> findByAppInfoAndVersionCode(AppInfo appInfo, Integer versionCode);

    /**
     * 查找指定应用的所有版本，按版本号倒序排列
     * 
     * @param appInfo 应用信息
     * @param pageable 分页参数
     * @return 应用版本分页列表
     */
    Page<AppVersion> findByAppInfoOrderByVersionCodeDesc(AppInfo appInfo, Pageable pageable);

    /**
     * 查找指定应用的所有版本，按创建时间倒序排列
     * 
     * @param appInfo 应用信息
     * @param pageable 分页参数
     * @return 应用版本分页列表
     */
    Page<AppVersion> findByAppInfoOrderByCreateTimeDesc(AppInfo appInfo, Pageable pageable);

    /**
     * 检查指定应用和版本号的版本是否存在
     * 
     * @param appInfo 应用信息
     * @param versionCode 版本号
     * @return 是否存在
     */
    boolean existsByAppInfoAndVersionCode(AppInfo appInfo, Integer versionCode);

    /**
     * 根据APK路径查找版本
     * 
     * @param apkPath APK文件路径
     * @return 应用版本
     */
    Optional<AppVersion> findByApkPath(String apkPath);

    /**
     * 统计指定应用的版本数量
     * 
     * @param appInfo 应用信息
     * @return 版本数量
     */
    long countByAppInfo(AppInfo appInfo);

    /**
     * 统计强制更新的版本数量
     * 
     * @return 强制更新版本数量
     */
    long countByForceUpdateTrue();

    /**
     * 计算所有版本文件的总大小
     * 
     * @return 文件总大小
     */
    @Query("SELECT SUM(av.fileSize) FROM AppVersion av")
    Long sumFileSize();

    /**
     * 查找最近创建的10个版本
     * 
     * @return 最近版本列表
     */
    List<AppVersion> findTop10ByOrderByCreateTimeDesc();
} 