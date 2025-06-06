package com.yancey.sdk.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import com.yancey.sdk.callback.DownloadCallback
import com.yancey.sdk.data.DownloadProgress
import com.yancey.sdk.data.DownloadStatus
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.util.Logger
import kotlinx.coroutines.*
import java.io.File

/**
 * APK下载管理器
 * 使用Android DownloadManager进行文件下载
 */
class ApkDownloadManager(private val context: Context) {
    
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var downloadId: Long = -1
    private var currentCallback: DownloadCallback? = null
    private var progressJob: Job? = null
    private var downloadReceiver: BroadcastReceiver? = null
    private var isDownloading = false
    private var lastProgressTime = 0L
    private var lastDownloadedBytes = 0L
    
    /**
     * 开始下载APK
     */
    fun downloadApk(updateInfo: UpdateInfo, callback: DownloadCallback): Boolean {
        if (isDownloading) {
            Logger.w("ApkDownloadManager", "已有下载任务在进行中")
            return false
        }
        
        try {
            Logger.d("ApkDownloadManager", "开始下载APK: ${updateInfo.downloadUrl}")
            
            currentCallback = callback
            isDownloading = true
            
            // 生成APK文件名
            val fileName = "update_${updateInfo.newVersionName}_${updateInfo.newVersionCode}.apk"
            val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            
            // 如果文件已存在，删除旧文件
            if (destinationFile.exists()) {
                destinationFile.delete()
                Logger.d("ApkDownloadManager", "删除已存在的APK文件")
            }
            
            // 创建下载请求
            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl)).apply {
                setTitle("应用更新")
                setDescription("正在下载 ${updateInfo.newVersionName}")
                setDestinationUri(Uri.fromFile(destinationFile))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
                setAllowedOverRoaming(false)
                
                // 设置请求头
                addRequestHeader("User-Agent", "UpdateSDK/1.0")
            }
            
            // 开始下载
            downloadId = downloadManager.enqueue(request)
            Logger.d("ApkDownloadManager", "下载任务已创建，ID: $downloadId")
            
            // 注册下载完成监听
            registerDownloadCompleteReceiver(updateInfo, destinationFile)
            
            // 开始监听下载进度
            startProgressMonitoring(updateInfo.fileSize)
            
            // 回调下载开始
            mainHandler.post { callback.onDownloadStart() }
            
