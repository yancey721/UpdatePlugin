# 移动端集成指南 (Android)

## 1. SDK设计概述

应用内更新SDK旨在为Android应用提供一套简单易用的更新检查、下载和安装引导功能。SDK应具备以下特点：

- **轻量级**：尽量减少对应用体积的影响。
- **易于集成**：提供简洁的API和清晰的集成文档。
- **可配置性**：允许开发者自定义更新UI、下载策略等。
- **稳定性**：处理各种网络异常和边界情况。

### 1.1 主要功能模块

1.  **版本检查模块 (`UpdateChecker`)**: 
    -   向服务端API发起检查更新请求。
    -   解析服务端返回的更新信息。
    -   回调通知应用是否有可用更新。
2.  **下载模块 (`DownloadManager`)**: 
    -   负责下载新版本APK文件。
    -   支持后台下载。
    -   支持下载进度回调。
    -   支持断点续传 (可选，但推荐)。
    -   MD5校验下载文件的完整性。
3.  **UI交互模块 (`UpdateUIHandler`)**: 
    -   提供默认的更新提示对话框 (强制更新、可选更新)。
    -   允许开发者自定义更新对话框样式和行为。
    -   引导用户安装已下载的APK。
4.  **配置模块 (`UpdateConfig`)**: 
    -   允许开发者配置服务端URL、App ID、UI样式、下载行为等。

## 2. 技术选型

- **语言**: Java 或 Kotlin (推荐Kotlin以利用其现代特性)
- **网络请求**: OkHttp (轻量、高效，广泛使用)
- **JSON解析**: Gson 或 Moshi
- **权限处理**: Android原生权限请求机制
- **文件操作**: Android原生API

## 3. 集成步骤

### 3.1 添加SDK依赖

(假设SDK以AAR包或Maven依赖形式提供)

```gradle
// build.gradle (Module: app)
dependencies {
    implementation 'com.yourdomain:app-update-sdk:1.0.0' // 示例依赖
}
```

### 3.2 配置SDK

在应用的 `Application` 类或启动Activity中初始化SDK配置：

```kotlin
// Kotlin示例
import com.yourdomain.sdk.AppUpdaterSDK;
import com.yourdomain.sdk.UpdateConfig;

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val updateConfig = UpdateConfig.Builder(this)
            .setBaseUrl("https://your-server-api-url.com/api/app/") // 服务端API基础路径
            .setAppId("your_app_id") // 在服务端注册的应用ID
            .setChannel("official") // 可选，渠道信息
            // .setUpdateDialogTheme(R.style.CustomUpdateDialog) // 可选，自定义对话框样式
            // .setNotificationIcon(R.drawable.ic_update_notification) // 可选，下载通知栏图标
            .build()

        AppUpdaterSDK.init(updateConfig)
    }
}
```

### 3.3 检查更新调用

在合适的时机（如App启动时、用户手动触发等）调用检查更新API。

```kotlin
// Kotlin示例
import com.yourdomain.sdk.AppUpdaterSDK;
import com.yourdomain.sdk.UpdateCallback;
import com.yourdomain.sdk.UpdateInfo;

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 检查更新
        checkAppUpdate()
    }

    private fun checkAppUpdate() {
        AppUpdaterSDK.checkUpdate(object : UpdateCallback {
            override fun onUpdateAvailable(updateInfo: UpdateInfo) {
                // 发现新版本，SDK内部会根据配置处理UI提示
                // 开发者也可以在此处获取updateInfo进行自定义处理
                Log.d("AppUpdate", "新版本可用: ${updateInfo.versionName}")
                // 例如：如果不想使用SDK的默认UI，可以在此弹出自定义对话框
                // showCustomUpdateDialog(updateInfo);
                // AppUpdaterSDK.startDownload(updateInfo); // 如果选择自定义UI，需要手动调用下载
            }

            override fun onNoUpdateAvailable() {
                Log.d("AppUpdate", "已是最新版本")
                // Toast.makeText(this@MainActivity, "已是最新版本", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                Log.e("AppUpdate", "检查更新失败: $errorCode - $errorMessage")
                // Toast.makeText(this@MainActivity, "检查更新失败: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onDownloadProgress(progress: Int) {
                // 下载进度回调 (如果使用SDK的下载功能)
                Log.d("AppUpdate", "下载进度: $progress%")
            }

            override fun onDownloadCompleted(apkPath: String) {
                // 下载完成，SDK会自动尝试安装
                // apkPath 是下载的APK文件路径
                Log.d("AppUpdate", "下载完成: $apkPath")
                // AppUpdaterSDK.installApp(apkPath); // 如果需要手动触发安装
            }

            override fun onDownloadFailed(errorCode: Int, errorMessage: String) {
                Log.e("AppUpdate", "下载失败: $errorCode - $errorMessage")
            }
        })
    }
}
```

### 3.4 权限配置

在 `AndroidManifest.xml` 中添加必要的权限：

```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- 如果需要下载到公共目录或进行安装 (Android 8.0+需要明确请求) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!-- Android 8.0及以上安装未知来源应用权限 -->
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

    <application ...>
        <!-- Android 7.0+ 文件共享配置 (用于APK安装) -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

创建 `res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 根据SDK下载文件的实际存储位置配置 -->
    <external-cache-path name="update_apk" path="." />
    <!-- 或者 <external-files-path name="update_apk" path="Download/" /> -->
