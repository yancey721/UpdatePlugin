package com.yancey.sdk.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.yancey.sdk.R
import com.yancey.sdk.data.DownloadProgress
import com.yancey.sdk.data.DownloadStatus
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.util.DeviceInfoHelper
import com.yancey.sdk.util.Logger

/**
 * 下载进度对话框
 * 显示APK下载进度，支持取消下载
 */
class DownloadProgressDialog(
    private val context: Context,
    private val updateInfo: UpdateInfo,
    private val onCancelDownload: () -> Unit
) {
    
    private var dialog: Dialog? = null
    private lateinit var tvAppNameVersion: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDownloadedSize: TextView
    private lateinit var tvDownloadSpeed: TextView
    private lateinit var btnCancelDownload: Button
    
    fun show() {
        if (!isValidContextForDialog()) {
            Logger.e("DownloadProgressDialog", "Invalid context for dialog")
            return
        }
        
        try {
            Logger.d("DownloadProgressDialog", "显示下载进度对话框")
            
            dialog = Dialog(context, R.style.CustomDialogTheme).apply {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_download_progress, null)
                setContentView(view)
                
                // 设置对话框属性
                setCancelable(false)  // 下载进度对话框不可点击外部取消
                setCanceledOnTouchOutside(false)
                
                // 设置窗口属性
                window?.let { window ->
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                    val layoutParams = window.attributes
                    layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
                    window.attributes = layoutParams
                }
                
                // 初始化视图
                initViews(view)
            }
            
            dialog?.show()
            
        } catch (e: Exception) {
            Logger.e("DownloadProgressDialog", "显示下载进度对话框失败", e)
        }
    }
    
    private fun initViews(view: View) {
        // 获取视图组件
        tvAppNameVersion = view.findViewById(R.id.tvAppNameVersion)
        tvFileSize = view.findViewById(R.id.tvFileSize)
        tvProgress = view.findViewById(R.id.tvProgress)
        progressBar = view.findViewById(R.id.progressBar)
        tvDownloadedSize = view.findViewById(R.id.tvDownloadedSize)
        tvDownloadSpeed = view.findViewById(R.id.tvDownloadSpeed)
        btnCancelDownload = view.findViewById(R.id.btnCancelDownload)
        
        // 设置应用信息
        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
        tvAppNameVersion.text = "$appName v${updateInfo.newVersionName}"
        tvFileSize.text = formatFileSize(updateInfo.fileSize)
        
        // 初始化进度信息
        updateProgress(DownloadProgress(
            downloadedBytes = 0,
            totalBytes = updateInfo.fileSize,
            progressPercent = 0,
            downloadSpeed = 0,
            status = DownloadStatus.PENDING
        ))
        
        // 设置取消按钮点击事件
        btnCancelDownload.setOnClickListener {
            Logger.d("DownloadProgressDialog", "用户点击取消下载")
            dismiss()
            onCancelDownload()
        }
    }
    
    /**
     * 更新下载进度
     */
    fun updateProgress(progress: DownloadProgress) {
        if (dialog?.isShowing != true) return
        
        try {
            // 更新进度百分比
            tvProgress.text = "${progress.progressPercent}%"
            progressBar.progress = progress.progressPercent
            
            // 更新下载大小信息
            val downloadedSize = formatFileSize(progress.downloadedBytes)
            val totalSize = formatFileSize(progress.totalBytes)
            tvDownloadedSize.text = "$downloadedSize / $totalSize"
            
            // 更新下载速度
            tvDownloadSpeed.text = if (progress.downloadSpeed > 0) {
                "${formatFileSize(progress.downloadSpeed)}/s"
            } else {
                "计算中..."
            }
            
            Logger.d("DownloadProgressDialog", "更新进度: ${progress.progressPercent}%, 速度: ${progress.downloadSpeed} bytes/s")
            
        } catch (e: Exception) {
            Logger.e("DownloadProgressDialog", "更新进度失败", e)
        }
    }
    
    /**
     * 下载完成，显示100%进度并更改按钮为立即安装
     */
    fun onDownloadComplete(onInstallClick: () -> Unit) {
        Logger.d("DownloadProgressDialog", "下载完成，更新UI为安装状态")
        
        if (dialog?.isShowing != true) return
        
        try {
            // 确保进度显示100%
            tvProgress.text = "100%"
            progressBar.progress = 100
            
            // 更新下载速度显示
            tvDownloadSpeed.text = "下载完成"
            
            // 修改按钮文字、背景和行为
            btnCancelDownload.text = "立即安装"
            btnCancelDownload.setBackgroundResource(R.drawable.button_install_background)
            btnCancelDownload.setTextColor(context.getColor(android.R.color.white))
            btnCancelDownload.setOnClickListener {
                Logger.d("DownloadProgressDialog", "用户点击立即安装")
                dismiss()
                onInstallClick()
            }
            
            Logger.d("DownloadProgressDialog", "下载完成UI更新完毕")
            
        } catch (e: Exception) {
            Logger.e("DownloadProgressDialog", "更新下载完成UI失败", e)
        }
    }
    
    /**
     * 下载失败，关闭对话框
     */
    fun onDownloadError() {
        Logger.d("DownloadProgressDialog", "下载失败，关闭对话框")
        dismiss()
    }
    
    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "${sizeInBytes} B"
            sizeInBytes < 1024 * 1024 -> "${String.format("%.1f", sizeInBytes / 1024.0)} KB"
            sizeInBytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", sizeInBytes / (1024.0 * 1024.0))} MB"
            else -> "${String.format("%.1f", sizeInBytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
    
    private fun isValidContextForDialog(): Boolean {
        return when {
            context !is Activity -> {
                Logger.e("DownloadProgressDialog", "Context is not an Activity")
                false
            }
            context.isFinishing -> {
                Logger.e("DownloadProgressDialog", "Activity is finishing")
                false
            }
            context.isDestroyed -> {
                Logger.e("DownloadProgressDialog", "Activity is destroyed")
                false
            }
            else -> true
        }
    }
    
    fun dismiss() {
        try {
            dialog?.dismiss()
            dialog = null
            Logger.d("DownloadProgressDialog", "下载进度对话框已关闭")
        } catch (e: Exception) {
            Logger.e("DownloadProgressDialog", "关闭下载进度对话框失败", e)
        }
    }
    
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
} 