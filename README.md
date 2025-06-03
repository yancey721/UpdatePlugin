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

## 📄 开源许可证

本项目基于 MIT License 开源许可证发布。

### MIT License

```
MIT License

Copyright (c) 2025 yancey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### 🤝 贡献说明

- 欢迎提交 Issue 和 Pull Request
- 代码贡献请遵循现有的代码风格
- 提交前请确保测试通过
- 重大功能变更请先创建 Issue 讨论

### ⭐ 如何使用

1. **Fork** 本项目
2. **Clone** 你的 fork 到本地
3. 创建 **feature 分支**
4. **提交** 你的修改
5. **推送** 到你的 fork
6. 创建 **Pull Request**

---

**作者**: yancey  
**版本**: v0.0.1-SNAPSHOT  
**更新**: 2025-05-30  
**许可证**: MIT License 