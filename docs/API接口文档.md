# 应用内更新系统 - API接口文档

## 概述

本文档描述了应用内更新系统的所有API接口，包括管理端和移动端接口。

### 服务器信息
- **管理端基础URL**: `http://localhost:8080/api/admin/app`
- **移动端基础URL**: `http://localhost:8080/api/app`
- **认证方式**: 管理端API需要API密钥认证，移动端API无需认证
- **数据格式**: JSON
- **字符编码**: UTF-8

### 认证机制

#### 管理端API认证
所有管理端API（`/api/admin/*`）都需要在请求头中提供API密钥：

```http
X-API-KEY: your-secret-api-key
```

**配置API密钥**：
- 默认密钥：`your-secret-api-key`
- 可通过环境变量 `ADMIN_API_KEY` 自定义
- 可在 `application.yml` 中配置 `app.admin.api-key`

**认证错误响应**：
- 缺少API密钥：返回 `401 Unauthorized`
- 无效API密钥：返回 `403 Forbidden`

#### 移动端API
移动端API（`/api/app/*`）无需认证，可直接访问。

### 通用响应格式

所有API接口都返回统一的响应格式：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {}, 
    "timestamp": 1748589121124
}
```

## 移动端API接口

### 1. 检查更新接口

#### 1.1 检查应用更新
- **接口**: `POST /check-update`
- **描述**: 移动端检查应用是否有新版本可更新
- **请求体**:

```json
{
    "appId": "com.unifysign.test",
    "currentVersionCode": 50
}
```

**参数说明**:
- `appId` (必填): 应用ID
- `currentVersionCode` (必填): 当前版本号

**响应示例 - 有更新**:
```json
{
    "code": 200,
    "message": "发现新版本",
    "data": {
        "hasUpdate": true,
        "newVersionName": "1.1.8001",
        "newVersionCode": 56,
        "updateDescription": "测试版本-强制更新",
        "forceUpdate": true,
        "downloadUrl": "http://localhost:8080/api/app/download/com.unifysign.test-56.apk",
        "md5": "d96f67fb6e2c8041cba9896ce7cbd8cb",
        "fileSize": 78083108
    }
}
```

**响应示例 - 无更新**:
```json
{
    "code": 200,
    "message": "当前已是最新版本",
    "data": {
        "hasUpdate": false
    }
}
```

**测试示例**:
```bash
# 检查更新 - 有新版本
curl -X POST "http://localhost:8080/api/app/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 50
  }'

# 检查更新 - 无更新
curl -X POST "http://localhost:8080/api/app/check-update" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": "com.unifysign.test",
    "currentVersionCode": 56
  }'
```

### 2. APK下载接口

#### 2.1 下载APK文件
- **接口**: `GET /download/{fileName}`
- **描述**: 下载指定的APK文件
- **参数**:
  - `fileName` (路径参数): APK文件名

**响应**:
- **Content-Type**: `application/vnd.android.package-archive`
- **Content-Disposition**: `attachment; filename="文件名.apk"`
- **状态码**: 200 (成功) / 404 (文件不存在)

**测试示例**:
```bash
# 下载APK文件
curl -O "http://localhost:8080/api/app/download/com.unifysign.test-56.apk"

# 检查文件信息
curl -I "http://localhost:8080/api/app/download/com.unifysign.test-56.apk"
```

## 管理端API接口

### 1. 应用管理接口

#### 1.1 查询应用列表
- **接口**: `GET /apps`
- **描述**: 查询应用列表，支持分页和按应用名称模糊搜索
- **参数**:
  - `appNameQuery` (可选): 应用名称搜索关键词
  - `page` (可选): 页码，从0开始，默认0
  - `size` (可选): 每页大小，默认10
  - `sort` (可选): 排序字段，默认createTime

```bash
GET /api/admin/app/apps?appNameQuery=桂云&page=0&size=10&sort=createTime,desc
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "content": [
            {
                "id": 1,
                "appId": "com.unifysign.android",
                "appName": "桂云签",
                "packageName": "cn.org.bjca.signet.unify.app",
                "createTime": "2025-05-30T12:59:45.079815",
                "updateTime": "2025-05-30T12:59:45.079858",
                "latestVersionId": 1,
                "latestVersionCode": 56,
                "latestVersionName": "1.1.8001",
                "latestFileSize": 78083108,
                "latestUpdateDescription": "修复了一些已知问题",
                "latestForceUpdate": false,
                "latestStatus": 1,
                "latestStatusDescription": "启用",
                "latestVersionCreateTime": "2025-05-30T12:59:45.14922",
                "totalVersions": 1
            }
        ],
        "totalPages": 1,
        "totalElements": 1,
        "size": 10,
        "number": 0
    }
}
```

#### 1.2 查询应用版本列表
- **接口**: `GET /app/{appId}/versions`
- **描述**: 查询指定应用的版本列表，支持分页和排序
- **参数**:
  - `appId` (必填): 应用ID
  - `page` (可选): 页码，从0开始，默认0
  - `size` (可选): 每页大小，默认10
  - `sort` (可选): 排序字段，默认versionCode,desc

```bash
GET /api/admin/app/app/com.unifysign.android/versions?page=0&size=10
```

### 2. 版本管理接口

#### 2.1 上传APK并创建版本
- **接口**: `POST /upload`
- **描述**: 上传APK文件并自动解析创建应用版本
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `apkFile` (必填): APK文件
  - `appId` (必填): 应用ID，最大100字符
  - `updateDescription` (可选): 更新说明
  - `forceUpdate` (可选): 是否强制更新，默认false

```bash
curl -X POST "http://localhost:8080/api/admin/app/upload" \
  -F "apkFile=@app.apk" \
  -F "appId=com.example.app" \
  -F "updateDescription=修复已知问题" \
  -F "forceUpdate=false"
