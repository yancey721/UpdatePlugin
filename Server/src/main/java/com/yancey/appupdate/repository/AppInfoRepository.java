package com.yancey.appupdate.repository;

import com.yancey.appupdate.entity.AppInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 应用信息Repository
 * 
 * @author yancey
 * @version 1.0
 * @since 2024-05-30
 */
@Repository
public interface AppInfoRepository extends JpaRepository<AppInfo, Long> {

    /**
     * 根据应用ID查找应用信息
     * 
     * @param appId 应用ID
     * @return 应用信息
     */
    Optional<AppInfo> findByAppId(String appId);

    /**
     * 检查应用ID是否存在
     * 
     * @param appId 应用ID
     * @return 是否存在
     */
    boolean existsByAppId(String appId);

    /**
     * 根据应用名称模糊查询
     * 
     * @param appName 应用名称
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    Page<AppInfo> findByAppNameContainingIgnoreCase(String appName, Pageable pageable);

    /**
     * 查询所有应用信息并按创建时间倒序排列
     * 
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    Page<AppInfo> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 根据包名查找应用信息
     * 
     * @param packageName 包名
     * @return 应用信息
     */
    Optional<AppInfo> findByPackageName(String packageName);

    /**
     * 查询应用信息及其最新版本
     * 
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    @Query(value = "SELECT DISTINCT ai FROM AppInfo ai LEFT JOIN FETCH ai.versions av " +
           "WHERE av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appInfo = ai AND av2.status = 1) " +
           "ORDER BY ai.createTime DESC",
           countQuery = "SELECT COUNT(DISTINCT ai) FROM AppInfo ai LEFT JOIN ai.versions av " +
           "WHERE av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appInfo = ai AND av2.status = 1)")
    Page<AppInfo> findAllWithLatestVersion(Pageable pageable);

    /**
     * 根据应用名称模糊查询应用信息及其最新版本
     * 
     * @param appName 应用名称
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    @Query(value = "SELECT DISTINCT ai FROM AppInfo ai LEFT JOIN FETCH ai.versions av " +
           "WHERE ai.appName LIKE %:appName% AND " +
           "(av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appInfo = ai AND av2.status = 1)) " +
           "ORDER BY ai.createTime DESC",
           countQuery = "SELECT COUNT(DISTINCT ai) FROM AppInfo ai LEFT JOIN ai.versions av " +
           "WHERE ai.appName LIKE %:appName% AND " +
           "(av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appInfo = ai AND av2.status = 1))")
    Page<AppInfo> findByAppNameContainingWithLatestVersion(@Param("appName") String appName, Pageable pageable);
} 