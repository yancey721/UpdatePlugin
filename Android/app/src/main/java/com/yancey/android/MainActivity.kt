package com.yancey.android

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.callback.DownloadCallback
import com.yancey.sdk.callback.InstallCallback
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.LogLevel
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.DownloadProgress
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
        initSDK(true)
        
        // 设置版本信息显示
        setupVersionInfo()
        
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
    
    private fun initSDK(enableDefaultUI: Boolean) {
        val config = UpdateConfig.Builder(this)
            .setBaseUrl("http://192.168.210.22:8080/api/app/")  // 替换为你的服务器地址
            .setAppId(packageName)
            .enableDefaultUI(enableDefaultUI)  // 启用默认UI
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
    
    /**
     * 设置版本信息显示
     */
    private fun setupVersionInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            val versionText = "当前版本: $versionName ($versionCode)"
            findViewById<android.widget.TextView>(R.id.tvCurrentVersion)?.text = versionText
            Logger.d(TAG, "应用版本信息: $versionText")
            
        } catch (e: Exception) {
            Logger.e(TAG, "获取版本信息失败", e)
            findViewById<android.widget.TextView>(R.id.tvCurrentVersion)?.text = "当前版本: 获取失败"
        }
    }
    
    private fun setupTestButtons() {
        // 快速集成测试
        findViewById<Button>(R.id.btnCheckUpdate)?.setOnClickListener {
            checkUpdateWithDefaultUI()
        }
        
        // 自定义UI演示
        findViewById<Button>(R.id.btnCustomUpdate)?.setOnClickListener {
            checkUpdateWithCustomUI()
        }
        
        findViewById<Button>(R.id.btnMockOptionalUpdate)?.setOnClickListener {
            showMockOptionalUpdateDialog()
        }
        
        findViewById<Button>(R.id.btnMockForceUpdate)?.setOnClickListener {
            showMockForceUpdateDialog()
        }
        
        // 分步骤控制演示
        findViewById<Button>(R.id.btnTestDownload)?.setOnClickListener {
            testDownloadFeature()
        }
        
        findViewById<Button>(R.id.btnTestInstall)?.setOnClickListener {
            testInstallFeature()
        }
        
        findViewById<Button>(R.id.btnTestPermission)?.setOnClickListener {
            testInstallPermission()
        }
        
        // 工具功能
        findViewById<Button>(R.id.btnSdkInfo)?.setOnClickListener {
            showSDKInfo()
        }
        
        findViewById<Button>(R.id.btnClearCache)?.setOnClickListener {
            clearCache()
        }
    }
    
    /**
     * 使用默认UI检查更新（完整流程演示）
     */
    private fun checkUpdateWithDefaultUI() {
        showToast("正在检查更新（默认UI模式）...")
        Logger.d(TAG, "开始默认UI更新流程测试")
        
        AppUpdaterSDK.checkUpdate(object : UpdateCallback {
            override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
                if (updateInfo.hasUpdate) {
                    Logger.i(TAG, "发现新版本: ${updateInfo.newVersionName}")
                    showToast("发现新版本，SDK将自动显示更新对话框")
                    // SDK会自动显示对话框、下载、安装
                } else {
                    Logger.i(TAG, "已是最新版本")
                    showToast("当前已是最新版本")
                }
            }
            
            override fun onError(errorCode: Int, errorMessage: String) {
                Logger.e(TAG, "检查更新失败: $errorMessage")
                showToast("检查更新失败: $errorMessage", Toast.LENGTH_LONG)
            }
        })
    }
    
    /**
     * 使用自定义UI检查更新
     */
    private fun checkUpdateWithCustomUI() {
        // 临时切换到自定义UI模式（实际使用中应该在初始化时设置）
        initSDK(false)
        showToast("正在检查更新（自定义UI模式）...")
        Logger.d(TAG, "开始自定义UI更新流程测试")
        
        // 临时切换到自定义UI模式（实际使用中应该在初始化时设置）
        AppUpdaterSDK.checkUpdate(object : UpdateCallback {
            override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
                if (updateInfo.hasUpdate) {
                    // 显示自定义的更新对话框
                    showCustomUpdateDialog(updateInfo)
                } else {
                    showToast("当前已是最新版本")
                }
            }
            
            override fun onError(errorCode: Int, errorMessage: String) {
                showToast("检查更新失败: $errorMessage", Toast.LENGTH_LONG)
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
     * 显示自定义更新对话框
     */
    private fun showCustomUpdateDialog(updateInfo: UpdateInfo) {
        val message = """
            新版本: ${updateInfo.newVersionName} (${updateInfo.newVersionCode})
            文件大小: ${formatFileSize(updateInfo.fileSize)}
            强制更新: ${if (updateInfo.forceUpdate) "是" else "否"}
            
            更新说明:
            ${updateInfo.updateDescription}
        """.trimIndent()
        
        val builder = AlertDialog.Builder(this)
            .setTitle("发现新版本")
            .setMessage(message)
            .setPositiveButton("立即更新") { _, _ ->
                showToast("开始下载更新...")
                startCustomDownload(updateInfo)
            }
            .setCancelable(!updateInfo.forceUpdate)
        
        if (!updateInfo.forceUpdate) {
            builder.setNegativeButton("稍后提醒", null)
        }
        
        builder.show()
    }
    
    /**
     * 自定义下载流程
     */
    private fun startCustomDownload(updateInfo: UpdateInfo) {
        AppUpdaterSDK.startDownload(updateInfo, object : DownloadCallback {
            override fun onDownloadStart() {
                showToast("下载开始")
            }
            
            override fun onDownloadProgress(downloadProgress: DownloadProgress) {
                val progress = "${downloadProgress.progressPercent}% (${formatFileSize(downloadProgress.downloadedBytes)}/${formatFileSize(downloadProgress.totalBytes)})"
                Logger.d(TAG, "下载进度: $progress")
            }
            
            override fun onDownloadComplete(file: File) {
                showToast("下载完成，开始安装")
                AppUpdaterSDK.installApk(file, createInstallCallback())
            }
            
            override fun onDownloadError(errorCode: Int, errorMessage: String) {
                showToast("下载失败: $errorMessage", Toast.LENGTH_LONG)
            }
            
            override fun onDownloadCancel() {
                showToast("下载已取消")
            }
        })
    }
    
    /**
     * 显示模拟可选更新对话框
     */
    private fun showMockOptionalUpdateDialog() {
        val mockUpdateInfo = UpdateInfo(
            hasUpdate = true,
            newVersionCode = 2,
            newVersionName = "1.1.0",
            updateDescription = "1. 修复已知问题\n2. 优化用户体验\n3. 新增功能特性\n4. 提升应用性能",
            forceUpdate = false,
            downloadUrl = "https://example.com/app-v1.1.0.apk",
            fileSize = 25600000L,  // 25.6MB
            md5 = "d41d8cd98f00b204e9800998ecf8427e"
        )
        
        showMockUpdateDialog(mockUpdateInfo, "可选更新")
    }
    
    /**
     * 模拟显示强制更新对话框(默认UI)
     */
    private fun showMockForceUpdateDialog() {
        val mockUpdateInfo = UpdateInfo(
            hasUpdate = true,
            newVersionCode = 3,
            newVersionName = "2.0.0",
            updateDescription = "重大版本更新！\n\n1. 全新界面设计\n2. 核心功能重构\n3. 安全性升级\n4. 性能大幅提升",
            forceUpdate = true,  // 强制更新
            downloadUrl = "https://example.com/app-v2.0.0.apk",
            fileSize = 35800000L,  // 35.8MB
            md5 = "e58ed763928cf9b4eff36f1d13f3bcdb"
        )

        showMockUpdateDialog(mockUpdateInfo, "强制更新")
    }
    
    /**
     * 显示模拟更新对话框
     */
    private fun showMockUpdateDialog(updateInfo: UpdateInfo, type: String) {
        val config = AppUpdaterSDK.getConfig()
        if (config != null) {
            val dialog = UpdateDialog(this, config)
            dialog.showUpdateDialog(updateInfo, object : UICallback {
                override fun onUserConfirmUpdate(updateInfo: UpdateInfo) {
                    showToast("$type 演示：用户确认更新 -> ${updateInfo.newVersionName}")
                }
                
                override fun onUserCancelUpdate(updateInfo: UpdateInfo) {
                    showToast("$type 演示：用户取消更新")
                }
                
                override fun onDialogDismissed(updateInfo: UpdateInfo) {
                    showToast("$type 演示：对话框被关闭")
                }
            })
        }
    }
    
    /**
     * 测试下载功能
     */
    private fun testDownloadFeature() {
        val mockUpdateInfo = UpdateInfo(
            hasUpdate = true,
            newVersionCode = 999,
            newVersionName = "测试版本",
            updateDescription = "这是一个测试下载功能的模拟版本",
            forceUpdate = false,
            downloadUrl = "https://httpbin.org/bytes/1048576", // 1MB测试文件
            fileSize = 1048576L,
            md5 = "test"
        )
        
        showToast("开始测试下载功能...")
        startCustomDownload(mockUpdateInfo)
    }
    
    /**
     * 测试安装功能
     */
    private fun testInstallFeature() {
        // 创建一个测试APK文件路径
        val testApkFile = File(getExternalFilesDir(null), "test_install.apk")
        
        showToast("测试安装功能（模拟APK文件）")
        AppUpdaterSDK.installApk(testApkFile, createInstallCallback())
    }
    
    /**
     * 显示SDK信息
     */
    private fun showSDKInfo() {
        val config = AppUpdaterSDK.getConfig()
        val permissionSummary = if (AppUpdaterSDK.checkInstallPermission()) "已授权" else "未授权"
        
        val info = """
            📱 SDK信息
            
            SDK版本: ${AppUpdaterSDK.getVersionInfo()}
            初始化状态: ${if (AppUpdaterSDK.isInitialized()) "已初始化" else "未初始化"}
            
            📋 配置信息
            应用包名: ${packageName}
            服务器地址: ${config?.baseUrl ?: "未配置"}
            默认UI: ${config?.enableDefaultUI ?: false}
            日志状态: ${config?.enableLog ?: false}
            
            🔐 权限状态
            安装权限: $permissionSummary
            Android版本: ${android.os.Build.VERSION.SDK_INT}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("SDK信息")
            .setMessage(info)
            .setPositiveButton("确定", null)
            .show()
    }
    
    /**
     * 清除缓存
     */
    private fun clearCache() {
        try {
            // 清除下载目录
            val downloadDir = File(getExternalFilesDir(null), "Download")
            if (downloadDir.exists()) {
                downloadDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk")) {
                        file.delete()
                        Logger.d(TAG, "删除缓存文件: ${file.name}")
                    }
                }
            }
            
            showToast("缓存清除完成")
            Logger.d(TAG, "缓存清除完成")
            
        } catch (e: Exception) {
            Logger.e(TAG, "清除缓存失败", e)
            showToast("清除缓存失败: ${e.message}")
        }
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