package com.yancey.android

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.callback.InstallCallback
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.LogLevel
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.ui.UpdateDialog
import com.yancey.sdk.util.Logger
import java.io.File

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // 待安装的APK文件（从设置页返回时使用）
    private var pendingInstallApk: File? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化SDK
        initSDK()
        
        // 设置测试按钮
        setupTestButtons()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 检查SDK内部是否有待安装的任务（用户从设置页返回）
        AppUpdaterSDK.checkAndHandlePendingInstall()
        
        // 检查是否有自定义的待安装任务（用户从设置页返回）
        pendingInstallApk?.let { file ->
            Logger.d(TAG, "检查自定义待安装任务的权限状态")
            
            // 检查权限是否已获得
            if (AppUpdaterSDK.checkInstallPermission()) {
                Logger.i(TAG, "权限已获得，继续安装")
                val apkToInstall = file
                pendingInstallApk = null // 清除待安装任务
                
                // 重新触发安装
                AppUpdaterSDK.installApk(apkToInstall, createInstallCallback())
            } else {
                Logger.w(TAG, "用户未授予权限")
                pendingInstallApk = null // 清除待安装任务
                Toast.makeText(this, "权限未开启，无法安装", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun initSDK() {
        val config = UpdateConfig.Builder(this)
            .setBaseUrl("http://192.168.210.22:8080/api/app/")  // 替换为你的服务器地址
            .setAppId(packageName)
            .enableDefaultUI(true)  // 启用默认UI
            .showNotification(true)
            .autoInstall(true)
            .enableLog(true)
            .setLogLevel(LogLevel.DEBUG)
            .setConnectTimeout(10000)
            .setReadTimeout(30000)
            .build()
        
        AppUpdaterSDK.init(this, config)
        
        // 显示SDK信息
        showToast("SDK初始化完成: ${AppUpdaterSDK.getVersionInfo()}")
    }
    
    private fun setupTestButtons() {
        findViewById<Button>(R.id.btnCheckUpdate)?.setOnClickListener {
            checkUpdate()
        }
        
        findViewById<Button>(R.id.btnTestPermission)?.setOnClickListener {
            testInstallPermission()
        }
    }
    
    private fun checkUpdate() {
        showToast("正在检查更新...")
        
        AppUpdaterSDK.checkUpdate(object : UpdateCallback {
            override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
                if (updateInfo.hasUpdate) {
                    val message = """
                        发现新版本！（将自动显示更新对话框）
                        
                        新版本: ${updateInfo.newVersionName} (${updateInfo.newVersionCode})
                        更新说明: ${updateInfo.updateDescription}
                        文件大小: ${formatFileSize(updateInfo.fileSize)}
                        强制更新: ${if (updateInfo.forceUpdate) "是" else "否"}
                    """.trimIndent()
                    
                    showToast(message, Toast.LENGTH_LONG)
                    // 注意：如果启用了默认UI，UpdateManager会自动显示对话框
                } else {
                    showToast("当前已是最新版本")
                }
            }
            
            override fun onError(errorCode: Int, errorMessage: String) {
                val message = "检查更新失败\n错误码: $errorCode\n错误信息: $errorMessage"
                showToast(message, Toast.LENGTH_LONG)
                }
        })
    }
    
    /**
     * 测试安装权限功能
     */
    private fun testInstallPermission() {
        val hasPermission = AppUpdaterSDK.checkInstallPermission()
        Logger.d(TAG, "当前安装权限状态: $hasPermission")
        
        if (hasPermission) {
            showToast("已有安装权限")
        } else {
            showToast("缺少安装权限，将测试权限请求流程")
            
            // 模拟安装一个测试文件来触发权限请求
            val testFile = File(getExternalFilesDir(null), "test.apk")
            AppUpdaterSDK.installApk(testFile, createInstallCallback())
        }
    }
    
    /**
     * 创建安装回调
     */
    private fun createInstallCallback(): InstallCallback {
        return object : InstallCallback {
            override fun onInstallStart(apkFile: File) {
                Logger.i(TAG, "安装启动成功: ${apkFile.name}")
                showToast("正在启动安装程序...")
                
                // 清除待安装任务
                pendingInstallApk = null
            }
            
            override fun onInstallPermissionRequired(onUserConfirm: () -> Unit, onUserCancel: () -> Unit) {
                Logger.w(TAG, "需要安装权限，显示权限说明对话框")
                
                // 显示权限说明对话框
                showPermissionExplanationDialog(
                    onConfirm = {
                        Logger.d(TAG, "用户确认前往设置页面")
                        onUserConfirm() // 调用SDK提供的确认回调
                    },
                    onCancel = {
                        Logger.d(TAG, "用户取消权限设置")
                        onUserCancel() // 调用SDK提供的取消回调
                    }
                )
            }
            
            override fun onInstallError(errorCode: Int, errorMessage: String) {
                Logger.e(TAG, "安装失败: $errorMessage (code: $errorCode)")
                
                when (errorCode) {
                    -1 -> showToast("APK文件不存在", Toast.LENGTH_LONG)
                    -2 -> showToast("无法打开权限设置页面", Toast.LENGTH_LONG)
                    -3 -> showToast("权限未开启，无法安装", Toast.LENGTH_LONG)
                    else -> showToast(errorMessage, Toast.LENGTH_LONG)
                }
                
                // 清除待安装任务
                pendingInstallApk = null
            }
        }
    }
    
    /**
     * 显示权限说明对话框
     */
    private fun showPermissionExplanationDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("需要安装权限")
            .setMessage("为了自动安装更新版本，需要开启\"安装未知应用\"权限。\n\n点击\"去设置\"将跳转到系统设置页面，请找到本应用并开启该权限。")
            .setPositiveButton("去设置") { _, _ ->
                // 记录当前尝试安装的文件，用于从设置页返回时继续安装
                setPendingInstallFromCurrentContext()
                onConfirm()
            }
            .setNegativeButton("取消") { _, _ ->
                onCancel()
            }
            .setCancelable(false) // 防止用户点击对话框外部关闭
            .show()
    }
    
    /**
     * 设置待安装的APK文件
     */
    private fun setPendingInstallFromCurrentContext() {
        // 在真实使用中，这里应该是实际下载完成的APK文件
        // 为了演示，我们创建一个测试文件路径
        pendingInstallApk = File(getExternalFilesDir(null), "downloaded_update.apk")
        Logger.d(TAG, "设置待安装APK: ${pendingInstallApk?.absolutePath}")
    }
    
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
    
    /**
     * 格式化文件大小显示
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}