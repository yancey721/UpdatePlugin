package com.yancey.sdk.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.yancey.sdk.util.Logger

/**
 * 权限管理器
 * 统一处理SDK所需的各种权限检查和引导
 */
class PermissionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PermissionManager"
    }
    
    /**
     * 检查应用安装权限
     * @return 是否有安装权限
     */
    fun checkInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = context.packageManager.canRequestPackageInstalls()
            Logger.d(TAG, "安装权限检查 (API ${Build.VERSION.SDK_INT}): $hasPermission")
            hasPermission
        } else {
            Logger.d(TAG, "安装权限检查 (API ${Build.VERSION.SDK_INT}): true (版本兼容)")
            true
        }
    }
    
    /**
     * 请求应用安装权限
     * @return 是否成功打开权限设置页面
     */
    fun requestInstallPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                Logger.d(TAG, "引导用户到应用安装权限设置页面")
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                Logger.e(TAG, "无法打开应用安装权限设置页面", e)
                // 降级到通用安全设置页面
                openSecuritySettings()
            }
        } else {
            Logger.d(TAG, "当前系统版本无需安装权限")
            return true
        }
    }
    
    /**
     * 打开安全设置页面（降级方案）
     */
    private fun openSecuritySettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Logger.d(TAG, "降级到安全设置页面")
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Logger.e(TAG, "无法打开安全设置页面", e)
            // 最后降级到应用设置页面
            openAppSettings()
        }
    }
    
    /**
     * 打开应用设置页面（最终降级方案）
     */
    private fun openAppSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Logger.d(TAG, "最终降级到应用设置页面")
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Logger.e(TAG, "无法打开应用设置页面", e)
            false
        }
    }
    
    /**
     * 获取权限状态摘要信息
     */
    fun getPermissionSummary(): PermissionSummary {
        val installPermission = checkInstallPermission()
        
        return PermissionSummary(
            hasInstallPermission = installPermission,
            androidVersion = Build.VERSION.SDK_INT,
            needsInstallPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        )
    }
    
    /**
     * 权限状态摘要
     */
    data class PermissionSummary(
        val hasInstallPermission: Boolean,
        val androidVersion: Int,
        val needsInstallPermission: Boolean
    ) {
        fun isAllPermissionsGranted(): Boolean = hasInstallPermission
        
        override fun toString(): String {
            return "PermissionSummary(install=$hasInstallPermission, android=$androidVersion, needsInstall=$needsInstallPermission)"
        }
    }
} 