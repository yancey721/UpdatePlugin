package com.yancey.android

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.callback.UICallback
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.LogLevel
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.UpdateInfo
import com.yancey.sdk.ui.UpdateDialog

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化SDK
        initSDK()
        
        // 设置测试按钮
        setupTestButtons()
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
        
        findViewById<Button>(R.id.btnTestOptionalUpdate)?.setOnClickListener {
            testOptionalUpdateUI()
        }
        
        findViewById<Button>(R.id.btnTestForceUpdate)?.setOnClickListener {
            testForceUpdateUI()
        }
        
        findViewById<Button>(R.id.btnSdkInfo)?.setOnClickListener {
            showSDKInfo()
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
     * 测试可选更新UI对话框
     */
    private fun testOptionalUpdateUI() {
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
        
        showMockUpdateDialog(mockUpdateInfo)
    }
    
    /**
     * 测试强制更新UI对话框
     */
    private fun testForceUpdateUI() {
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
        
        showMockUpdateDialog(mockUpdateInfo)
    }
    
    /**
     * 显示模拟更新对话框
     */
    private fun showMockUpdateDialog(updateInfo: UpdateInfo) {
        val config = AppUpdaterSDK.getConfig()
        if (config != null) {
            val dialog = UpdateDialog(this, config)
            dialog.showUpdateDialog(updateInfo, object : UICallback {
                override fun onUserConfirmUpdate(updateInfo: UpdateInfo) {
                    showToast("用户确认更新 -> 开始下载 ${updateInfo.newVersionName}")
                }
                
                override fun onUserCancelUpdate(updateInfo: UpdateInfo) {
                    showToast("用户取消更新 -> 稍后提醒")
                }
                
                override fun onDialogDismissed(updateInfo: UpdateInfo) {
                    showToast("对话框被关闭")
                }
            })
        }
    }
    
    private fun showSDKInfo() {
        val config = AppUpdaterSDK.getConfig()
        val info = """
            SDK版本: ${AppUpdaterSDK.getVersionInfo()}
            初始化状态: ${AppUpdaterSDK.isInitialized()}
            应用包名: ${packageName}
            服务器地址: ${config?.baseUrl ?: "未配置"}
            默认UI: ${config?.enableDefaultUI ?: false}
            日志状态: ${config?.enableLog ?: false}
        """.trimIndent()
        
        showToast(info, Toast.LENGTH_LONG)
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