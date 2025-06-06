package com.yancey.android

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.config.LogLevel
import com.yancey.sdk.config.UpdateConfig
import com.yancey.sdk.data.UpdateInfo

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
            .enableDefaultUI(true)
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
                        发现新版本！
                        
                        新版本: ${updateInfo.newVersionName} (${updateInfo.newVersionCode})
                        更新说明: ${updateInfo.updateDescription}
                        文件大小: ${formatFileSize(updateInfo.fileSize)}
                        强制更新: ${if (updateInfo.forceUpdate) "是" else "否"}
                    """.trimIndent()
                    
                    showToast(message, Toast.LENGTH_LONG)
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
    
    private fun showSDKInfo() {
        val config = AppUpdaterSDK.getConfig()
        val info = """
            SDK版本: ${AppUpdaterSDK.getVersionInfo()}
            初始化状态: ${AppUpdaterSDK.isInitialized()}
            应用包名: ${packageName}
            服务器地址: ${config?.baseUrl ?: "未配置"}
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