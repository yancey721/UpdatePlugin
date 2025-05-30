# APK 文件存储目录

## 📁 目录说明
此目录用于存储上传的 APK 文件，是 Android 应用更新系统的文件存储位置。

## 📋 文件说明
- **原始APK文件**: 用户上传的原始 APK 文件
- **处理后文件**: 系统按照 `{appId}-{versionCode}.apk` 格式重命名的文件
- **临时文件**: 上传过程中的临时文件（系统会自动清理）

## 🔧 配置信息
- **存储路径**: 通过 `app.upload-path` 配置项设置
- **默认配置**: `../apk_uploads/` (相对于Server目录)
- **环境变量**: 可通过 `UPLOAD_PATH` 环境变量覆盖

## 📝 注意事项
- APK 文件不会被提交到 Git 仓库中
- 系统会自动创建必要的子目录
- 确保目录具有读写权限
- 生产环境建议使用绝对路径

## 🗂️ 文件命名规则
```
原始文件: {originalFileName}.apk
处理文件: {appId}-{versionCode}.apk
临时文件: {appId}-temp.apk
```

## 🔍 示例文件
```
apk_uploads/
├── UnifySign_productRelease_v1.1.8001.apk    # 原始文件
├── com.unifysign.android-56.apk              # 处理后文件
└── com.unifysign.test-56.apk                 # 测试文件
``` 