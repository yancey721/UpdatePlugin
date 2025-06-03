# 应用内更新功能实施计划

## 概述
本计划旨在实现一个轻量级的应用内更新系统，主要服务于公司内部Android应用，核心目标是快速集成和满足基本的应用更新需求。系统包括以下组件：
- Java服务端应用（@Server）：提供更新检查、安装包管理API，采用JAR包直接运行方式。
- Vue3前端应用（@H5）：提供安装包上传与版本管理界面。
- 移动端SDK：集成到Android应用中实现版本检查和更新功能（保持现有详细设计）。

## 一、服务端应用（@Server）

### 1. 技术栈选型
- 框架：Spring Boot (内嵌Tomcat)
- 数据库：MySQL (生产环境推荐) / H2 (开发或简单部署时可选)
- 文件存储：本地文件系统
- 构建工具: Maven
- JDK: Java 8+

### 2. 数据库设计
```sql
-- 应用信息表 (app_info)
CREATE TABLE app_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(100) NOT NULL UNIQUE COMMENT '应用唯一标识 (例如：com.company.appname)',
    app_name VARCHAR(100) NOT NULL COMMENT '应用名称 (可由前端上传时填写或服务端从APK解析)',
    package_name VARCHAR(100) COMMENT '应用包名 (服务端从APK解析)',
    -- platform TINYINT DEFAULT 1 COMMENT '平台, 1-Android (未来可扩展)', 
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 应用版本表 (app_version)
CREATE TABLE app_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_info_id BIGINT NOT NULL COMMENT '关联app_info表的主键',
    version_code INT NOT NULL COMMENT '版本号 (从APK解析)',
    version_name VARCHAR(50) NOT NULL COMMENT '版本名称 (从APK解析)',
    file_size BIGINT COMMENT '文件大小(字节) (从APK解析)',
    md5 VARCHAR(32) COMMENT '文件MD5 (服务端计算或从APK解析)',
    apk_path VARCHAR(255) NOT NULL COMMENT 'APK文件在服务器的存储路径',
    download_url VARCHAR(255) COMMENT '完整的下载地址 (服务端拼接或直接存储)',
    update_description TEXT COMMENT '更新说明 (前端上传时填写)',
    force_update BOOLEAN DEFAULT FALSE COMMENT '是否强制更新 (前端上传时指定)',
    status INT DEFAULT 1 COMMENT '版本状态: 0-禁用, 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_info_id) REFERENCES app_info(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_version (app_info_id, version_code) -- 同一应用下版本号唯一
);
```
*备注：`app_name`, `package_name`, `version_code`, `version_name`, `file_size`, `md5` 等信息主要由服务端解析APK后填充。`appId` 在首次上传某应用APK时，如果系统中不存在，则会创建新的 `app_info` 记录。*

### 3. API接口设计

#### 3.1 移动端API
##### 检查更新
- 接口：`GET /api/app/check-update`
- 功能：客户端检查应用是否有新版本。
- 请求参数：
  - `appId`: String, 应用ID (如：com.company.appname)
  - `versionCode`: Integer, 当前应用版本号
  // - `packageName`: String, 包名 (可选, 用于校验) 
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": { // 更新信息，或null表示无更新或应用不存在
      "versionCode": 102,
      "versionName": "V1.0.2",
      "fileSize": 12582912,
      "md5": "f6e5d4c3b2a1f0e9d8c7b6a5b4c3d2e1",
      "downloadUrl": "http://your-server.com/api/app/download/123.apk", // 实际下载地址
      "updateDescription": "新增了XX功能，优化了性能。",
      "forceUpdate": true 
    }
  }
  ```
- 返回数据（无更新或失败）：
  ```json
  {
    "code": 1, // 或其他错误码
    "message": "已是最新版本或应用不存在" 
  }
  ```
##### 下载APK (示例)
- 接口：`GET /api/app/download/{fileName}` (或者基于版本ID的下载链接，由check-update返回)
- 功能：下载APK文件。服务端需要配置静态资源映射或通过Controller提供下载流。


#### 3.2 管理端API (@H5 使用)

##### 上传应用版本
- 接口：`POST /api/admin/app/upload`
- 功能：上传APK文件，服务端解析包信息，创建 `app_info` (如果不存在) 和 `app_version` 记录。
- Content-Type: `multipart/form-data`
- 请求参数：
  - `apkFile`: File, APK安装包文件 (必填)
  - `appId`: String, 应用ID (如：com.company.appname) (必填, 若此appId在app_info中不存在，则新建)
  - `updateDescription`: String, 版本更新说明 (可选)
  - `forceUpdate`: Boolean, 是否强制更新 (可选, 默认为 `false`)
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "上传成功",
    "data": { // 解析并保存后的版本信息
      "appId": "com.company.appname",
      "appName": "示例应用", 
      "packageName": "com.company.appname",
      "versionCode": 101,
      "versionName": "V1.0.1",
      // ... 其他app_version中的字段
    }
  }
  ```