            return true
            
        } catch (e: Exception) {
            Logger.e("ApkDownloadManager", "启动下载失败", e)
            isDownloading = false
            mainHandler.post { 
                callback.onDownloadError(-1, "启动下载失败: ${e.message}") 
            }
            return false
        }
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload() {
        if (!isDownloading || downloadId == -1L) {
            Logger.w("ApkDownloadManager", "没有正在进行的下载任务")
            return
        }
        
        try {
            Logger.d("ApkDownloadManager", "取消下载任务: $downloadId")
            
            // 移除下载任务
            downloadManager.remove(downloadId)
            
            // 停止进度监听
            stopProgressMonitoring()
            
            // 注销广播接收器
            unregisterDownloadReceiver()
            
            // 重置状态
            resetDownloadState()
            
            // 回调取消
            currentCallback?.let { callback ->
                mainHandler.post { callback.onDownloadCancel() }
            }
            
        } catch (e: Exception) {
            Logger.e("ApkDownloadManager", "取消下载失败", e)
        }
    }
    
    /**
     * 开始监听下载进度
     */
    private fun startProgressMonitoring(totalBytes: Long) {
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (isDownloading && downloadId != -1L) {
                try {
                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val downloadedBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val totalBytesFromDownload = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            
                            // 使用传入的文件大小或下载管理器返回的大小
                            val actualTotalBytes = if (totalBytesFromDownload > 0) totalBytesFromDownload else totalBytes
                            
                            // 计算进度百分比
                            val progressPercent = if (actualTotalBytes > 0) {
                                ((downloadedBytes * 100) / actualTotalBytes).toInt()
                            } else 0
                            
                            // 计算下载速度
                            val currentTime = System.currentTimeMillis()
                            val downloadSpeed = if (lastProgressTime > 0 && currentTime > lastProgressTime) {
                                val timeDiff = (currentTime - lastProgressTime) / 1000.0
                                val bytesDiff = downloadedBytes - lastDownloadedBytes
                                if (timeDiff > 0) (bytesDiff / timeDiff).toLong() else 0L
                            } else 0L
                            
                            lastProgressTime = currentTime
                            lastDownloadedBytes = downloadedBytes
                            
                            // 创建进度信息
                            val downloadProgress = DownloadProgress(
                                downloadedBytes = downloadedBytes,
                                totalBytes = actualTotalBytes,
                                progressPercent = progressPercent,
                                downloadSpeed = downloadSpeed,
                                status = when (status) {
                                    DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
                                    DownloadManager.STATUS_RUNNING -> DownloadStatus.DOWNLOADING
                                    DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                                    DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                                    DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                                    else -> DownloadStatus.DOWNLOADING
                                }
                            )
                            
                            // 回调进度更新
                            currentCallback?.let { callback ->
                                mainHandler.post { callback.onDownloadProgress(downloadProgress) }
                            }
                            
                            // 如果下载失败，处理错误
                            if (status == DownloadManager.STATUS_FAILED) {
                                val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                                handleDownloadError(reason)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("ApkDownloadManager", "查询下载进度失败", e)
                }
                
                // 每500毫秒更新一次进度，提高更新频率
                delay(500)
            }
        }
    }
    
    /**
     * 停止进度监听
     */
    private fun stopProgressMonitoring() {
        progressJob?.cancel()
        progressJob = null
    }
    
    /**
     * 注册下载完成监听
     */
    private fun registerDownloadCompleteReceiver(updateInfo: UpdateInfo, downloadFile: File) {
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    Logger.d("ApkDownloadManager", "下载完成，ID: $id")
                    handleDownloadComplete(updateInfo, downloadFile)
                }
            }
        }
        
        context.registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    
    /**
     * 注销下载完成监听
     */
    private fun unregisterDownloadReceiver() {
        downloadReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Logger.w("ApkDownloadManager", "注销广播接收器失败: ${e.message}")
            }
        }
        downloadReceiver = null
    }
    
    /**
     * 处理下载完成
     */
    private fun handleDownloadComplete(updateInfo: UpdateInfo, downloadFile: File) {
        try {
            if (downloadFile.exists() && downloadFile.length() > 0) {
                Logger.d("ApkDownloadManager", "下载文件验证成功: ${downloadFile.absolutePath}, 大小: ${downloadFile.length()}")
                
                // 确保最后一次进度回调显示100%
                val finalProgress = DownloadProgress(
                    downloadedBytes = downloadFile.length(),
                    totalBytes = downloadFile.length(),
                    progressPercent = 100,
                    downloadSpeed = 0L,
                    status = DownloadStatus.SUCCESSFUL
                )
                
                currentCallback?.let { callback ->
                    mainHandler.post { 
                        callback.onDownloadProgress(finalProgress)
                        // 稍微延迟一下再调用完成回调，确保UI更新
                        mainHandler.postDelayed({
                            callback.onDownloadComplete(downloadFile)
                        }, 100)
                    }
                }
                
                // TODO: 可以在这里添加MD5校验
                
            } else {
                Logger.e("ApkDownloadManager", "下载文件不存在或大小为0")
                currentCallback?.let { callback ->
                    mainHandler.post { callback.onDownloadError(-1, "下载文件验证失败") }
                }
            }
            
            stopProgressMonitoring()
            unregisterDownloadReceiver()
            resetDownloadState()
            
        } catch (e: Exception) {
            Logger.e("ApkDownloadManager", "处理下载完成失败", e)
            stopProgressMonitoring()
            unregisterDownloadReceiver()
            resetDownloadState()
            currentCallback?.let { callback ->
                mainHandler.post { callback.onDownloadError(-1, "处理下载完成失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 处理下载错误
     */
    private fun handleDownloadError(reason: Int) {
        val errorMessage = when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "无法恢复下载"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "未找到存储设备"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "文件已存在"
            DownloadManager.ERROR_FILE_ERROR -> "文件错误"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP数据错误"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "重定向次数过多"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "未处理的HTTP错误码"
            DownloadManager.ERROR_UNKNOWN -> "未知错误"
            else -> "下载失败，错误码: $reason"
        }
        
        Logger.e("ApkDownloadManager", "下载失败: $errorMessage")
        
        stopProgressMonitoring()
        unregisterDownloadReceiver()
        resetDownloadState()
        
        currentCallback?.let { callback ->
            mainHandler.post { callback.onDownloadError(reason, errorMessage) }
        }
    }
    
    /**
     * 重置下载状态
     */
    private fun resetDownloadState() {
        isDownloading = false
        downloadId = -1L
        currentCallback = null
        lastProgressTime = 0L
        lastDownloadedBytes = 0L
    }
} 