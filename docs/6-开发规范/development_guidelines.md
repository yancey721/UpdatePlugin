# 开发规范

## 1. 概述

本文档旨在为应用内更新系统的开发团队提供一套统一的开发规范，涵盖代码风格、版本控制、代码审查、文档编写等方面。遵守这些规范有助于提高代码质量、增强团队协作效率、降低维护成本。

## 2. 通用代码规范

- **语言**: 所有代码、注释、文档均使用**英文**。
- **命名**: 
    - 类名、接口名：PascalCase (e.g., `AppVersionController`, `UpdateService`)
    - 方法名、变量名：camelCase (e.g., `checkUpdate`, `latestVersionCode`)
    - 常量：UPPER_SNAKE_CASE (e.g., `MAX_FILE_SIZE`, `DEFAULT_TIMEOUT`)
    - 文件名和目录名：kebap-case (e.g., `app-version.entity.ts`, `user-service.java`) 或 snake_case (根据具体技术栈约定)。
- **格式化**: 
    - 使用统一的代码格式化工具 (如Prettier, Spotless, Checkstyle) 并配置到IDE中。
    - 遵循一致的缩进 (如4个空格或2个空格，团队统一) 和换行风格。
- **注释**: 
    - 对公共API、复杂逻辑、重要算法、非常规代码进行清晰、简洁的注释。
    - 避免过多不必要的注释。
    - JavaDoc / TSDoc / KDoc 等用于生成API文档的注释应规范编写。
