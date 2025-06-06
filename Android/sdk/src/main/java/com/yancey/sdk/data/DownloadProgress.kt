package com.yancey.sdk.data

/**
 * 下载进度信息
 */
data class DownloadProgress(
    val downloadedBytes: Long,          // 已下载字节数
    val totalBytes: Long,               // 总字节数
    val progressPercent: Int,           // 进度百分比 (0-100)
    val downloadSpeed: Long,            // 下载速度 (字节/秒)
    val status: DownloadStatus          // 下载状态
)

/**
 * 下载状态枚举
 */
enum class DownloadStatus {
    PENDING,        // 等待开始
    DOWNLOADING,    // 下载中
    PAUSED,         // 暂停
    SUCCESSFUL,     // 下载成功
    FAILED,         // 下载失败
    CANCELED        // 取消
} 