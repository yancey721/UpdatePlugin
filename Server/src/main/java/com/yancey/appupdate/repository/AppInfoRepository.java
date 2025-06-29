package com.yancey.appupdate.repository;

import com.yancey.appupdate.entity.AppInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
public interface AppInfoRepository extends JpaRepository<AppInfo, String> {

    // ===========================================
    // 强制更新管理相关方法
    // ===========================================

    /**
     * 更新应用的强制更新设置
     * 
     * @param appId 应用ID（packageName）
     * @param forceUpdate 是否强制更新
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE AppInfo ai SET ai.forceUpdate = :forceUpdate WHERE ai.appId = :appId")
    int updateForceUpdate(@Param("appId") String appId, @Param("forceUpdate") Boolean forceUpdate);

    /**
     * 统计强制更新的应用数量
     * 
     * @return 强制更新应用数量
     */
    long countByForceUpdateTrue();

    // ===========================================
    // 应用基础查询方法
    // ===========================================

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
     * 查询应用信息及其最新发布版本
     * 
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    @Query(value = "SELECT DISTINCT ai FROM AppInfo ai LEFT JOIN FETCH ai.versions av " +
           "WHERE av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appId = ai.appId AND av2.isReleased = true) " +
           "ORDER BY ai.createTime DESC",
           countQuery = "SELECT COUNT(DISTINCT ai) FROM AppInfo ai LEFT JOIN ai.versions av " +
           "WHERE av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appId = ai.appId AND av2.isReleased = true)")
    Page<AppInfo> findAllWithLatestVersion(Pageable pageable);

    /**
     * 根据应用名称模糊查询应用信息及其最新发布版本
     * 
     * @param appName 应用名称
     * @param pageable 分页参数
     * @return 应用信息分页列表
     */
    @Query(value = "SELECT DISTINCT ai FROM AppInfo ai LEFT JOIN FETCH ai.versions av " +
           "WHERE ai.appName LIKE %:appName% AND " +
           "(av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appId = ai.appId AND av2.isReleased = true)) " +
           "ORDER BY ai.createTime DESC",
           countQuery = "SELECT COUNT(DISTINCT ai) FROM AppInfo ai LEFT JOIN ai.versions av " +
           "WHERE ai.appName LIKE %:appName% AND " +
           "(av.id IS NULL OR av.id = (SELECT MAX(av2.id) FROM AppVersion av2 WHERE av2.appId = ai.appId AND av2.isReleased = true))")
    Page<AppInfo> findByAppNameContainingWithLatestVersion(@Param("appName") String appName, Pageable pageable);
} 