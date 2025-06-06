package com.yancey.sdk.data

import com.google.gson.annotations.SerializedName

/**
 * 检查更新响应数据模型
 * 对应移动端接口文档的响应格式
 */
data class CheckUpdateResponse(
    @SerializedName("code")
    val code: Int,                          // 业务状态码
    
    @SerializedName("message") 
    val message: String,                    // 状态信息
    
    @SerializedName("data")
    val data: UpdateResponseData?,          // 数据内容（可为null）
    
    @SerializedName("timestamp")
    val timestamp: Long                     // 时间戳
)

/**
 * 更新响应数据
 */
data class UpdateResponseData(
    @SerializedName("hasUpdate")
    val hasUpdate: Boolean,                 // 是否有更新
    
    @SerializedName("newVersionCode")
    val newVersionCode: Int?,               // 新版本号（可为空）
    
    @SerializedName("newVersionName")
    val newVersionName: String?,            // 新版本名称（可为空）
    
    @SerializedName("updateDescription")
    val updateDescription: String?,         // 更新说明（可为空）
    
    @SerializedName("forceUpdate")
    val forceUpdate: Boolean?,              // 是否强制更新（可为空）
    
    @SerializedName("downloadUrl")
    val downloadUrl: String?,               // 下载地址（可为空）
    
    @SerializedName("fileSize")
    val fileSize: Long?,                    // 文件大小（字节）（可为空）
    
    @SerializedName("md5")
    val md5: String?                        // 文件MD5值（可为空）
)

/**
 * 扩展函数：将服务端响应数据转换为SDK内部使用的UpdateInfo
 */
fun UpdateResponseData.toUpdateInfo(): UpdateInfo {
    return if (this.hasUpdate) {
        // 有更新时，使用服务端返回的真实数据，空值用默认值替代
        UpdateInfo(
            hasUpdate = true,
            newVersionCode = this.newVersionCode ?: 0,
            newVersionName = this.newVersionName ?: "",
            updateDescription = this.updateDescription ?: "",
            forceUpdate = this.forceUpdate ?: false,
            downloadUrl = this.downloadUrl ?: "",
            fileSize = this.fileSize ?: 0L,
            md5 = this.md5 ?: ""
        )
    } else {
        // 无更新时，提供默认值
        UpdateInfo(
            hasUpdate = false,
            newVersionCode = 0,
            newVersionName = "",
            updateDescription = "",
            forceUpdate = false,
            downloadUrl = "",
            fileSize = 0L,
            md5 = ""
        )
    }
} 