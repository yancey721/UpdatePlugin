# APK存储按应用分文件夹升级说明

## 概述

为了更好地组织和管理APK文件，我们将APK存储结构从单一目录升级为按应用ID分文件夹存储。

## 修改内容

### 1. FileStorageService 修改

**文件**: `Server/src/main/java/com/yancey/appupdate/service/FileStorageService.java`

#### 主要变更：

1. **storeApkFile方法**：
   - 新增`sanitizeForFilename`方法清理应用ID
   - 按应用ID创建子目录：`uploads/{cleanAppId}/`
   - 返回相对路径：`{cleanAppId}/{fileName}`
   - 自动创建应用专属目录

2. **文件操作方法更新**：
   - `resolveApkPath`: 支持子目录路径解析
   - `generateDownloadUrl`: 处理子目录URL路径
   - `loadFileAsResource`: 支持子目录文件加载
   - `deleteFile`: 支持子目录文件删除
   - `fileExists`: 支持子目录文件检查
   - `getFileSize`: 支持子目录文件大小获取

3. **新增方法**：
   - `sanitizeForFilename`: 清理文件名特殊字符，确保可作为文件夹名

### 2. AppController 修改

**文件**: `Server/src/main/java/com/yancey/appupdate/controller/AppController.java`

#### 主要变更：

1. **下载接口升级**：
   - 路径映射从 `@GetMapping("/download/{fileName:.+}")` 改为 `@GetMapping("/download/**")`
   - 支持子目录路径：`/api/app/download/{appId}/{fileName}`
   - 动态解析请求URI获取完整文件路径
   - 提取文件名用于下载响应头

## 新的存储结构

### 目录结构

```
apk_uploads/
├── com.example.app1/
│   ├── com.example.app1-1.apk
│   ├── com.example.app1-2.apk
│   └── com.example.app1-3.apk
├── com.example.app2/
│   ├── com.example.app2-1.apk
│   └── com.example.app2-2.apk
├── com.yancey.android/
│   └── com.yancey.android-1.apk
└── ...
```

### 文件命名规则

- **文件夹名**: `{cleanAppId}` (清理后的应用ID)
- **文件名**: `{cleanAppId}-{versionCode}.apk`
- **相对路径**: `{cleanAppId}/{cleanAppId}-{versionCode}.apk`

### URL格式

- **下载URL**: `/api/app/download/{cleanAppId}/{fileName}`
- **示例**: `/api/app/download/com.example.app1/com.example.app1-2.apk`

## 优势

### 1. 组织清晰
- 每个应用的APK文件都在独立的文件夹中
- 便于查看特定应用的所有版本

### 2. 避免冲突
- 不同应用的同版本号文件不会冲突
- 文件名更加明确和唯一

### 3. 便于管理
- 可以按应用进行文件管理操作
- 便于备份和清理特定应用的文件

### 4. 扩展性好
- 支持更多应用和版本
- 便于添加应用级别的元数据

## 兼容性

### 向后兼容
- 现有的API接口保持不变
- 数据库结构无需修改
- 客户端无需更新

### 文件名清理
- 应用ID中的特殊字符会被替换为下划线
- 只保留字母、数字、点、下划线、连字符
- 确保跨平台文件系统兼容性

## 测试

### 测试脚本
提供了 `Server/test_upload.sh` 脚本用于测试新的存储功能：

```bash
cd Server
./test_upload.sh
```

### 测试内容
1. 服务器连接测试
2. 多应用APK上传测试
3. 文件存储结构验证
4. 下载功能测试
5. 应用列表查询测试

## 部署注意事项

### 1. 现有文件迁移
如果有现有的APK文件，需要手动迁移到新的目录结构：

```bash
# 示例迁移脚本
mkdir -p apk_uploads/com.example.app
mv apk_uploads/com.example.app-*.apk apk_uploads/com.example.app/
```

### 2. 权限设置
确保应用目录有正确的读写权限：

```bash
chmod -R 755 apk_uploads/
```

### 3. 备份策略
更新备份脚本以适应新的目录结构。

## 总结

这次升级显著改善了APK文件的组织结构，提高了管理效率，同时保持了良好的向后兼容性。新的按应用分文件夹存储方式为未来的功能扩展奠定了良好的基础。 