##### 获取应用列表
- 接口：`GET /api/admin/app/list`
- 功能：获取所有已创建的应用信息 (`app_info`) 列表，支持分页。
- 请求参数：
  - `page`: Integer (可选, 默认为1)
  - `pageSize`: Integer (可选, 默认为10)
  - `appName`: String (可选, 用于按应用名称搜索)
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "查询成功",
    "data": {
      "total": 15,
      "list": [
        {
          "id": 1, // app_info.id
          "appId": "com.example.app1",
          "appName": "示例应用1",
          "packageName": "com.example.app1",
          "createTime": "2023-01-01 10:00:00"
        } 
        // ... more apps
      ]
    }
  }
  ```

##### 获取指定应用的版本列表
- 接口：`GET /api/admin/app/{appInfoId}/versions`
- 功能：获取指定应用 (`app_info_id`) 的所有版本列表 (`app_version`)，支持分页。
- 请求参数：
  - `appInfoId`: Long (路径参数, app_info表的主键)
  - `page`: Integer (可选, 默认为1)
  - `pageSize`: Integer (可选, 默认为10)
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "查询成功",
    "data": {
      "total": 5,
      "list": [
        {
          "id": 10, // app_version.id
          "versionCode": 101,
          "versionName": "V1.0.1",
          "updateDescription": "首次发布",
          "forceUpdate": false,
          "status": 1, // 1-启用, 0-禁用
          "createTime": "2023-01-02 11:00:00"
        }
        // ... more versions
      ]
    }
  }
  ```

##### 修改版本信息
- 接口：`PUT /api/admin/app/version/{versionId}`
- 功能：修改指定版本 (`app_version` 的主键 `id`) 的信息。
- 请求参数 (`application/json`):
  - `updateDescription`: String (可选)
  - `forceUpdate`: Boolean (可选)
  // - `status`: Integer (可选, 0-禁用, 1-启用) - 建议用单独接口修改状态
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "修改成功",
    "data": { // 修改后的版本信息
      "id": 10,
      "updateDescription": "修复了bug",
      "forceUpdate": true,
      // ...
    }
  }
  ```
  
##### 修改版本状态 (启用/禁用)
- 接口：`PUT /api/admin/app/version/{versionId}/status`
- 功能：修改指定版本 (`app_version` 的主键 `id`) 的状态。
- 请求参数 (`application/json`):
  - `status`: Integer (必填, 0-禁用, 1-启用)
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "状态修改成功"
  }
  ```

##### 删除版本
- 接口：`DELETE /api/admin/app/version/{versionId}`
- 功能：删除指定版本 (`app_version` 的主键 `id`)。同时应考虑删除服务器上的APK文件。
- 返回数据（成功）：
  ```json
  {
    "code": 0,
    "message": "删除成功"
  }
  ```

### 4. 核心功能实现
- **APK文件解析**：服务端使用如 `ApkParser` 库 (例如 `net.dongliu:apk-parser`) 或调用 `aapt` 工具解析APK，提取 `packageName`, `versionCode`, `versionName`, `appName` (应用名称), 应用图标等。
- **文件存储**：APK文件存储在服务器本地指定目录下。路径配置应灵活。
- **版本比对**：移动端检查更新时，服务端根据 `appId` 和 `versionCode` 查询 `app_version` 表中是否有更高版本号且状态为启用的记录。
- **数据库操作**：使用Spring Data JPA或MyBatis进行数据库操作。

## 二、前端应用（@H5）

### 1. 技术栈选型
- 框架：Vue 3 + TypeScript
- 构建工具：Vite
- UI组件库：Element Plus
- HTTP请求：Axios
- 状态管理：Pinia (可选, 根据项目复杂度)

### 2. 页面设计
- **应用列表页面 (`AppListPage.vue`)**:
  - 展示所有已创建的应用 (`app_info`) 列表 (表格形式: 应用ID, 应用名称, 包名, 创建时间)。
  - 提供搜索功能 (按应用名称)。
  - 每行提供操作：查看版本列表。
- **版本管理页面 (`VersionManagementPage.vue`)**:
  - 导航栏显示当前应用信息。
  - 展示当前应用的所版本列表 (`app_version`) (表格形式: 版本号, 版本名, 更新说明, 是否强制, 状态, 创建时间)。
  - 提供操作：
    - "上传新版本" 按钮，打开上传弹窗/页面。
    - 每行版本提供操作：编辑信息、删除版本、启用/禁用。
