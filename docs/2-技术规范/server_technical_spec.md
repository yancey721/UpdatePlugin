# 服务端技术规范

## 1. 技术栈选型

- **框架**：Spring Boot (内嵌Tomcat，方便JAR包直接运行)
- **语言**: Java 8+
- **构建工具**：Maven
- **数据库**：MySQL (生产环境推荐) / H2 (开发或简单内部署时可选)
- **ORM**：Spring Data JPA (或 MyBatis，根据团队熟悉度选择)
- **文件存储**：服务器本地文件系统
- **APK解析库**: 例如 `net.dongliu:apk-parser` (或通过命令行调用 `aapt`)

## 2. 开发环境配置 (推荐)

- **IDE**：IntelliJ IDEA
- **数据库管理工具**：DataGrip, Navicat, 或 DBeaver
- **版本控制**：Git

## 3. 数据库设计

与 `docs/APP更新功能实施计划.md` 中定义的数据库表结构一致。

```sql
-- 应用信息表 (app_info)
CREATE TABLE app_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(100) NOT NULL UNIQUE COMMENT '应用唯一标识 (例如：com.company.appname)',
    app_name VARCHAR(100) NOT NULL COMMENT '应用名称 (可由前端上传时填写或服务端从APK解析)',
    package_name VARCHAR(100) COMMENT '应用包名 (服务端从APK解析)',
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

## 4. API接口设计

所有API接口建议以 `/api` 作为基础路径。
管理端接口建议以 `/api/admin` 作为前缀，并考虑增加安全校验（如HTTP Basic Auth或简单Token）。

### 4.1 移动端API

#### 4.1.1 检查更新
- **接口**：`GET /api/app/check-update`
- **功能**：客户端检查应用是否有新版本。优先返回状态为"启用"的最新版本。
- **请求参数**：
  | 参数名        | 类型   | 是否必填 | 描述                                       |
  |---------------|--------|----------|--------------------------------------------|
  | `appId`       | String | 是       | 应用ID (例如：com.company.appname)          |
  | `versionCode` | Integer| 是       | 客户端当前安装的应用版本号                   |
- **成功响应 (HTTP 200)**：
  - **有更新**:
    ```json
    {
      "code": 0,
      "message": "发现新版本",
      "data": {
        "versionCode": 102,
        "versionName": "V1.0.2",
        "fileSize": 12582912, // 单位：字节
        "md5": "f6e5d4c3b2a1f0e9d8c7b6a5b4c3d2e1", // APK文件MD5，用于校验
        "downloadUrl": "http://your-server.com/api/app/download/appname-v1.0.2.apk", // 完整的APK下载地址
        "updateDescription": "1. 新增了XX功能\n2. 优化了性能体验\n3. 修复了若干已知问题",
        "forceUpdate": true // 或 false
      }
    }
    ```
  - **无更新或应用不存在**:
    ```json
    {
      "code": 1, // 自定义错误码，例如1表示无更新，2表示应用不存在等
      "message": "已是最新版本", // 或 "应用不存在"
      "data": null
    }
    ```
- **失败响应 (HTTP 4xx/5xx)**：返回标准错误信息结构。

#### 4.1.2 下载APK
- **接口**：`GET /api/app/download/{fileName}` (此路径可由检查更新接口返回的 `downloadUrl` 确定)
- **功能**：提供APK文件下载。
- **实现方式**：
    1.  **静态资源映射**：将APK存储目录配置为静态资源目录，由Web服务器直接处理下载请求。
    2.  **Controller下载**：通过Controller读取文件流并写入HTTP响应，可以更灵活控制下载过程（如限速、统计等）。
- **响应**: 文件流 `application/vnd.android.package-archive`。

### 4.2 管理端API (@H5 使用)

#### 4.2.1 上传应用版本
- **接口**：`POST /api/admin/app/upload`
- **功能**：上传APK文件。服务端接收后解析APK信息，如果 `appId` 对应的 `app_info` 不存在则创建，然后创建 `app_version` 记录，并将APK文件保存到本地指定目录。
- **Content-Type**: `multipart/form-data`
- **请求参数**：
  | 参数名             | 类型    | 是否必填 | 描述                                       |
  |--------------------|---------|----------|--------------------------------------------|
  | `apkFile`          | File    | 是       | APK安装包文件                              |
  | `appId`            | String  | 是       | 应用ID (例如：com.company.appname)。若不存在，则会基于此ID和解析出的appName创建新的`app_info`记录。 |
  | `updateDescription`| String  | 否       | 版本更新说明                               |
  | `forceUpdate`      | Boolean | 否       | 是否强制更新 (默认为 `false`)                |
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "上传成功",
    "data": { // 解析并保存后的版本信息，与app_version表结构对应
      "id": 25, // app_version.id
      "appInfoId": 5, // app_info.id
      "appId": "com.company.appname",
      "appName": "示例应用", // 从APK解析或基于appId的app_info中的appName
      "packageName": "com.company.appname", // 从APK解析
      "versionCode": 101,
      "versionName": "V1.0.1",
      "fileSize": 10485760,
      "md5": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
      "apkPath": "/path/to/apks/com.company.appname/101.apk",
      "downloadUrl": "http://your-server.com/api/app/download/com.company.appname-101.apk",
      "updateDescription": "首次发布版本。",
      "forceUpdate": false,
      "status": 1,
      "createTime": "2023-10-27T10:00:00"
    }
  }
  ```
