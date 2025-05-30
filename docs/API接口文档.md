# 应用内更新系统 - 管理端API接口文档

## 概述

本文档描述了应用内更新系统的所有管理端API接口，包括应用版本的查询、创建、修改、删除和统计功能。

### 服务器信息
- **基础URL**: `http://localhost:8080/api/admin/app`
- **认证方式**: API密钥（待第14个任务实现）
- **数据格式**: JSON
- **字符编码**: UTF-8

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

## API接口列表

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