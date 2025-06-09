package com.yancey.sdk

import android.content.Context
import com.yancey.sdk.callback.DownloadCallback
import com.yancey.sdk.callback.InstallCallback
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.core.UpdateManager
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.util.Logger
import java.io.File

/**
 * 应用更新SDK主入口类
 * 使用单例模式，提供统一的API接口
 */
object AppUpdaterSDK {
    
    // 版本信息
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
    
    // 私有变量
    private var config: UpdateConfig? = null
    private var updateManager: UpdateManager? = null
    private var isInitialized: Boolean = false
    
    /**
     * 初始化SDK
     * @param context 应用上下文（建议传入Activity）
     * @param updateConfig SDK配置
     */
    fun init(context: Context, updateConfig: UpdateConfig) {
        this.config = updateConfig
        // 注意：为了支持默认UI对话框，这里保留原始context而不是applicationContext
        this.updateManager = UpdateManager(context, updateConfig)
        this.isInitialized = true
        
        // 初始化日志
        Logger.init(updateConfig.enableLog, updateConfig.logLevel)
        Logger.i("AppUpdaterSDK", "SDK initialized successfully. Version: $VERSION_NAME")
        Logger.i("AppUpdaterSDK", "Context type: ${context.javaClass.simpleName}")
    }
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * 获取SDK版本信息
     */
    fun getVersionInfo(): String = "AppUpdaterSDK v$VERSION_NAME ($VERSION_CODE)"
    
    /**
     * 检查应用更新
     * @param callback 更新检查回调
     */
    fun checkUpdate(callback: UpdateCallback) {
        if (!ensureInitialized(callback)) return
        
        Logger.d("AppUpdaterSDK", "Starting update check...")
        updateManager?.checkUpdate(callback)
    }
    
    /**
     * 开始下载更新
     * @param updateInfo 更新信息
     * @param downloadCallback 下载回调（可选）
     */
    fun startDownload(updateInfo: UpdateInfo, downloadCallback: DownloadCallback? = null) {
        if (!ensureInitialized()) return
        
        Logger.d("AppUpdaterSDK", "Starting download for version: ${updateInfo.newVersionName}")
        updateManager?.startDownload(updateInfo, downloadCallback)
    }
    
    /**
     * 取消当前下载
     */
    fun cancelDownload() {
        if (!ensureInitialized()) return
        
        Logger.d("AppUpdaterSDK", "Cancelling download...")
        updateManager?.cancelDownload()
    }
    
    /**
     * 安装APK文件
     * @param apkFile APK文件
     * @param installCallback 安装回调（可选）
     */
    fun installApk(apkFile: File, installCallback: InstallCallback? = null) {
        if (!ensureInitialized()) return
        
        Logger.d("AppUpdaterSDK", "Installing APK: ${apkFile.absolutePath}")
        updateManager?.installApk(apkFile, installCallback)
    }
    
    /**
     * 检查安装权限
     * @return 是否有安装权限
     */
    fun checkInstallPermission(): Boolean {
        if (!ensureInitialized()) return false
        
        return updateManager?.checkInstallPermission() ?: false
    }
    
    /**
     * 请求安装权限
     * @return 是否成功打开权限设置页面
     */
    fun requestInstallPermission(): Boolean {
        if (!ensureInitialized()) return false
        
        return updateManager?.requestInstallPermission() ?: false
    }
    
    /**
     * 检查从设置页返回后的权限状态（用于Activity的onResume中）
     * @return 是否有待安装任务并已处理
     */
    fun checkAndHandlePendingInstall(): Boolean {
        if (!ensureInitialized()) return false
        
        return updateManager?.checkAndHandlePendingInstall() ?: false
    }
    
    /**
     * 获取当前配置
     */
    fun getConfig(): UpdateConfig? = config
    
    /**
     * 释放SDK资源
     */
    fun release() {
        Logger.d("AppUpdaterSDK", "Releasing SDK resources...")
        updateManager?.release()
        updateManager = null
        config = null
        isInitialized = false
    }
    
    /**
     * 确保SDK已初始化（带错误回调）
     */
    private fun ensureInitialized(callback: UpdateCallback? = null): Boolean {
        if (!isInitialized) {
            val errorMsg = "AppUpdaterSDK not initialized. Please call init() first."
            Logger.e("AppUpdaterSDK", errorMsg)
            callback?.onError(-1, errorMsg)
            return false
        }
        return true
    }
    
    /**
     * 确保SDK已初始化（无回调）
     */
    private fun ensureInitialized(): Boolean {
        if (!isInitialized) {
            Logger.e("AppUpdaterSDK", "AppUpdaterSDK not initialized. Please call init() first.")
            return false
        }
        return true
    }
} 