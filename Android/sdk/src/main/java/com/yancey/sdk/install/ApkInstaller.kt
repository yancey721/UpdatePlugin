package com.yancey.sdk.install

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.yancey.sdk.util.Logger
import java.io.File

/**
 * APK安装管理器
 * 处理APK安装Intent和权限引导
 */
class ApkInstaller(private val context: Context) {
    
    companion object {
        private const val TAG = "ApkInstaller"
        private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".update.fileprovider"
    }
    
    /**
     * 安装APK文件
     * @param apkFile APK文件
     * @return 是否成功启动安装
     */
    fun installApk(apkFile: File): Boolean {
        Logger.d(TAG, "准备安装APK: ${apkFile.absolutePath}")
        
        // 检查文件是否存在
        if (!apkFile.exists()) {
            Logger.e(TAG, "APK文件不存在: ${apkFile.absolutePath}")
            return false
        }
        
        // 检查安装权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Logger.w(TAG, "缺少应用安装权限，引导用户到设置页面")
                requestInstallPermission()
                return false
            }
        }
        
        return try {
            val intent = createInstallIntent(apkFile)
            Logger.d(TAG, "启动安装Intent")
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Logger.e(TAG, "找不到合适的安装器应用", e)
            false
        } catch (e: Exception) {
            Logger.e(TAG, "启动安装Intent失败", e)
            false
        }
    }
    
    /**
     * 创建安装Intent
     */
    private fun createInstallIntent(apkFile: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        
        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0+使用FileProvider
            val authority = "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX"
            Logger.d(TAG, "使用FileProvider: $authority")
            FileProvider.getUriForFile(context, authority, apkFile)
        } else {
            // Android 7.0以下直接使用文件URI
            Logger.d(TAG, "使用文件URI: ${apkFile.absolutePath}")
            Uri.fromFile(apkFile)
        }
        
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        Logger.d(TAG, "创建安装Intent: data=$apkUri")
        return intent
    }
    
    /**
     * 请求安装权限（Android 8.0+）
     */
    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                Logger.d(TAG, "引导用户到应用安装权限设置页面")
                context.startActivity(intent)
            } catch (e: Exception) {
                Logger.e(TAG, "无法打开应用安装权限设置页面", e)
                // 降级到通用设置页面
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Logger.e(TAG, "无法打开安全设置页面", e2)
                }
            }
        }
    }
    
    /**
     * 检查安装权限
     * @return 是否有安装权限
     */
    fun checkInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasPermission = context.packageManager.canRequestPackageInstalls()
            Logger.d(TAG, "安装权限检查结果: $hasPermission")
            hasPermission
        } else {
            Logger.d(TAG, "Android 7.0及以下版本，默认有安装权限")
            true
        }
    }
    
    /**
     * 检查是否可以处理安装Intent
     */
    fun canHandleInstallIntent(): Boolean {
        return try {
            val testIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://test.apk"), "application/vnd.android.package-archive")
            }
            
            val activities = context.packageManager.queryIntentActivities(testIntent, 0)
            val canHandle = activities.isNotEmpty()
            Logger.d(TAG, "系统是否支持APK安装: $canHandle")
            canHandle
        } catch (e: Exception) {
            Logger.e(TAG, "检查安装Intent支持时发生错误", e)
            false
        }
    }
} 