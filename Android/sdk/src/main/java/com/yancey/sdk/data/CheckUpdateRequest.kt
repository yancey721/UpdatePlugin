package com.yancey.sdk.data

import com.google.gson.annotations.SerializedName

/**
 * 检查更新请求数据模型
 * 对应移动端接口文档的POST /check-update
 */
data class CheckUpdateRequest(
    @SerializedName("appId")
    val appId: String,                      // 应用ID（包名）
    
    @SerializedName("currentVersionCode")
    val currentVersionCode: Int,            // 当前版本号
    
    @SerializedName("channel")
    val channel: String = "default",        // 渠道信息（可选）
    
    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo? = null      // 设备信息（可选）
)

/**
 * 设备信息数据模型
 */
data class DeviceInfo(
    @SerializedName("model")
    val model: String,                      // 设备型号
    
    @SerializedName("brand")
    val brand: String,                      // 设备品牌
    
    @SerializedName("osVersion")
    val osVersion: String,                  // Android版本
    
    @SerializedName("apiLevel")
    val apiLevel: Int                       // API级别
) 