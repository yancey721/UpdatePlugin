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
            .setBaseUrl("https://your-server.com/api/")
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
        Toast.makeText(this, AppUpdaterSDK.getVersionInfo(), Toast.LENGTH_SHORT).show()
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
        AppUpdaterSDK.checkUpdate(object : UpdateCallback {
            override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
                // 目前这个回调不会被调用，因为UpdateManager中还是占位符实现
                Toast.makeText(this@MainActivity, "检查更新成功", Toast.LENGTH_SHORT).show()
            }
            
            override fun onError(errorCode: Int, errorMessage: String) {
                // 当前会回调这个方法，显示"Not implemented yet"
                Toast.makeText(this@MainActivity, "检查更新失败: $errorMessage", Toast.LENGTH_LONG).show()
            }
        })
    }
    
    private fun showSDKInfo() {
        val info = """
            SDK版本: ${AppUpdaterSDK.getVersionInfo()}
            是否已初始化: ${AppUpdaterSDK.isInitialized()}
            配置信息: ${AppUpdaterSDK.getConfig()?.baseUrl ?: "未配置"}
        """.trimIndent()
        
        Toast.makeText(this, info, Toast.LENGTH_LONG).show()
    }
}