- **失败响应 (HTTP 4xx/5xx)**：
  ```json
  {
    "code": 1001, // 例如：文件解析失败
    "message": "APK文件解析失败，请确认文件是否正确。"
  }
  ```

#### 4.2.2 获取应用列表
- **接口**：`GET /api/admin/app/list`
- **功能**：获取所有已创建的应用信息 (`app_info`) 列表，支持分页和按应用名称搜索。
- **请求参数**：
  | 参数名     | 类型    | 是否必填 | 描述             |
  |------------|---------|----------|------------------|
  | `page`     | Integer | 否       | 页码 (默认为 1)    |
  | `pageSize` | Integer | 否       | 每页数量 (默认为 10) |
  | `appName`  | String  | 否       | 应用名称模糊查询   |
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "查询成功",
    "data": {
      "total": 15, // 总记录数
      "list": [
        {
          "id": 1, // app_info.id
          "appId": "com.example.app1",
          "appName": "示例应用1",
          "packageName": "com.example.app1", // 从最近一个版本解析得到
          "createTime": "2023-01-01T10:00:00"
        },
        // ... more apps
      ]
    }
  }
  ```

#### 4.2.3 获取指定应用的版本列表
- **接口**：`GET /api/admin/app/{appInfoId}/versions`
- **功能**：获取指定应用 (`app_info` 表的 `id`) 的所有版本列表 (`app_version`)，按创建时间降序排列，支持分页。
- **路径参数**：
  - `appInfoId`: Long, `app_info` 表的主键 `id`。
- **请求参数**：
  | 参数名     | 类型    | 是否必填 | 描述             |
  |------------|---------|----------|------------------|
  | `page`     | Integer | 否       | 页码 (默认为 1)    |
  | `pageSize` | Integer | 否       | 每页数量 (默认为 10) |
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "查询成功",
    "data": {
      "total": 5, // 该应用下的版本总数
      "list": [
        {
          "id": 10, // app_version.id
          "versionCode": 101,
          "versionName": "V1.0.1",
          "fileSize": 10485760,
          "md5": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
          "updateDescription": "首次发布",
          "forceUpdate": false,
          "status": 1, // 1-启用, 0-禁用
          "createTime": "2023-01-02T11:00:00",
          "downloadUrl": "http://your-server.com/api/app/download/app1-v1.0.1.apk"
        },
        // ... more versions
      ]
    }
  }
  ```

#### 4.2.4 修改版本信息
- **接口**：`PUT /api/admin/app/version/{versionId}`
- **功能**：修改指定版本 (`app_version` 表的 `id`) 的信息。通常用于修改更新说明、是否强制更新。
- **路径参数**：
  - `versionId`: Long, `app_version` 表的主键 `id`。
- **请求体 (`application/json`)**：
  ```json
  {
    "updateDescription": "修复了严重的BUG，建议立即更新！",
    "forceUpdate": true
  }
  ```
  *只传递需要修改的字段。*
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "修改成功",
    "data": { // 修改后的完整版本信息
      "id": 10,
      "versionCode": 101,
      // ... 其他字段
      "updateDescription": "修复了严重的BUG，建议立即更新！",
      "forceUpdate": true
    }
  }
  ```

#### 4.2.5 修改版本状态 (启用/禁用)
- **接口**：`PUT /api/admin/app/version/{versionId}/status`
- **功能**：修改指定版本 (`app_version` 表的 `id`) 的状态 (启用或禁用)。禁用的版本不会被检查更新接口返回。
- **路径参数**：
  - `versionId`: Long, `app_version` 表的主键 `id`。
- **请求体 (`application/json`)**：
  ```json
  {
    "status": 0 // 0-禁用, 1-启用
  }
  ```
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "状态修改成功"
  }
  ```

#### 4.2.6 删除版本
- **接口**：`DELETE /api/admin/app/version/{versionId}`
- **功能**：删除指定版本 (`app_version` 表的 `id`) 的记录。同时应删除服务器上对应的APK文件。
- **路径参数**：
  - `versionId`: Long, `app_version` 表的主键 `id`。
- **成功响应 (HTTP 200)**：
  ```json
  {
    "code": 0,
    "message": "删除成功"
  }
  ```

## 5. 核心功能实现要点

