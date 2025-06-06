package com.yancey.sdk.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.yancey.sdk.R
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.util.DeviceInfoHelper
import com.yancey.sdk.util.Logger

/**
 * 自定义更新对话框
 * 使用Material Design风格的UI
 */
class UpdateDialog(
    private val context: Context,
    private val config: UpdateConfig
) {
    
    private var currentDialog: Dialog? = null
    
    /**
     * 显示更新对话框
     * @param updateInfo 更新信息
     * @param callback UI操作回调
     */
    fun showUpdateDialog(updateInfo: UpdateInfo, callback: UICallback) {
        // 检查Context类型和Activity状态
        if (!isValidContextForDialog()) {
            Logger.e("UpdateDialog", "Invalid context for dialog: ${context.javaClass.simpleName}")
            return
        }
        
        // 关闭之前的对话框
        dismissCurrentDialog()
        
        try {
            Logger.d("UpdateDialog", "显示更新对话框 - 强制更新: ${updateInfo.forceUpdate}")
            
            // 创建自定义对话框
            currentDialog = Dialog(context, R.style.CustomDialogTheme).apply {
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null)
                setContentView(view)
                
                // 设置对话框属性
                setCancelable(!updateInfo.forceUpdate)
                setCanceledOnTouchOutside(!updateInfo.forceUpdate)
                
                // 设置窗口属性
                window?.let { window ->
                    window.setBackgroundDrawableResource(android.R.color.transparent)
                    val layoutParams = window.attributes
                    layoutParams.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
                    window.attributes = layoutParams
                }
                
                // 初始化视图
                initViews(view, updateInfo, callback)
                
                // 设置取消监听
                if (!updateInfo.forceUpdate) {
                    setOnCancelListener {
                        Logger.d("UpdateDialog", "Dialog cancelled by system")
                        callback.onDialogDismissed(updateInfo)
                    }
                }
            }
            
            currentDialog?.show()
            
        } catch (e: Exception) {
            Logger.e("UpdateDialog", "显示对话框失败", e)
        }
    }
    
    /**
     * 检查Context是否适合显示对话框
     */
    private fun isValidContextForDialog(): Boolean {
        return when {
            context !is Activity -> {
                Logger.e("UpdateDialog", "Context is not an Activity: ${context.javaClass.simpleName}")
                false
            }
            context.isFinishing -> {
                Logger.e("UpdateDialog", "Activity is finishing")
                false
            }
            context.isDestroyed -> {
                Logger.e("UpdateDialog", "Activity is destroyed")
                false
            }
            else -> true
        }
    }
    
    private fun initViews(view: View, updateInfo: UpdateInfo, callback: UICallback) {
        // 获取视图组件
        val tvCurrentVersion = view.findViewById<TextView>(R.id.tvCurrentVersion)
        val tvNewVersion = view.findViewById<TextView>(R.id.tvNewVersion)
        val tvFileSize = view.findViewById<TextView>(R.id.tvFileSize)
        val tvUpdateDescription = view.findViewById<TextView>(R.id.tvUpdateDescription)
        val tvForceUpdateWarning = view.findViewById<TextView>(R.id.tvForceUpdateWarning)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)
        
        // 设置版本信息
        tvCurrentVersion.text = DeviceInfoHelper.getCurrentVersionName(context)
        tvNewVersion.text = updateInfo.newVersionName
        tvFileSize.text = formatFileSize(updateInfo.fileSize)
        
        // 设置更新描述
        if (updateInfo.updateDescription.isNotEmpty()) {
            tvUpdateDescription.text = formatUpdateDescription(updateInfo.updateDescription)
        }
        
        // 处理强制更新
        if (updateInfo.forceUpdate) {
            tvForceUpdateWarning.visibility = View.VISIBLE
            btnCancel.visibility = View.GONE
        } else {
            tvForceUpdateWarning.visibility = View.GONE
            btnCancel.visibility = View.VISIBLE
        }
        
        // 设置按钮点击事件
        btnCancel.setOnClickListener {
            Logger.d("UpdateDialog", "用户点击稍后提醒")
            dismissCurrentDialog()
            callback.onUserCancelUpdate(updateInfo)
        }
        
        btnUpdate.setOnClickListener {
            Logger.d("UpdateDialog", "用户点击立即更新")
            dismissCurrentDialog()
            callback.onUserConfirmUpdate(updateInfo)
        }
    }
    
    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "${sizeInBytes} B"
            sizeInBytes < 1024 * 1024 -> "${String.format("%.1f", sizeInBytes / 1024.0)} KB"
            sizeInBytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", sizeInBytes / (1024.0 * 1024.0))} MB"
            else -> "${String.format("%.1f", sizeInBytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
    
    private fun formatUpdateDescription(description: String): String {
        // 如果描述已经包含项目符号，直接返回
        if (description.contains("•") || description.contains("-") || description.contains("*")) {
            return description
        }
        
        // 否则将每行前面添加项目符号
        return description.split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { "• ${it.trim()}" }
    }
    
    /**
     * 关闭当前对话框
     */
    fun dismissCurrentDialog() {
        currentDialog?.let { dialog ->
            if (dialog.isShowing) {
                try {
                    dialog.dismiss()
                    Logger.d("UpdateDialog", "对话框已关闭")
                } catch (e: Exception) {
                    Logger.w("UpdateDialog", "关闭对话框失败: ${e.message}")
                }
            }
            currentDialog = null
        }
    }
    
    /**
     * 检查对话框是否正在显示
     */
    fun isShowing(): Boolean {
        return currentDialog?.isShowing == true
    }
} 