- **上传新版本弹窗/组件 (`UploadVersionDialog.vue`)**:
  - 文件上传控件 (`el-upload`) 选择APK文件。
  - 表单填写：`appId` (通常关联到当前应用，或允许手动输入创建新应用)、更新说明 (`updateDescription`)、是否强制更新 (`forceUpdate`)。
  - 上传成功后，解析并展示关键APK信息 (包名, 版本号, 版本名)。
- **编辑版本信息弹窗/组件 (`EditVersionDialog.vue`)**:
  - 表单编辑：更新说明、是否强制更新。

### 3. 核心功能实现
- **文件上传**：使用 `el-upload` 组件配合 `axios`，将APK文件和表单数据提交到服务端 `POST /api/admin/app/upload`接口。
- **版本管理**：
  - 调用 `GET /api/admin/app/list` 获取应用列表。
  - 调用 `GET /api/admin/app/{appInfoId}/versions` 获取版本列表。
  - 调用 `PUT /api/admin/app/version/{versionId}` 修改版本信息。
  - 调用 `PUT /api/admin/app/version/{versionId}/status` 修改版本状态。
  - 调用 `DELETE /api/admin/app/version/{versionId}` 删除版本。
- **数据显示与交互**：使用Element Plus组件构建用户界面，实现数据展示、表单提交、操作反馈等。

## 三、移动端集成
*(此部分保持 `docs/2-技术规范/mobile_integration_guide.md` 中的详细设计，这里仅作概述)*
- **SDK设计**: 轻量级、易集成、可配置。
- **核心模块**: `UpdateChecker`, `DownloadManager`, `UpdateUIHandler`, `UpdateConfig`.
- **集成步骤**: 添加依赖, 配置SDK (服务端URL, AppID), 调用检查更新API。
- **权限处理**: `INTERNET`, `WRITE_EXTERNAL_STORAGE` (根据目标SDK和Android版本), `REQUEST_INSTALL_PACKAGES` (Android 8.0+)。
- **API调用**: 调用服务端 `GET /api/app/check-update` 接口。

## 四、实施阶段规划 (简化版)

### 第一阶段：服务端核心API与前端上传功能（2-3周）
1. 服务端项目初始化: Spring Boot, 数据库表结构。
2. 服务端核心API实现:
   - APK解析逻辑。
   - 文件上传接口 (`/api/admin/app/upload`) 并保存信息。
   - 检查更新接口 (`/api/app/check-update`)。
   - 文件下载接口。
3. 前端 `@H5` 项目初始化: Vue3 + Vite + Element Plus。
4. 前端 `@H5` 核心页面:
   - APK上传页面/弹窗实现 (调用上传接口, 展示解析信息)。

### 第二阶段：前端版本管理与移动端SDK集成（2-3周）
1. 服务端API完善:
   - 应用列表查询接口。
   - 版本列表查询接口。
   - 版本修改、删除、状态切换接口。
2. 前端 `@H5` 功能完善:
   - 应用列表页面。
   - 版本管理页面 (列表展示, 调用修改/删除/状态接口)。
3. Android SDK核心功能开发与测试 (基于 `mobile_integration_guide.md`):
   - 检查更新模块。
   - 下载和安装模块。

### 第三阶段：联调测试与部署（1-2周）
1. 前后端联调测试。
2. 移动端与服务端联调测试。
3. 服务端部署测试 (JAR包运行)。
4. 文档完善。

## 五、注意事项与风险管理

### 安全性考虑 (简化)
- **接口访问**: 管理端API (`/api/admin/*`) 应考虑增加基础的认证机制 (如HTTP Basic Auth或简单的Token机制)，因为涉及版本管理。移动端API可相对开放。
- **文件上传**: 限制上传文件类型为APK，进行基本的安全扫描 (如果条件允许)。
- **本地文件存储**: 确保APK存储目录的权限安全。

### 部署与运维
- **服务端部署**: 提供清晰的JAR包运行指南，包括如何指定配置文件、日志路径、APK存储路径等。
  ```bash
  # 示例运行命令
  java -jar app-update-server.jar --spring.config.location=./application.yml --logging.file.path=./logs --app.upload-path=./apks
  ```
- **数据库备份**: 定期备份数据库。
- **日志监控**: 监控服务端运行日志，及时发现问题。

### 可能的风险
- **APK解析兼容性**: 不同工具或库对特殊APK的解析可能存在差异。
- **Android版本兼容性**: SDK需充分测试Android 6.0及以上各主流版本的兼容性。
- **存储空间**: 服务器本地存储空间管理。

## 六、未来扩展 (可选，当前版本不实现)
- iOS应用更新支持。
- 更细致的灰度发布功能。
 