- **APK文件解析**：
  - 服务端接收到上传的APK后，使用如 `net.dongliu:apk-parser` 等库解析。
  - 提取信息：`packageName`, `versionCode`, `versionName`, 应用名称 (`applicationLabel`)，应用图标 (`icon`) 等。
  - 计算文件MD5值。
- **文件存储**：
  - APK文件存储在服务器本地文件系统，路径可配置 (例如：`app.upload-path=/data/apks`)。
  - 建议按 `appId` 和 `versionCode` 或版本 `id` 组织存储目录，例如：`{upload-path}/{appId}/{versionCode}.apk`。
  - 生成 `downloadUrl` 时，应拼接服务器域名和端口，以及文件访问路径。
- **数据库操作**：
  - 使用Spring Data JPA或MyBatis简化数据库交互。
  - `app_info` 记录：当上传新 `appId` 的APK时，如果 `app_info` 中不存在该 `appId`，则自动创建一条记录。
- **错误处理**：定义统一的API响应格式，包含 `code`, `message`, `data`。错误码应有规划。
- **安全性**：管理端API (`/api/admin/*`) 必须进行权限校验。简单的方案可以考虑HTTP Basic Authentication或固定的Token。 

## 6. 部署 (JAR包直接运行)

### 6.1 打包
使用Maven将Spring Boot应用打包为可执行的JAR文件：
```bash
mvn clean package -DskipTests
```
生成的JAR文件通常在 `target/` 目录下。

### 6.2 运行
将打包好的JAR文件上传到服务器，通过以下命令运行：
```bash
java -jar your-app-name.jar
```

### 6.3 配置文件
Spring Boot应用通常使用 `application.properties` 或 `application.yml` 作为配置文件。
- **内置配置**：配置文件可以打包在JAR内部。
- **外置配置**：推荐使用外置配置文件，方便修改，无需重新打包。可以将配置文件与JAR放在同一目录，或通过参数指定：
  ```bash
  java -jar your-app-name.jar --spring.config.location=file:./application.yml
  # 或者，如果配置文件名为application.properties且在JAR同级目录，Spring Boot会自动加载
  ```

### 6.4 常用配置项示例 (`application.yml`)
```yaml
server:
  port: 8080 # 应用监听端口
  servlet:
    context-path: /api # API基础路径 (可选, 如果所有接口都以/api开头)

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app_update_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update # 开发时可选update或create-drop，生产环境建议validate或none，通过Flyway/Liquibase管理表结构
    show-sql: true
  # H2数据库配置 (开发时可选)
  # h2:
  #   console:
  #     enabled: true
  #     path: /h2-console
  # datasource:
  #   url: jdbc:h2:file:./data/appupdatedb;AUTO_SERVER=TRUE
  #   username: sa
  #   password:
  #   driver-class-name: org.h2.Driver

logging:
  level:
    root: INFO
    com.yourcompany: DEBUG # 自定义包路径日志级别
  file:
    name: ./logs/app-update.log # 日志文件路径

app:
  upload-path: ./apks/ # APK文件上传存储的基础路径
  server-base-url: http://your-server-domain.com # 服务器基础URL，用于拼接下载链接
```

### 6.5 后台运行与进程守护
在生产环境，应使用工具使Java应用在后台运行，并在意外退出时自动重启。
- **`nohup` 与 `&`** (简单方式):
  ```bash
  nohup java -jar your-app-name.jar > app.log 2>&1 &
  ```
- **`systemd` (推荐)**: 创建一个service文件来管理应用进程。
  示例 `/etc/systemd/system/app-update.service`:
  ```ini
  [Unit]
  Description=App Update Service
  After=network.target

  [Service]
  User=youruser # 运行应用的用户
  WorkingDirectory=/path/to/your/app # JAR包所在目录
  ExecStart=/usr/bin/java -jar your-app-name.jar --spring.config.location=file:./application.yml
  SuccessExitStatus=143
  TimeoutStopSec=10
  Restart=on-failure
  RestartSec=5

  [Install]
  WantedBy=multi-user.target
  ```
  然后使用 `sudo systemctl enable app-update` 和 `sudo systemctl start app-update`。

## 7. APK文件存储与下载URL生成

- **存储路径**: 例如，配置文件中 `app.upload-path: /data/uploads/apks`。
- 当上传 `com.example.app1` 的 `versionCode=101` 的APK时，文件可存储为 `/data/uploads/apks/com.example.app1/101/com.example.app1-101.apk`。
- **下载URL**: 例如，配置文件中 `app.server-base-url: http://updates.mycompany.com`。
- 服务端应能据此生成完整的下载URL：`http://updates.mycompany.com/downloads/com.example.app1/101/com.example.app1-101.apk`。
- 这需要服务端配置一个特定的路径 (如 `/downloads/**`) 来映射到APK存储的根目录，或通过Controller动态提供下载。 Spring Boot中可以通过 `WebMvcConfigurer` 的 `addResourceHandlers` 方法配置静态资源映射。 