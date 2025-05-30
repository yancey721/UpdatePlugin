# 技术规范文档

## 📚 文档列表

### 🔧 运行调试
- **[服务端运行调试指南](./服务端运行调试指南.md)** - 完整的服务端运行、配置和调试指南
- **[快速测试脚本](./快速测试脚本.sh)** - 一键自动化测试脚本

### 🧪 接口测试
- **[Postman测试集合](./Android更新系统API测试.postman_collection.json)** - 完整的API测试用例集合

## 🚀 快速开始

### 对于技术小白
1. **运行自动化测试**:
   ```bash
   cd UpdatePlugin
   ./docs/2-技术规范/快速测试脚本.sh
   ```

2. **查看详细指南**: 阅读 [服务端运行调试指南](./服务端运行调试指南.md)

### 对于开发者
1. **启动服务**: `cd Server && mvn spring-boot:run`
2. **导入Postman集合**: 使用 `Android更新系统API测试.postman_collection.json`
3. **开始开发**: 参考运行调试指南中的开发技巧

## 📋 核心配置

### 🏗️ 服务端源码工程结构

```
Server/                                    # 服务端根目录
├── pom.xml                               # Maven项目配置文件
├── src/main/                             # 源码主目录
│   ├── java/com/dongshiqian/appupdate/   # Java源码包
│   │   ├── AppUpdateServerApplication.java  # Spring Boot主启动类
│   │   ├── controller/                   # 控制器层 (REST API)
│   │   │   └── AdminAppController.java   # 管理端APK上传控制器
│   │   ├── entity/                       # 实体层 (JPA实体)
│   │   │   ├── AppInfo.java             # 应用信息实体
│   │   │   └── AppVersion.java          # 应用版本实体
│   │   ├── repository/                   # 数据访问层 (JPA Repository)
│   │   │   ├── AppInfoRepository.java    # 应用信息数据访问
│   │   │   └── AppVersionRepository.java # 应用版本数据访问
│   │   ├── service/                      # 业务逻辑层
│   │   │   ├── AppVersionService.java    # 版本管理服务
│   │   │   ├── ApkParserService.java     # APK解析服务
│   │   │   └── FileStorageService.java   # 文件存储服务
│   │   ├── dto/                          # 数据传输对象
│   │   │   ├── ApiResponse.java          # 统一API响应格式
│   │   │   ├── AppVersionDto.java        # 版本信息DTO
│   │   │   └── ParsedApkData.java        # APK解析数据DTO
│   │   ├── config/                       # 配置类
│   │   │   ├── AppProperties.java        # 应用配置属性
│   │   │   └── AppConfigValidator.java   # 配置验证器
│   │   ├── exception/                    # 异常定义
│   │   │   ├── BusinessException.java    # 业务异常
│   │   │   ├── ApkParseException.java    # APK解析异常
│   │   │   └── FileStorageException.java # 文件存储异常
│   │   └── util/                         # 工具类 (当前为空)
│   └── resources/                        # 资源文件
│       └── application.yml               # Spring Boot配置文件
├── database/                             # H2数据库文件目录 (运行时生成)
│   └── app_update_db.mv.db              # H2数据库文件
├── apk_uploads/                          # APK文件存储目录 (运行时生成)
└── target/                               # Maven构建输出目录
```

### 🔧 架构说明

#### 分层架构
- **Controller层**: 处理HTTP请求，参数验证，调用Service层
- **Service层**: 业务逻辑处理，事务管理
- **Repository层**: 数据访问，JPA查询
- **Entity层**: 数据库实体映射
- **DTO层**: 数据传输对象，API输入输出
- **Config层**: 配置管理，启动验证
- **Exception层**: 自定义异常定义

#### 核心组件
- **AppVersionService**: 版本管理核心服务，处理APK上传、解析、存储
- **ApkParserService**: APK文件解析，提取应用信息
- **FileStorageService**: 文件存储管理，APK文件操作
- **AppProperties**: 配置属性管理，支持环境变量
- **AppConfigValidator**: 启动时配置验证

#### 数据模型
- **AppInfo**: 应用基本信息 (appId, appName, packageName)
- **AppVersion**: 版本详细信息 (versionCode, versionName, apkPath, 更新说明等)

#### 技术栈
- **框架**: Spring Boot 2.7.18
- **数据库**: H2 (开发) / MySQL (生产)
- **ORM**: Spring Data JPA + Hibernate
- **构建工具**: Maven 3.6+
- **Java版本**: Java 11+
- **APK解析**: net.dongliu:apk-parser 2.6.10
- **日志**: SLF4J + Logback
- **配置**: YAML + 环境变量支持

#### 设计模式
- **分层架构**: Controller-Service-Repository模式
- **依赖注入**: Spring IoC容器管理
- **配置管理**: @ConfigurationProperties + 环境变量
- **异常处理**: 统一异常处理机制
- **数据传输**: DTO模式避免实体直接暴露
- **文件存储**: 策略模式支持多种存储方式
- **事务管理**: Spring声明式事务

#### 代码统计
| 模块 | 文件数 | 行数 | 说明 |
|------|--------|------|------|
| Controller | 1 | 63 | REST API控制器 |
| Entity | 2 | 203 | JPA实体类 |
| Repository | 2 | 221 | 数据访问层 |
| Service | 3 | 563 | 业务逻辑服务 |
| DTO | 3 | 259 | 数据传输对象 |
| Config | 2 | 206 | 配置管理类 |
| Exception | 3 | 57 | 自定义异常 |
| Main | 1 | 22 | 启动类 |
| **总计** | **17** | **1,594** | **Java源码** |

### 🔑 重要配置项
- **服务端口**: `8080` (可在application.yml中修改)
- **API密钥**: `your-secret-api-key` (必须修改为安全密钥)
- **数据库**: H2 (开发环境) / MySQL (生产环境)
- **文件存储**: `./apk_uploads/`

### 🔐 安全设置
```bash
# 设置生产环境API密钥
export ADMIN_API_KEY=your-production-secret-key

# 设置MySQL数据库
export DB_URL=jdbc:mysql://localhost:3306/app_update_db
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

## 🛠️ 故障排除

### 常见问题
1. **端口被占用**: `lsof -i :8080` 查看占用进程
2. **Maven未找到**: `brew install maven` 安装Maven
3. **Java版本不兼容**: 确保使用Java 11+
4. **API密钥错误**: 检查 `X-API-Key` 请求头

### 获取帮助
- 查看应用日志
- 检查 [服务端运行调试指南](./服务端运行调试指南.md) 中的常见问题解决
- 使用H2控制台查看数据库: http://localhost:8080/h2-console

## 📞 技术支持

如果遇到问题，请按以下顺序检查：
1. ✅ Java和Maven版本是否正确
2. ✅ 服务是否正常启动
3. ✅ 端口是否被占用
4. ✅ API密钥是否正确
5. ✅ 文件权限是否正确

更多详细信息请查看具体的技术文档。 