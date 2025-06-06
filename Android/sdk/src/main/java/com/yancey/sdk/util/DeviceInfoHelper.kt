package com.yancey.sdk.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.yancey.sdk.data.DeviceInfo

/**
 * 设备信息助手类
 * 用于获取设备相关信息
 */
object DeviceInfoHelper {
    
    /**
     * 获取当前应用版本号
     */
    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("DeviceInfoHelper", "Failed to get version code", e)
            1
        }
    }
    
    /**
     * 获取当前应用版本名
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("DeviceInfoHelper", "Failed to get version name", e)
            "1.0.0"
        }
    }
    
    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL,
            brand = Build.BRAND,
            osVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT
        )
    }
    
    /**
     * 获取设备信息摘要（用于日志）
     */
    fun getDeviceInfoSummary(): String {
        return "${Build.BRAND} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})"
    }
} 