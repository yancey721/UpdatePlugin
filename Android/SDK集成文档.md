# App Updater SDK - Android 集成文档

**版本**: 1.0.0

本文档旨在帮助Android开发者快速、高效地将App Updater SDK集成到现有应用中。

---

## 目录

1. [特性概览](#特性概览)
2. [快速集成](#快速集成)
    - [1. 添加Gradle依赖](#1-添加gradle依赖)
    - [2. 配置AndroidManifest.xml](#2-配置androidmanifestxml)
    - [3. 初始化SDK](#3-初始化sdk)
3. [基本用法](#基本用法)
    - [检查更新（使用默认UI）](#检查更新使用默认ui)
    - [检查更新（自定义UI）](#检查更新自定义ui)
4. [高级用法](#高级用法)
    - [手动触发下载和安装](#手动触发下载和安装)
    - [处理更新回调](#处理更新回调)
    - [处理下载回调](#处理下载回调)
    - [处理安装回调](#处理安装回调)
    - [权限处理](#权限处理)
5. [API参考](#api参考)
    - [`AppUpdaterSDK`](#appupdatersdk)
    - [`UpdateConfig`](#updateconfig)
    - [`UpdateCallback`](#updatecallback)
    - [`DownloadCallback`](#downloadcallback)
    - [`InstallCallback`](#installcallback)
    - [`UpdateInfo`](#updateinfo)
6. [注意事项](#注意事项)
    - [ProGuard混淆规则](#proguard混淆规则)
    - [Android版本兼容性](#android版本兼容性)

---

## 特性概览

- **轻量级**: SDK体积小，对应用性能影响降到最低。
- **高可控**: 提供分离的步骤（检查、下载、安装），开发者可完全控制更新流程。
- **UI自定义**: 支持默认UI和完全自定义UI两种模式。
- **版本兼容**: 完美适配Android 5.0 (API 21) 到最新版本。
- **权限处理**: 内置智能的权限检查和引导机制。
- **后台下载**: 使用系统`DownloadManager`，稳定可靠。
- **文件校验**: 支持MD5校验，确保安装包完整性。

---

## 快速集成

### 1. 添加Gradle依赖

首先，将SDK模块作为依赖添加到您的主应用模块（通常是 `app`）的 `build.gradle.kts` 文件中。

```kotlin
// app/build.gradle.kts

dependencies {
    // ... 其他依赖
    implementation(project(":sdk"))
}
```

### 2. 配置AndroidManifest.xml

SDK需要一些基本权限和组件声明才能正常工作。请确保您的主应用模块的 `AndroidManifest.xml` 文件包含了以下内容。

> **注意**: 如果您的应用已经有这些权限，则无需重复添加。

```xml
<!-- /app/src/main/AndroidManifest.xml -->
<manifest ...>

    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- 外部存储权限 (仅在部分旧版本系统需要) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
        android:maxSdkVersion="32" />
    
    <!-- 安装应用权限 (Android 8.0+) -->
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    
    <!-- 下载管理器权限 -->
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

    <application ...>
        
        <!-- FileProvider配置，支持Android 7.0+文件共享 -->
        <!-- 注意：android:authorities需要与您的应用包名一致 -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.update.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_provider_paths" />
        </provider>
        
        <!-- ... 其他组件 -->
    </application>
</manifest>
```

接下来，您需要在 `app` 模块的 `res/xml/` 目录下创建一个 `file_provider_paths.xml` 文件，用于定义文件共享路径。

```xml
<!-- /app/src/main/res/xml/file_provider_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <!-- 定义APK下载目录，与DownloadManager的配置保持一致 -->
    <external-files-path name="update_apk" path="Download/" />
</paths>
```

### 3. 初始化SDK

在您的 `Application` 类的 `onCreate` 方法中初始化SDK。这是推荐的做法，以确保SDK在整个应用生命周期中都可用。

```kotlin
// YourApplication.kt

import android.app.Application
import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.config.UpdateConfig

class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 创建SDK配置
        val config = UpdateConfig.Builder(this)
            .setBaseUrl("https://your-server.com/api/app/") // 设置您的更新服务器地址
            .setAppId(packageName) // 通常是应用的包名
            .setEnableDefaultUI(true) // 启用SDK内置的UI对话框
            .setEnableLog(true) // 建议在调试时开启日志
            .build()
            
        // 初始化SDK
        AppUpdaterSDK.init(this, config)
    }
}
```

别忘了在 `AndroidManifest.xml` 中注册您的 `Application` 类。

```xml
<application
    android:name=".YourApplication"
    ...>
</application>
```

---

## 基本用法

### 检查更新（使用默认UI）

如果初始化时 `setEnableDefaultUI(true)`，那么检查更新流程将由SDK自动处理，包括显示更新对话框、下载进度条和触发安装。

在您需要检查更新的地方（例如 `MainActivity` 的 `onCreate` 方法或一个按钮点击事件），调用 `checkUpdate` 方法。

```kotlin
// MainActivity.kt

import com.yancey.sdk.AppUpdaterSDK
import com.yancey.sdk.callback.UpdateCallback
import com.yancey.sdk.data.UpdateInfo

// ...

AppUpdaterSDK.checkUpdate(object : UpdateCallback {
    override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
        if (updateInfo.hasUpdate) {
            // SDK已检测到更新，如果启用了默认UI，将自动弹出对话框
            // 您可以在这里添加额外的逻辑，例如数据上报
        } else {
            // 没有可用更新
            Toast.makeText(this@MainActivity, "已是最新版本", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(errorCode: Int, errorMessage: String) {
        // 检查更新失败
        Toast.makeText(this@MainActivity, "检查更新失败: $errorMessage", Toast.LENGTH_LONG).show()
    }
})
```

### 检查更新（自定义UI）

如果您希望完全控制UI，请在初始化时设置 `setEnableDefaultUI(false)`。

```kotlin
// 初始化时禁用默认UI
val config = UpdateConfig.Builder(this)
    .setEnableDefaultUI(false)
    .build()
```

然后，在 `UpdateCallback` 的 `onUpdateCheckSuccess` 回调中，您可以获取到更新信息 `UpdateInfo`，并使用它来构建您自己的UI。

```kotlin
// 检查更新回调
override fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
    if (updateInfo.hasUpdate) {
        // 检测到更新，显示您自己的对话框
        showCustomUpdateDialog(updateInfo)
    } else {
        // 无更新
    }
}

// ...

private fun showCustomUpdateDialog(updateInfo: UpdateInfo) {
    AlertDialog.Builder(this)
        .setTitle("发现新版本 ${updateInfo.newVersionName}")
        .setMessage(updateInfo.updateDescription)
        .setPositiveButton("立即更新") { _, _ ->
            // 用户点击更新，开始下载
            startCustomDownload(updateInfo)
        }
        .setNegativeButton("稍后", null)
        .show()
}

private fun startCustomDownload(updateInfo: UpdateInfo) {
    // 在这里显示您自己的下载进度UI
    // ...
    
    // 调用SDK开始下载
    AppUpdaterSDK.startDownload(updateInfo, object : DownloadCallback {
        override fun onDownloadStart() {
            // 下载开始
        }

        override fun onDownloadProgress(progress: DownloadProgress) {
            // 更新您的下载进度UI
            // progress.percent, progress.downloadedSize, progress.totalSize
        }

        override fun onDownloadComplete(file: File) {
            // 下载完成，触发安装
            AppUpdaterSDK.installApk(file)
        }

        override fun onDownloadError(errorCode: Int, errorMessage: String) {
            // 下载失败
        }

        override fun onDownloadCancel() {
            // 下载被取消
        }
    })
}
```

---

## 高级用法

### 手动触发下载和安装

即使使用默认UI，您也可以在获取到 `UpdateInfo` 对象后，手动调用下载和安装方法。

```kotlin
// 手动开始下载
AppUpdaterSDK.startDownload(updateInfo, downloadCallback)

// 手动取消下载
AppUpdaterSDK.cancelDownload()

// 手动触发安装
val apkFile = File(apkPath) // 获取已下载的APK文件
AppUpdaterSDK.installApk(apkFile, installCallback)
```

### 处理更新回调

`UpdateCallback` 接口提供了检查更新过程中的详细回调。

```kotlin
interface UpdateCallback {
    // 检查成功，返回更新信息
    fun onUpdateCheckSuccess(updateInfo: UpdateInfo)
    
    // 检查失败
    fun onError(errorCode: Int, errorMessage: String)
}
```

### 处理下载回调

`DownloadCallback` 接口提供了下载过程中的详细回调。

```kotlin
interface DownloadCallback {
    fun onDownloadStart()
    fun onDownloadProgress(downloadProgress: DownloadProgress)
    fun onDownloadComplete(file: File)
    fun onDownloadError(errorCode: Int, errorMessage: String)
    fun onDownloadCancel()
}
```

### 处理安装回调

`InstallCallback` 接口提供了安装过程中的回调。

```kotlin
interface InstallCallback {
    // 成功启动系统安装器
    fun onInstallStart(apkFile: File)
    
    // 需要安装权限，询问用户是否前往设置页面
    fun onInstallPermissionRequired(onUserConfirm: () -> Unit, onUserCancel: () -> Unit)
    
    // 安装失败
    fun onInstallError(errorCode: Int, errorMessage: String)
}
```

### 权限处理

SDK内置了完整的权限处理逻辑，但您也可以手动调用相关方法。

```kotlin
// 检查是否拥有安装权限
val hasPermission = AppUpdaterSDK.checkInstallPermission()

// 如果没有权限，可以主动请求
if (!hasPermission) {
    AppUpdaterSDK.requestInstallPermission()
}
```

**完整的权限处理示例**

以下是一个完整的权限处理示例，包括权限说明对话框和从设置页返回后的处理：

```kotlin
// MainActivity.kt

class MainActivity : AppCompatActivity() {
    
    // 待安装的APK文件（从设置页返回时使用）
    private var pendingInstallApk: File? = null
    
    override fun onResume() {
        super.onResume()
        
        // 【重要】检查SDK内部是否有待安装的任务（用户从设置页返回时自动处理）
        AppUpdaterSDK.checkAndHandlePendingInstall()
        
        // 如果您有自定义的安装逻辑，也需要检查自定义的待安装任务
        pendingInstallApk?.let { file ->
            if (AppUpdaterSDK.checkInstallPermission()) {
                val apkToInstall = file
                pendingInstallApk = null // 清除待安装任务
                AppUpdaterSDK.installApk(apkToInstall, createInstallCallback())
            } else {
                pendingInstallApk = null // 清除待安装任务
                Toast.makeText(this, "权限未开启，无法安装", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 创建安装回调
     */
    private fun createInstallCallback(): InstallCallback {
        return object : InstallCallback {
            override fun onInstallStart(apkFile: File) {
                Toast.makeText(this@MainActivity, "正在启动安装程序...", Toast.LENGTH_SHORT).show()
                pendingInstallApk = null // 清除待安装任务
            }
            
            override fun onInstallPermissionRequired(onUserConfirm: () -> Unit, onUserCancel: () -> Unit) {
                // 显示权限说明对话框
                showPermissionExplanationDialog(
                    onConfirm = { onUserConfirm() },
                    onCancel = { onUserCancel() }
                )
            }
            
            override fun onInstallError(errorCode: Int, errorMessage: String) {
                when (errorCode) {
                    -3 -> Toast.makeText(this@MainActivity, "权限未开启，无法安装", Toast.LENGTH_LONG).show()
                    else -> Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                pendingInstallApk = null // 清除待安装任务
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
            .setCancelable(false)
            .show()
    }
    
    private fun setPendingInstallFromCurrentContext() {
        // 设置实际的APK文件路径
        pendingInstallApk = File(getExternalFilesDir(null), "downloaded_update.apk")
    }
}
```

---

## API参考

### `AppUpdaterSDK`

SDK的主入口，提供所有公共API。

- `init(context: Context, config: UpdateConfig)`: 初始化SDK。
- `checkUpdate(callback: UpdateCallback)`: 检查更新。
- `startDownload(updateInfo: UpdateInfo, callback: DownloadCallback?)`: 开始下载。
- `cancelDownload()`: 取消下载。
- `installApk(file: File, callback: InstallCallback?)`: 安装APK。
- `checkInstallPermission(): Boolean`: 检查安装权限。
- `requestInstallPermission(): Boolean`: 请求安装权限。
- `checkAndHandlePendingInstall(): Boolean`: 检查并处理待安装任务（在Activity的onResume中调用）。
- `release()`: 释放资源。

### `UpdateConfig`

使用 `UpdateConfig.Builder` 来创建SDK配置。

- `setBaseUrl(url: String)`: **必须**，更新服务器地址。
- `setAppId(appId: String)`: **必须**，应用唯一标识。
- `setEnableDefaultUI(enable: Boolean)`: 是否启用默认UI，默认`true`。
- `setEnableLog(enable: Boolean)`: 是否启用日志，默认`false`。
- `setLogLevel(level: Int)`: 设置日志级别。
- `setChannel(channel: String)`: 设置渠道号。

### `UpdateCallback`

检查更新的回调接口。

### `DownloadCallback`

文件下载的回调接口。

### `InstallCallback`

APK安装的回调接口。

### `UpdateInfo`

包含版本更新信息的只读数据类。

- `hasUpdate: Boolean`: 是否有更新。
- `forceUpdate: Boolean`: 是否强制更新。
- `newVersionCode: Int`: 新版本号。
- `newVersionName: String`: 新版本名。
- `updateDescription: String`: 更新描述。
- `fileSize: Long`: 文件大小（字节）。
- `downloadUrl: String`: 下载地址。
- `md5: String`: 文件MD5值。

---

## 注意事项

### ProGuard混淆规则

如果您的应用使用了ProGuard进行代码混淆，请在您的 `proguard-rules.pro` 文件中添加以下规则，以确保SDK正常工作。

```proguard
# App Updater SDK
-keep class com.yancey.sdk.** { *; }
-keep interface com.yancey.sdk.** { *; }
-dontwarn com.yancey.sdk.**
```

### Android版本兼容性

- **Android 7.0 (API 24)**: 必须配置 `FileProvider` 才能正常安装APK。
- **Android 8.0 (API 26)**: 必须处理 `REQUEST_INSTALL_PACKAGES` 权限。SDK已内置此逻辑。
- **Android 10.0 (API 29)**: `WRITE_EXTERNAL_STORAGE` 权限受限，SDK使用应用专属目录，不受影响。
- **Android 11.0 (API 30)**: 存储权限进一步收紧，SDK已适配分区存储。
- **Android 13.0 (API 33)**: `READ_EXTERNAL_STORAGE` 权限被细分，但SDK下载和安装功能不受影响。

---
**文档结束** 