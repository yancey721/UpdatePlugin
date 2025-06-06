package com.yancey.sdk.config

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes

/**
 * SDK配置类
 * 使用Builder模式进行配置
 */
@ConsistentCopyVisibility
data class UpdateConfig private constructor(
    // 必填配置
    val baseUrl: String,                    // 服务端API基础URL
    val appId: String,                      // 应用ID（通常为包名）
    
    // UI配置
    val enableDefaultUI: Boolean,           // 是否启用默认更新对话框
    val dialogTheme: Int?,                  // 自定义对话框主题
    val notificationIcon: Int?,             // 下载通知图标
    
    // 下载配置  
    val showNotification: Boolean,          // 是否显示下载通知
    val downloadPath: String?,              // 自定义下载路径
    
    // 安装配置
    val autoInstall: Boolean,               // 下载完成后是否自动引导安装
    val enableInstallGuide: Boolean,        // 是否启用权限引导
    
    // 网络配置
    val connectTimeout: Int,                // 连接超时(毫秒)
    val readTimeout: Int,                   // 读取超时(毫秒)
    
    // 调试配置
    val enableLog: Boolean,                 // 是否启用日志
    val logLevel: LogLevel                  // 日志级别
) {
    
    class Builder(private val context: Context) {
        private var baseUrl: String? = null
        private var appId: String? = null
        private var enableDefaultUI: Boolean = true
        private var dialogTheme: Int? = null
        private var notificationIcon: Int? = null
        private var showNotification: Boolean = true
        private var downloadPath: String? = null
        private var autoInstall: Boolean = true
        private var enableInstallGuide: Boolean = true
        private var connectTimeout: Int = 10000
        private var readTimeout: Int = 30000
        private var enableLog: Boolean = false
        private var logLevel: LogLevel = LogLevel.INFO
        
        // 必填参数
        fun setBaseUrl(url: String): Builder {
            this.baseUrl = url
            return this
        }
        
        fun setAppId(appId: String): Builder {
            this.appId = appId
            return this
        }
        
        // UI配置
        fun enableDefaultUI(enable: Boolean = true): Builder {
            this.enableDefaultUI = enable
            return this
        }
        
        fun setDialogTheme(@StyleRes themeResId: Int): Builder {
            this.dialogTheme = themeResId
            return this
        }
        
        fun setNotificationIcon(@DrawableRes iconResId: Int): Builder {
            this.notificationIcon = iconResId
            return this
        }
        
        // 下载配置
        fun showNotification(show: Boolean = true): Builder {
            this.showNotification = show
            return this
        }
        
        fun setDownloadPath(path: String): Builder {
            this.downloadPath = path
            return this
        }
        
        // 安装配置
        fun autoInstall(auto: Boolean = true): Builder {
            this.autoInstall = auto
            return this
        }
        
        fun enableInstallGuide(enable: Boolean = true): Builder {
            this.enableInstallGuide = enable
            return this
        }
        
        // 网络配置
        fun setConnectTimeout(timeout: Int): Builder {
            this.connectTimeout = timeout
            return this
        }
        
        fun setReadTimeout(timeout: Int): Builder {
            this.readTimeout = timeout
            return this
        }
        
        // 调试配置
        fun enableLog(enable: Boolean = false): Builder {
            this.enableLog = enable
            return this
        }
        
        fun setLogLevel(level: LogLevel = LogLevel.INFO): Builder {
            this.logLevel = level
            return this
        }
        
        fun build(): UpdateConfig {
            require(!baseUrl.isNullOrBlank()) { "baseUrl cannot be null or empty" }
            require(!appId.isNullOrBlank()) { "appId cannot be null or empty" }
            
            return UpdateConfig(
                baseUrl = baseUrl!!.let { if (it.endsWith("/")) it else "$it/" },
                appId = appId!!,
                enableDefaultUI = enableDefaultUI,
                dialogTheme = dialogTheme,
                notificationIcon = notificationIcon,
                showNotification = showNotification,
                downloadPath = downloadPath,
                autoInstall = autoInstall,
                enableInstallGuide = enableInstallGuide,
                connectTimeout = connectTimeout,
                readTimeout = readTimeout,
                enableLog = enableLog,
                logLevel = logLevel
            )
        }
    }
}

/**
 * 日志级别枚举
 */
enum class LogLevel { 
    DEBUG, INFO, WARN, ERROR 
} 