```

#### 2.2 修改版本信息
- **接口**: `PUT /version/{versionId}`
- **描述**: 修改指定版本的信息
- **参数**:
  - `versionId` (路径参数): 版本ID
- **请求体**:

```json
{
    "updateDescription": "修改后的更新说明：修复了一些已知问题，提升了应用稳定性",
    "forceUpdate": true,
    "status": 1
}
```

#### 2.3 更新版本状态
- **接口**: `PUT /version/{versionId}/status`
- **描述**: 更新版本的状态
- **参数**:
  - `versionId` (路径参数): 版本ID
- **请求体**:

```json
{
    "status": 2
}
```

**状态值说明**:
- `0`: 禁用
- `1`: 启用  
- `2`: 测试

#### 2.4 删除版本
- **接口**: `DELETE /version/{versionId}`
- **描述**: 删除指定的版本
- **参数**:
  - `versionId` (路径参数): 版本ID
  - `forceDelete` (可选): 是否强制删除文件，默认true

```bash
DELETE /api/admin/app/version/1?forceDelete=true
```

#### 2.5 批量删除版本
- **接口**: `DELETE /versions`
- **描述**: 批量删除多个版本
- **请求体**:

```json
{
    "versionIds": [1, 2, 3],
    "forceDelete": false
}
```

### 3. 统计信息接口

#### 3.1 获取版本统计信息
- **接口**: `GET /stats`
- **描述**: 获取系统的版本统计信息

**响应示例**:
```json
{
    "code": 200,
    "message": "获取统计信息成功",
    "data": {
        "totalApps": 2,
        "totalVersions": 2,
        "enabledVersions": 1,
        "disabledVersions": 0,
        "testVersions": 1,
        "forceUpdateVersions": 1,
        "recentVersions": [
            {
                "id": 1,
                "appId": "com.unifysign.android",
                "appName": "桂云签",
                "versionCode": 56,
                "versionName": "1.1.8001",
                "status": 2,
                "statusDescription": "测试",
                "createTime": "2025-05-30T12:59:45.14922"
            }
        ],
        "statusStats": {
            "启用": 1,
            "测试": 1,
            "禁用": 0
        },
        "totalFileSize": 156166216,
        "statisticsTime": "2025-05-30T15:12:01.279526"
    }
}
```

## 错误处理

### 常见错误码

- `200`: 成功
- `400`: 请求参数错误
- `404`: 资源不存在
- `500`: 服务器内部错误

### 错误响应示例

```json
{
    "code": 400,
    "message": "版本不存在: 999",
    "data": null,
    "timestamp": 1748589121233
}
```

### 参数验证错误

```json
{
    "code": 400,
    "message": "状态值必须在0-2之间",
    "data": null,
    "timestamp": 1748589217400
}
```

## 数据模型

### AppVersionDto
```json
{
    "id": 1,
    "appId": "com.unifysign.android",
    "appName": "桂云签",
    "packageName": "cn.org.bjca.signet.unify.app",
    "versionCode": 56,
    "versionName": "1.1.8001",
    "fileSize": 78083108,
    "md5": "d96f67fb6e2c8041cba9896ce7cbd8cb",
    "apkPath": "com.unifysign.android-56.apk",
    "downloadUrl": "http://localhost:8080/api/app/download/com.unifysign.android-56.apk",
    "updateDescription": "UnifySign安全签名工具v1.1.8001正式版",
    "forceUpdate": false,
    "status": 1,
    "statusDescription": "启用",
    "createTime": "2025-05-30T12:59:45.14922",
    "updateTime": "2025-05-30T12:59:45.149232"
}
```

## 测试脚本

项目提供了两个测试脚本：

1. `test_all_admin_apis.sh` - 全面的API功能测试
2. `test_final_admin_apis.sh` - 最终验证测试

运行测试：
```bash
chmod +x test_final_admin_apis.sh
./test_final_admin_apis.sh
```

## 开发状态

✅ **已完成功能**:
- 应用列表查询（支持搜索、分页、排序）
- 应用版本列表查询
- 版本信息修改
- 版本状态更新（禁用/启用/测试）
- 版本删除（单个/批量）
- 统计信息查询
- 完整的参数验证和错误处理

🚧 **待开发功能**:
- API密钥认证（第14个任务）
- 更多高级查询功能
- 版本发布流程管理

## 部署说明

- **JDK版本**: 11+
- **Spring Boot版本**: 2.7.18
- **数据库**: H2（开发环境）
- **端口**: 8080
- **APK存储**: ../apk_uploads/

服务器启动命令：
```bash
cd Server
mvn spring-boot:run
``` 