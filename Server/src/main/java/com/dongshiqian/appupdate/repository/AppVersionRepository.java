package com.dongshiqian.appupdate.repository;

import com.dongshiqian.appupdate.entity.AppInfo;
import com.dongshiqian.appupdate.entity.AppVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用版本Repository
 * 
 * @author dongshiqian
 * @version 1.0
 * @since 2024-05-30
 */
@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

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
     * 查找指定应用的最新启用版本
     * 
     * @param appInfo 应用信息
     * @param status 状态（1-启用）
     * @return 最新启用版本
     */
    Optional<AppVersion> findTopByAppInfoAndStatusOrderByVersionCodeDesc(AppInfo appInfo, Integer status);

    /**
     * 查找指定应用中版本号大于指定值的最新启用版本
     * 
     * @param appInfo 应用信息
     * @param versionCode 当前版本号
     * @param status 状态（1-启用）
     * @return 更新版本
     */
    Optional<AppVersion> findTopByAppInfoAndVersionCodeGreaterThanAndStatusOrderByVersionCodeDesc(
            AppInfo appInfo, Integer versionCode, Integer status);

    /**
     * 查找指定应用的所有启用版本
     * 
     * @param appInfo 应用信息
     * @param status 状态（1-启用）
     * @return 启用版本列表
     */
    List<AppVersion> findByAppInfoAndStatusOrderByVersionCodeDesc(AppInfo appInfo, Integer status);

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
     * 统计指定应用的启用版本数量
     * 
     * @param appInfo 应用信息
     * @param status 状态（1-启用）
     * @return 启用版本数量
     */
    long countByAppInfoAndStatus(AppInfo appInfo, Integer status);

    /**
     * 查找所有需要更新的版本（版本号大于指定值的启用版本）
     * 
     * @param appId 应用ID
     * @param currentVersionCode 当前版本号
     * @param status 状态（1-启用）
     * @return 可更新的版本列表
     */
    @Query("SELECT av FROM AppVersion av JOIN av.appInfo ai " +
           "WHERE ai.appId = :appId AND av.versionCode > :currentVersionCode AND av.status = :status " +
           "ORDER BY av.versionCode DESC")
    List<AppVersion> findUpdatableVersions(@Param("appId") String appId, 
                                          @Param("currentVersionCode") Integer currentVersionCode, 
                                          @Param("status") Integer status);
} 