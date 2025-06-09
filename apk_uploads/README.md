# APK文件存储结构说明

## 目录结构

APK文件现在按应用ID分文件夹存储，结构如下：

```
apk_uploads/
├── {appId1}/
│   ├── {appId1}-{version1}.apk
│   ├── {appId1}-{version2}.apk
│   └── ...
├── {appId2}/
│   ├── {appId2}-{version1}.apk
│   └── ...
└── ...
```

## 示例结构

```
apk_uploads/
├── com.example.app1/
│   ├── com.example.app1-1.apk
│   └── com.example.app1-2.apk
├── com.example.app2/
│   └── com.example.app2-1.apk
└── com.yancey.android/
    └── com.yancey.android-1.apk
```

## 优势

1. **组织清晰**: 每个应用的APK文件都在独立的文件夹中
2. **便于管理**: 可以轻松查看和管理特定应用的所有版本
3. **避免冲突**: 不同应用的同版本号文件不会冲突
4. **便于备份**: 可以按应用进行选择性备份
5. **便于清理**: 可以轻松删除特定应用的所有版本

## 文件命名规则

- 文件夹名: `{appId}` (经过清理，只保留字母、数字、点、下划线、连字符)
- 文件名: `{appId}-{versionCode}.apk`

## 下载URL格式

- 新格式: `/api/app/download/{appId}/{fileName}`
- 示例: `/api/app/download/com.example.app1/com.example.app1-2.apk`

## 兼容性

- 服务器端已更新支持子目录结构
- 下载接口支持新的路径格式
- 文件存储服务已适配子目录操作 