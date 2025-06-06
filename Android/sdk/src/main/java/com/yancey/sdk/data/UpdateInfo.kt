package com.yancey.sdk.data

/**
 * 更新信息数据模型
 * 包含服务端返回的版本更新相关信息
 */
data class UpdateInfo(
    // 基础信息
    val hasUpdate: Boolean,                 // 是否有更新
    val newVersionCode: Int,                // 新版本号
    val newVersionName: String,             // 新版本名称
    
    // 更新内容
    val updateDescription: String,          // 更新说明
    val forceUpdate: Boolean,               // 是否强制更新
    
    // 文件信息
    val downloadUrl: String,                // 下载地址
    val fileSize: Long,                     // 文件大小（字节）
    val md5: String                         // 文件MD5值
) 