</paths>
```

### 3.5 处理Android 8.0+的安装权限

对于Android 8.0 (API 26) 及以上版本，安装未知来源的应用需要用户明确授予权限。SDK应能检测并引导用户开启此权限。

```kotlin
// 在尝试安装APK前检查权限
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val desconocidoSource = context.packageManager.canRequestPackageInstalls()
    if (!desconocidoSource) {
        // 请求权限
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        intent.data = Uri.parse("package:" + context.packageName)
        // 建议在Activity中启动，并处理onActivityResult
        // activity.startActivityForResult(intent, REQUEST_CODE_UNKNOWN_APP_SOURCES)
        return // 暂不安装，等待用户授权
    }
}
// 继续安装流程
AppUpdaterSDK.installApp(apkPath)
```
SDK内部应封装此逻辑。

## 4. API接口规范 (SDK提供给App调用)

### `AppUpdaterSDK`

- `fun init(config: UpdateConfig)`: 初始化SDK。
- `fun checkUpdate(callback: UpdateCallback)`: 异步检查更新。
- `fun startDownload(updateInfo: UpdateInfo)`: (可选) 如果开发者选择自定义UI，可以手动调用此方法开始下载。
- `fun installApp(apkPath: String)`: (可选) 手动触发安装已下载的APK。
- `fun cancelDownload()`: 取消当前下载。
- `fun getSDKVersion(): String`: 获取SDK版本号。

### `UpdateConfig.Builder`

- `constructor(context: Context)`
- `fun setBaseUrl(url: String): Builder`: 设置服务端API基础地址。
- `fun setAppId(appId: String): Builder`: 设置应用ID。
- `fun setChannel(channel: String): Builder`: 设置渠道号。
- `fun setCheckUpdateUrl(url: String): Builder`: (可选) 自定义检查更新的完整URL。
- `fun setUiTheme(dialogThemeResId: Int): Builder`: (可选) 自定义更新对话框的样式。
- `fun setNotificationIcon(iconResId: Int): Builder`: (可选) 下载通知栏图标。
- `fun setAlwaysShowNotification(show: Boolean): Builder`: (可选) 是否总是显示下载通知。
- `fun setAutoInstall(auto: Boolean): Builder`: (可选) 下载完成后是否自动尝试安装 (默认为true)。
- `fun build(): UpdateConfig`

### `UpdateCallback` (Interface)

- `fun onUpdateAvailable(updateInfo: UpdateInfo)`: 发现新版本。
- `fun onNoUpdateAvailable()`: 当前已是最新版本。
- `fun onError(errorCode: Int, errorMessage: String)`: 检查更新过程中发生错误。
- `fun onDownloadProgress(progress: Int)`: 下载进度 (0-100)。
- `fun onDownloadCompleted(apkPath: String)`: APK下载完成。
- `fun onDownloadFailed(errorCode: Int, errorMessage: String)`: 下载失败。
- `fun onDownloadCanceled()`: 下载被取消。

### `UpdateInfo` (Data Class)

包含从服务端获取的版本信息：

```kotlin
data class UpdateInfo(
    val hasUpdate: Boolean,            // 是否有更新
    val versionCode: Int,              // 新版本号
    val versionName: String,           // 新版本名
    val fileSize: Long,                // 文件大小 (字节)
    val md5: String,                   // 文件MD5
    val downloadUrl: String,           // 下载地址
    val updateDescription: String,     // 更新说明
    val forceUpdate: Boolean           // 是否强制更新
)
```

## 5. UI交互设计建议

### 5.1 更新提示对话框

- **可选更新**: 
    - 标题: "发现新版本 V1.0.2"
    - 内容: 更新说明 (可滚动)
    - 按钮: "立即更新", "稍后提醒" (或 "忽略此版本")
- **强制更新**: 
    - 标题: "重要更新 V1.0.3"
    - 内容: 更新说明 (可滚动)
    - 按钮: "立即更新" (用户不可取消对话框)
    - 提示: "此版本包含重要安全更新，请立即升级。"

### 5.2 下载通知

- 显示应用图标、下载进度条、百分比。
- 支持点击通知栏暂停/继续下载 (高级功能)。
- 下载完成后，通知栏提示点击安装。

## 6. 错误处理与日志

- 定义清晰的错误码，方便排查问题。
- SDK内部应记录详细的日志，便于调试。
- `UpdateCallback` 的 `onError`, `onDownloadFailed` 回调应提供明确的错误信息。

## 7. 兼容性与测试

- **Android版本兼容性**: 至少支持Android 5.0 (API 21) 及以上。
- **厂商ROM兼容性**: 在主流厂商的设备上进行测试 (华为、小米、OPPO、VIVO等)。
- **网络测试**: 测试不同网络环境下的表现 (Wi-Fi, 4G, 弱网, 无网络)。

## 8. 未来扩展

- **增量更新/热更新支持** (如果业务需要，但会增加复杂度)。
- **下载断点续传的鲁棒性增强**。
- **更灵活的UI定制方案**。
- **后台静默下载与安装提示**。 