- **代码简洁性**: 
    - 遵循DRY (Don't Repeat Yourself) 原则。
    - 保持函数/方法短小精悍，单一职责 (SRP)。
    - 避免过深的嵌套层级。
    - 使用有意义的变量名和函数名。
- **错误处理**: 
    - 对可能发生的错误进行妥善处理，提供明确的错误信息。
    - 不应忽略或隐藏异常。
    - 使用try-catch-finally处理可能抛出异常的代码块。
    - 服务端API应返回统一格式的错误响应。
- **日志记录**: 
    - 在关键路径、重要操作、错误发生时记录日志。
    - 日志信息应包含足够上下文，便于问题排查。
    - 合理使用日志级别 (ERROR, WARN, INFO, DEBUG, TRACE)。
    - 避免在日志中记录敏感信息 (如密码、Token)。

## 3. 服务端开发规范 (Java / Spring Boot)

- **遵循Spring Boot最佳实践**。
- **分层架构**: 清晰划分Controller, Service, Repository, Entity/DTO等层次。
- **依赖注入**: 推荐使用构造器注入。
- **DTO (Data Transfer Object)**: 用于Controller层与Service层、Service层之间的数据传输，避免直接暴露Entity。
- **异常处理**: 使用 `@ControllerAdvice` 和 `@ExceptionHandler` 进行全局异常处理。
- **校验**: 使用Bean Validation (`javax.validation` 或 `jakarta.validation`) 对请求参数进行校验。
- **API设计**: 
    - 遵循RESTful原则。
    - 使用合适的HTTP方法 (GET, POST, PUT, DELETE)。
    - 返回合适的HTTP状态码。
    - API版本控制 (如果需要，如 `/api/v1/...`)。
- **数据库操作**: 
    - 优先使用Spring Data JPA。
    - 避免直接编写原生SQL，除非性能优化等特殊场景。
    - 注意N+1查询问题。
    - 事务管理：合理使用 `@Transactional` 注解。
- **配置文件**: 
    - 使用 `application.yml` (或 `.properties`) 进行配置。
    - 敏感配置 (如数据库密码) 应通过环境变量或外部配置中心管理。
- **单元测试**: 使用JUnit 5 / Mockito对Service层和关键工具类进行单元测试。

## 4. 前端开发规范 (Vue 3 / TypeScript)

- **遵循Vue 3官方推荐实践**。
- **Composition API**: 优先使用Composition API (`<script setup>`)。
- **TypeScript**: 
    - 充分利用类型系统，为props, emits, ref, reactive, store state/actions等添加明确类型。
    - 使用 `interface` 定义对象结构和props类型。
- **组件化**: 
    - 将UI拆分为可复用的组件。
    - 组件命名：PascalCase (e.g., `AppCard.vue`, `ButtonPrimary.vue`)。
    - Props命名：camelCase。
    - Emits命名：kebab-case (DOM模板中) 或 camelCase (JS/TS中)。
- **状态管理 (Pinia)**:
    - 模块化Store。
    - State应尽可能扁平化。
    - Actions中处理异步逻辑和复杂的状态变更。
- **路由 (Vue Router)**:
    - 路由配置集中管理。
    - 使用路由元信息 (meta) 控制页面标题、权限等。
    - 路由懒加载优化性能。
- **API请求 (Axios)**:
    - 封装Axios实例，统一处理baseURL, timeout, interceptors (请求头、响应错误处理)。
    - API请求函数按模块组织。
- **样式**: 
    - 推荐使用SCSS。
    - 使用 `<style scoped>` 避免组件间样式污染。
    - 全局样式和变量统一管理。
- **代码检查与格式化**: 使用ESLint和Prettier，并集成到IDE和Git提交钩子中。
- **单元测试/组件测试**: 使用Vitest或Jest + Vue Test Utils对关键组件和Store进行测试。

## 5. 移动端SDK开发规范 (Android - Kotlin/Java)

- **遵循Android官方开发指南和最佳实践**。
- **语言**: 推荐使用Kotlin。
- **架构**: 考虑使用Clean Architecture或类似的清晰分层架构。
- **线程管理**: 
    - 网络请求、文件IO等耗时操作必须在子线程执行。
    - UI更新必须在主线程执行。
    - 合理使用Coroutines (Kotlin) 或RxJava/ThreadPools (Java)。
- **资源管理**: 
    - 注意内存泄漏，及时释放不再使用的资源 (如Bitmap, Cursor, BroadcastReceiver)。
    - 优化布局层级，避免过度绘制。
- **权限处理**: 
    - 明确请求必要的权限，并在 `AndroidManifest.xml` 中声明。
    - 运行时动态请求危险权限，并处理用户拒绝授权的情况。
- **API设计 (SDK内部)**: 设计简洁、易用、高内聚低耦合的API接口供App开发者调用。
- **错误处理与回调**: 为异步操作提供清晰的回调接口，包含成功、失败、进度等状态，并提供明确的错误码和错误信息。
- **日志**: SDK应提供可配置的日志开关和日志级别，方便App开发者调试。
- **兼容性**: 
    - 考虑不同Android版本API的差异。
    - 针对不同屏幕密度提供合适的资源。
- **安全性**: 
    - HTTPS通信。
    - 敏感数据不应硬编码或明文存储。
- **文档**: 为SDK的公共API提供清晰的KDoc/JavaDoc注释。

## 6. Git 版本控制规范

### 6.1 分支模型

推荐使用类似 Git Flow 的分支模型：

-   **`main` (或 `master`)**: 主分支，用于存放正式发布的稳定版本。只允许从 `release` 分支或 `hotfix` 分支合并。
-   **`develop`**: 开发主分支，汇集所有已完成的功能开发。作为新功能分支和 `release` 分支的基础。
-   **`feature/xxx`**: 功能分支，用于开发新功能。从 `develop` 分支创建，完成后合并回 `develop`。
    -   命名规范: `feature/issue-id-short-description` (e.g., `feature/TICKET-123-user-login`)
-   **`release/vx.y.z`**: 发布分支，用于准备新版本发布。从 `develop` 分支创建。在此分支上只进行Bug修复、文档生成等与发布相关的任务。
    -   命名规范: `release/v1.0.0`
    -   完成后，合并到 `main` (打Tag) 和 `develop` (同步修复)。
-   **`hotfix/vx.y.z-fix-description`**: 热修复分支，用于修复 `main` 分支上的紧急Bug。从 `main` 分支创建。
    -   命名规范: `hotfix/v1.0.1-login-bug`
    -   完成后，合并到 `main` (打Tag) 和 `develop` (同步修复)。

### 6.2 提交信息 (Commit Message)

遵循 Conventional Commits 规范是一个好选择，格式如下：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

-   **Type**: 
    -   `feat`: 新功能 (feature)
    -   `fix`: Bug修复
    -   `docs`: 文档变更
    -   `style`: 代码风格调整 (不影响代码含义的修改，如空格、格式化)
    -   `refactor`: 代码重构 (既不是新增功能，也不是修复bug)
    -   `perf`: 性能优化
    -   `test`: 增加或修改测试
    -   `build`: 影响构建系统或外部依赖的更改 (例如：gulp, broccoli, npm)
    -   `ci`: CI配置文件和脚本的更改 (例如：Travis, Circle, BrowserStack, SauceLabs)
    -   `chore`: 其他不修改 `src` 或 `test` 文件的更改 (如更新依赖库)
    -   `revert`: 撤销之前的commit
-   **Scope** (可选): 本次提交影响的范围，如模块名 (e.g., `api`, `ui`, `sdk`)
-   **Description**: 简短描述本次提交的目的，祈使句，首字母小写。
-   **Body** (可选): 更详细的说明。
-   **Footer** (可选): 通常用于BREAKING CHANGE说明或关闭Issue (e.g., `Closes #123`)

**示例:**
```
feat(api): add endpoint for user profile retrieval

Implement GET /api/users/profile to fetch current user data.

Closes #45
```

### 6.3 合并请求 (Pull Request / Merge Request)

-   所有向 `develop` 和 `main` 分支的合并都必须通过PR/MR。
-   PR/MR应包含清晰的描述，说明变更内容、目的和相关的Issue。
-   PR/MR至少需要一位其他团队成员审查通过后才能合并。
-   确保CI构建和自动化测试通过。

## 7. 代码审查 (Code Review)

- **目的**: 提高代码质量，发现潜在问题，知识共享，促进团队成员共同进步。
- **范围**: 所有提交到共享分支 (特别是 `develop`, `release`, `hotfix`) 的代码都应进行审查。
- **审查内容**: 
    - 功能是否按需求实现。
    - 是否存在逻辑错误或潜在Bug。
    - 是否遵循团队代码规范。
    - 代码是否清晰、可读、可维护。
    - 是否有必要的注释和文档。
    - 是否有合适的单元测试。
    - 是否存在性能或安全隐患。
- **审查者**: 
    - 应保持建设性和尊重的态度。
    - 提出具体、可操作的修改建议。
- **被审查者**: 
    - 虚心接受反馈，积极讨论和修改。

## 8. 文档编写规范

- **及时性**: 文档应与代码开发和变更保持同步更新。
- **准确性**: 文档内容必须准确反映系统实际情况。
- **清晰性**: 使用简洁明了的语言，结构清晰，易于理解。
- **完整性**: 覆盖必要的信息，如API接口文档应包含请求参数、响应格式、错误码等。
- **存储位置**: 所有项目相关文档统一存储在项目的 `docs/` 目录下，并纳入版本控制。
- **格式**: 推荐使用Markdown格式编写文档。
- **图表**: 对于架构图、流程图、ER图等，可以使用Mermaid、PlantUML等工具嵌入到Markdown中，或使用图片并提供源文件。

本文档将作为开发团队的共同约定，并根据项目进展和团队反馈持续改进。 