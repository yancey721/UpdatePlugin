package com.yancey.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.util.Logger

/**
 * 默认更新对话框
 * 使用Material Design风格的AlertDialog
 */
class UpdateDialog(
    private val context: Context,
    private val config: UpdateConfig
) {
    
    private var currentDialog: AlertDialog? = null
    
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
            Logger.d("UpdateDialog", "Showing update dialog for version ${updateInfo.newVersionName}")
            
            val dialog = createUpdateDialog(updateInfo, callback)
            currentDialog = dialog
            dialog.show()
            
        } catch (e: Exception) {
            Logger.e("UpdateDialog", "Failed to show update dialog: ${e.message}")
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
    
    /**
     * 创建更新对话框
     */
    private fun createUpdateDialog(updateInfo: UpdateInfo, callback: UICallback): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(context, getDialogTheme())
        
        // 设置标题
        val title = "发现新版本 ${updateInfo.newVersionName}"
        dialogBuilder.setTitle(title)
        
        // 设置内容
        val message = buildDialogMessage(updateInfo)
        dialogBuilder.setMessage(message)
        
        // 设置正面按钮（立即更新）
        dialogBuilder.setPositiveButton("立即更新") { dialog, _ ->
            Logger.d("UpdateDialog", "User confirmed update")
            dialog.dismiss()
            callback.onUserConfirmUpdate(updateInfo)
        }
        
        // 设置负面按钮和取消行为
        if (updateInfo.forceUpdate) {
            // 强制更新：不可取消，没有负面按钮
            dialogBuilder.setCancelable(false)
            Logger.d("UpdateDialog", "Force update mode - dialog not cancelable")
        } else {
            // 可选更新：可以取消，有稍后提醒按钮
            dialogBuilder.setCancelable(true)
            dialogBuilder.setNegativeButton("稍后提醒") { dialog, _ ->
                Logger.d("UpdateDialog", "User cancelled update")
                dialog.dismiss()
                callback.onUserCancelUpdate(updateInfo)
            }
            
            // 设置对话框关闭监听
            dialogBuilder.setOnCancelListener {
                Logger.d("UpdateDialog", "Dialog cancelled by system")
                callback.onDialogDismissed(updateInfo)
            }
        }
        
        return dialogBuilder.create()
    }
    
    /**
     * 构建对话框消息内容
     */
    private fun buildDialogMessage(updateInfo: UpdateInfo): String {
        val stringBuilder = StringBuilder()
        
        // 更新说明
        if (updateInfo.updateDescription.isNotEmpty()) {
            stringBuilder.append(updateInfo.updateDescription)
            stringBuilder.append("\n\n")
        }
        
        // 版本信息
        stringBuilder.append("新版本：${updateInfo.newVersionName} (${updateInfo.newVersionCode})")
        stringBuilder.append("\n")
        
        // 文件大小
        val fileSize = formatFileSize(updateInfo.fileSize)
        stringBuilder.append("文件大小：$fileSize")
        
        // 强制更新提示
        if (updateInfo.forceUpdate) {
            stringBuilder.append("\n\n")
            stringBuilder.append("⚠️ 此为强制更新，必须更新后才能继续使用")
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * 获取对话框主题
     */
    private fun getDialogTheme(): Int {
        return config.dialogTheme ?: androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes > 0 -> "$bytes B"
            else -> "未知"
        }
    }
    
    /**
     * 关闭当前对话框
     */
    fun dismissCurrentDialog() {
        currentDialog?.let { dialog ->
            if (dialog.isShowing) {
                try {
                    dialog.dismiss()
                    Logger.d("UpdateDialog", "Current dialog dismissed")
                } catch (e: Exception) {
                    Logger.w("UpdateDialog", "Error dismissing dialog: ${e.message}")
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