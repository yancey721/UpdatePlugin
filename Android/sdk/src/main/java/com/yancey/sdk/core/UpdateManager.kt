package com.yancey.sdk.core

import android.content.Context
import com.yancey.sdk.callback.DownloadCallback
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.CheckUpdateRequest
import com.yancey.sdk.data.DownloadProgress
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.data.toUpdateInfo
import com.yancey.sdk.download.ApkDownloadManager
import com.yancey.sdk.network.NetworkClient
import com.yancey.sdk.network.NetworkException
import com.yancey.sdk.ui.DownloadProgressDialog
import com.yancey.sdk.ui.UpdateDialog
import com.yancey.sdk.util.DeviceInfoHelper
import com.yancey.sdk.util.Logger
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 更新管理器核心类
 * 负责协调各个模块的工作流程
 */
class UpdateManager(
    private val context: Context,
    private val config: UpdateConfig
) {
    
    // 协程作用域
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 网络客户端
    private val networkClient = NetworkClient(config)
    
    // UI对话框
    private val updateDialog = if (config.enableDefaultUI) UpdateDialog(context, config) else null
    
    // 下载管理器
    private val downloadManager = ApkDownloadManager(context)
    
    // 下载进度对话框
    private var downloadProgressDialog: DownloadProgressDialog? = null
    
    // 当前版本信息
    private val currentVersionCode = DeviceInfoHelper.getCurrentVersionCode(context)
    private val currentVersionName = DeviceInfoHelper.getCurrentVersionName(context)
    
    init {
        val deviceInfo = DeviceInfoHelper.getDeviceInfoSummary()
        Logger.i("UpdateManager", "UpdateManager initialized")
        Logger.i("UpdateManager", "Current version: $currentVersionName ($currentVersionCode)")
        Logger.i("UpdateManager", "Device: $deviceInfo")
        Logger.i("UpdateManager", "Server URL: ${config.baseUrl}")
        Logger.i("UpdateManager", "Default UI enabled: ${config.enableDefaultUI}")
    }
    
    /**
     * 检查应用更新
     * @param callback 更新检查回调
     */
    fun checkUpdate(callback: UpdateCallback) {
        Logger.d("UpdateManager", "Starting update check...")
        
        // 在协程中执行网络请求
        managerScope.launch {
            try {
                // 构建请求参数
                val request = CheckUpdateRequest(
                    appId = config.appId,
                    currentVersionCode = currentVersionCode,
                    channel = "default",
                    deviceInfo = if (config.enableLog) DeviceInfoHelper.getDeviceInfo() else null
                )
                
                Logger.d("UpdateManager", "Request params: appId=${request.appId}, versionCode=${request.currentVersionCode}")
                
                // 发起网络请求
                val response = networkClient.checkUpdate(request)
                
                // 处理响应
                handleUpdateResponse(response, callback)
                
            } catch (e: NetworkException) {
                // 网络异常
                Logger.e("UpdateManager", "Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onError(e.errorCode, e.message ?: "网络请求失败")
                }
                
            } catch (e: Exception) {
                // 其他异常
                Logger.e("UpdateManager", "Unexpected error during update check", e)
                withContext(Dispatchers.Main) {
                    callback.onError(-1, "检查更新时发生未知错误")
                }
            }
        }
    }
    
    /**
     * 处理更新响应
     */
    private suspend fun handleUpdateResponse(
        response: com.yancey.sdk.data.CheckUpdateResponse,
        callback: UpdateCallback
    ) {
        withContext(Dispatchers.Main) {
            try {
                Logger.d("UpdateManager", "Processing response: code=${response.code}, message=${response.message}")
                
                when (response.code) {
                    200 -> {
                        // 成功响应
                        val responseData = response.data
                        if (responseData != null) {
                            val updateInfo = responseData.toUpdateInfo()
                            
                            if (updateInfo.hasUpdate) {
                                Logger.i("UpdateManager", "Update available: ${updateInfo.newVersionName} (${updateInfo.newVersionCode})")
                                Logger.i("UpdateManager", "Force update: ${updateInfo.forceUpdate}")
                                Logger.i("UpdateManager", "File size: ${formatFileSize(updateInfo.fileSize)}")
                                
                                // 先调用回调通知有更新
                                callback.onUpdateCheckSuccess(updateInfo)
                                
                                // 如果启用了默认UI，显示更新对话框
                                if (config.enableDefaultUI && updateDialog != null) {
                                    showDefaultUpdateDialog(updateInfo)
                                }
                            } else {
                                Logger.i("UpdateManager", "No update available")
                                callback.onUpdateCheckSuccess(updateInfo)
                            }
                        } else {
                            Logger.w("UpdateManager", "Response data is null")
                            callback.onError(-1, "服务端响应数据为空")
                        }
                    }
                    
                    else -> {
                        // 业务错误（透传服务端错误码和消息）
                        Logger.w("UpdateManager", "Business error: code=${response.code}, message=${response.message}")
                        callback.onError(response.code, response.message)
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("UpdateManager", "Error processing response", e)
                callback.onError(-1, "处理服务端响应时发生错误")
            }
        }
    }
    
    /**
     * 显示默认更新对话框
     */
    private fun showDefaultUpdateDialog(updateInfo: UpdateInfo) {
        Logger.d("UpdateManager", "Showing default update dialog")
        
        updateDialog?.showUpdateDialog(updateInfo, object : UICallback {
            override fun onUserConfirmUpdate(updateInfo: UpdateInfo) {
                Logger.i("UpdateManager", "User confirmed update, starting download...")
                // TODO: 在阶段4中实现下载功能
                startDownload(updateInfo, null)
            }
            
            override fun onUserCancelUpdate(updateInfo: UpdateInfo) {
                Logger.i("UpdateManager", "User cancelled update")
                // 用户选择稍后提醒，暂时不做特殊处理
            }
            
            override fun onDialogDismissed(updateInfo: UpdateInfo) {
                Logger.i("UpdateManager", "Update dialog dismissed")
                // 对话框被系统关闭，暂时不做特殊处理
            }
        })
    }
    
    /**
     * 开始下载更新包
     * @param updateInfo 更新信息
     * @param downloadCallback 下载回调
     */
    fun startDownload(updateInfo: UpdateInfo, downloadCallback: DownloadCallback?) {
        Logger.d("UpdateManager", "开始下载APK文件")
        Logger.d("UpdateManager", "Download URL: ${updateInfo.downloadUrl}")
        Logger.d("UpdateManager", "File size: ${formatFileSize(updateInfo.fileSize)}")
        
        // 创建下载回调，结合用户回调和内部逻辑
        val internalDownloadCallback = object : DownloadCallback {
            override fun onDownloadStart() {
                Logger.d("UpdateManager", "下载开始")
                
                // 如果启用默认UI，显示下载进度对话框
                if (config.enableDefaultUI) {
                    showDownloadProgressDialog(updateInfo)
                }
                
                // 调用用户回调
                downloadCallback?.onDownloadStart()
            }
            
            override fun onDownloadProgress(downloadProgress: DownloadProgress) {
                // 更新下载进度对话框
                downloadProgressDialog?.updateProgress(downloadProgress)
                
                // 调用用户回调
                downloadCallback?.onDownloadProgress(downloadProgress)
            }
            
            override fun onDownloadComplete(file: File) {
                Logger.d("UpdateManager", "下载完成: ${file.absolutePath}")
                
                // 更新下载进度对话框为安装状态
                downloadProgressDialog?.onDownloadComplete {
                    // 用户点击立即安装的回调
                    Logger.i("UpdateManager", "用户点击立即安装")
                    // TODO: 在阶段5中实现APK安装功能
                    Logger.i("UpdateManager", "APK安装功能将在阶段5中实现")
                    
                    // 临时关闭对话框
                    downloadProgressDialog?.dismiss()
                    downloadProgressDialog = null
                }
                
                // 调用用户回调
                downloadCallback?.onDownloadComplete(file)
            }
            
            override fun onDownloadError(errorCode: Int, errorMessage: String) {
                Logger.e("UpdateManager", "下载失败: $errorMessage")
                
                // 关闭下载进度对话框
                downloadProgressDialog?.onDownloadError()
                downloadProgressDialog = null
                
                // 调用用户回调
                downloadCallback?.onDownloadError(errorCode, errorMessage)
            }
            
            override fun onDownloadCancel() {
                Logger.d("UpdateManager", "下载被取消")
                
                // 关闭下载进度对话框
                downloadProgressDialog?.dismiss()
                downloadProgressDialog = null
                
                // 调用用户回调
                downloadCallback?.onDownloadCancel()
            }
        }
        
        // 启动下载
        val success = downloadManager.downloadApk(updateInfo, internalDownloadCallback)
        if (!success) {
            Logger.e("UpdateManager", "启动下载失败")
            downloadCallback?.onDownloadError(-1, "启动下载失败")
        }
    }
    
    /**
     * 显示下载进度对话框
     */
    private fun showDownloadProgressDialog(updateInfo: UpdateInfo) {
        // 关闭之前的下载进度对话框
        downloadProgressDialog?.dismiss()
        
        // 创建新的下载进度对话框
        downloadProgressDialog = DownloadProgressDialog(
            context = context,
            updateInfo = updateInfo,
            onCancelDownload = {
                Logger.d("UpdateManager", "用户请求取消下载")
                cancelDownload()
            }
        )
        
        // 显示对话框
        downloadProgressDialog?.show()
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload() {
        Logger.d("UpdateManager", "取消下载")
        downloadManager.cancelDownload()
        
        // 关闭下载进度对话框
        downloadProgressDialog?.dismiss()
        downloadProgressDialog = null
    }
    
    /**
     * 释放资源
     */
    fun release() {
        Logger.d("UpdateManager", "UpdateManager resources released")
        updateDialog?.dismissCurrentDialog()
        downloadProgressDialog?.dismiss()
        downloadProgressDialog = null
        // 注意：不调用cancelDownload()，避免中断正在进行的下载
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
} 