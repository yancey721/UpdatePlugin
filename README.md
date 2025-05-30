# Android 应用更新系统

一个完整的Android应用内更新解决方案，包含服务端API、管理界面和Android SDK。

## 🏗️ 项目结构

```
UpdatePlugin/
├── Server/           # 🖥️  Spring Boot 服务端
├── H5/              # 🌐 Vue3 管理界面 (计划中)
├── Android/         # 📱 Android SDK (计划中)
├── apk_uploads/     # 📦 APK文件存储
└── docs/            # 📚 技术文档
```

## 🚀 快速开始

### 1. 启动服务端
```bash
cd Server
mvn spring-boot:run
```

### 2. 运行测试
```bash
# 一键测试脚本
./docs/2-技术规范/快速测试脚本.sh
```

### 3. 查看文档
- [服务端运行调试指南](docs/2-技术规范/服务端运行调试指南.md)
- [技术规范总览](docs/2-技术规范/README.md)

## 🔧 技术栈

- **后端**: Spring Boot 2.7.18 + H2/MySQL + Maven
- **前端**: Vue3 + TypeScript + Element Plus (计划中)
- **移动端**: Kotlin + AAR + OkHttp (计划中)

## 📋 功能特性

### ✅ 已完成
- APK文件上传和解析
- 版本信息管理
- 文件存储服务
- 数据库设计
- API安全验证

### 🔄 开发中
- 版本检查API
- APK下载服务
- 管理界面
- Android SDK

## 🧪 测试接口

```bash
# 上传APK
curl -X POST \
  -H "X-API-Key: your-secret-api-key" \
  -F "apkFile=@your-app.apk" \
  -F "appId=com.your.app" \
  -F "updateDescription=版本更新说明" \
  http://localhost:8080/api/admin/app/upload
```

## 🔑 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 服务端口 | 8080 | HTTP服务端口 |
| API密钥 | `your-secret-api-key` | 管理API访问密钥 |
| 上传目录 | `../apk_uploads/` | APK文件存储路径 |
| 数据库 | H2 | 开发环境数据库 |

## 📞 技术支持

遇到问题请查看：
1. [技术文档](docs/2-技术规范/)
2. [运行调试指南](docs/2-技术规范/服务端运行调试指南.md)
3. 应用日志输出

---

**作者**: dongshiqian  
**版本**: v0.0.1-SNAPSHOT  
**更新**: 2025-05-30 