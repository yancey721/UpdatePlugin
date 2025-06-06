package com.yancey.sdk.util

import android.util.Log
import com.yancey.sdk.config.LogLevel

/**
 * SDK日志工具类
 * 支持配置日志级别和开关
 */
object Logger {
    
    private const val DEFAULT_TAG = "AppUpdaterSDK"
    private var isEnabled: Boolean = false
    private var logLevel: LogLevel = LogLevel.INFO
    
    /**
     * 初始化日志配置
     * @param enabled 是否启用日志
     * @param level 日志级别
     */
    fun init(enabled: Boolean, level: LogLevel) {
        isEnabled = enabled
        logLevel = level
    }
    
    /**
     * DEBUG级别日志
     */
    fun d(tag: String = DEFAULT_TAG, message: String) {
        if (isEnabled && logLevel <= LogLevel.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    /**
     * INFO级别日志
     */
    fun i(tag: String = DEFAULT_TAG, message: String) {
        if (isEnabled && logLevel <= LogLevel.INFO) {
            Log.i(tag, message)
        }
    }
    
    /**
     * WARN级别日志
     */
    fun w(tag: String = DEFAULT_TAG, message: String) {
        if (isEnabled && logLevel <= LogLevel.WARN) {
            Log.w(tag, message)
        }
    }
    
    /**
     * ERROR级别日志
     */
    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.ERROR) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
} 