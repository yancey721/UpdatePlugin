# 前端技术规范 (@H5)

## 1. 技术栈选型

- **框架**: Vue 3
  - 使用 Composition API 和 `<script setup>` 语法糖。
- **语言**: TypeScript
- **构建工具**: Vite
- **UI 组件库**: Element Plus (完整引入或按需引入，根据项目打包大小考虑)
- **HTTP 请求**: Axios (封装实例，统一处理请求/响应拦截、错误处理)
- **状态管理**: Pinia (用于管理全局状态，如当前操作的应用信息、用户登录状态等，如果需要)
- **路由**: Vue Router
- **代码规范**: ESLint + Prettier (建议配置，保证代码风格统一)

## 2. 项目结构 (推荐)

```
@H5/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/             # API请求模块 (按模块划分，如 app.ts, version.ts)
│   ├── assets/          # 静态资源 (图片、全局SCSS变量等)
│   ├── components/      # 全局公共组件
│   │   └── UploadDialog.vue
│   │   └── EditVersionDialog.vue 
│   ├── layouts/         # 布局组件 (如 AdminLayout.vue 包含头部、侧边栏、内容区)
│   ├── router/          # 路由配置 (index.ts)
│   ├── store/           # Pinia状态管理 (index.ts, modules/)
│   ├── types/           # TypeScript类型定义 (api.d.ts, components.d.ts)
│   ├── utils/           # 工具函数 (request.ts, helpers.ts)
│   ├── views/           # 页面级组件
│   │   ├── AppList.vue      # 应用列表页面
│   │   ├── VersionManagement.vue # 版本管理页面 (某个应用的)
│   │   └── LoginPage.vue    # 登录页面 (如果需要管理端认证)
│   ├── App.vue          # 根组件
│   ├── main.ts          # 应用入口文件
│   └── shims-vue.d.ts   # Vue SFC 类型声明
├── .env.development     # 开发环境变量
├── .env.production      # 生产环境变量
├── .eslintrc.cjs
├── .gitignore
├── index.html
├── package.json
├── postcss.config.js
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 3. 核心页面与组件设计

### 3.1 登录页面 (`views/LoginPage.vue`) (如果需要认证)
- 功能: 提供管理员登录入口。
- 实现: 简单的表单，调用登录API，成功后存储Token并跳转到应用列表页。

### 3.2 应用列表页面 (`views/AppList.vue`)
- **功能**:
  - 展示所有已创建的应用 (`app_info`) 列表。
  - 支持按应用名称 (`appName`) 或应用ID (`appId`) 搜索/筛选。
  - 分页显示。
  - 每行提供操作：点击进入该应用的 "版本管理页面"。
- **UI**: `el-table` 展示应用信息 (ID, AppID, 应用名称, 包名, 创建时间)。`el-input` 用于搜索，`el-pagination` 用于分页。
- **API调用**: `GET /api/admin/app/list`。

### 3.3 版本管理页面 (`views/VersionManagement.vue`)
- **路由**: 可能类似 `/app/:appInfoId/versions`，通过路由参数获取当前操作的 `appInfoId`。
- **功能**:
  - 页面顶部显示当前应用的基本信息 (如 AppID, 应用名称)。
  - 展示当前应用的所有版本列表 (`app_version`)。
  - 提供 "上传新版本" 按钮。
  - 版本列表每行提供操作：编辑版本信息、删除版本、启用/禁用版本。
  - 分页显示。
- **UI**: `el-table` 展示版本信息 (版本ID, 版本号, 版本名, 更新说明, 是否强制, 状态, 创建时间, 下载链接等)。相关操作使用 `el-button`，状态切换可使用 `el-switch`。
- **API调用**:
  - `GET /api/admin/app/{appInfoId}/versions` (获取版本列表)。
  - 点击 "上传新版本" 按钮时，打开 `UploadVersionDialog.vue`。
  - 点击 "编辑" 时，打开 `EditVersionDialog.vue`，并传入当前版本信息。
  - 点击 "删除" 时，调用 `DELETE /api/admin/app/version/{versionId}`。
  - 点击 "启用/禁用" 时，调用 `PUT /api/admin/app/version/{versionId}/status`。

### 3.4 上传新版本弹窗/组件 (`components/UploadVersionDialog.vue`)
- **触发**: 在版本管理页面点击 "上传新版本" 按钮。
- **功能**:
  - 允许用户选择APK文件进行上传。
  - 包含表单填写：
    - `appId`: String (通常自动填充当前应用ID，如果允许通过此组件创建全新应用，则可编辑)
    - `updateDescription`: String (多行文本输入)
    - `forceUpdate`: Boolean (`el-switch` 或 `el-checkbox`)
  - 文件上传成功后，服务端会解析APK，弹窗内可以考虑展示部分解析出的关键信息 (如包名, 版本号, 版本名)，供用户确认。
- **UI**: `el-dialog` 作为弹窗。`el-upload` (设置为手动上传模式) 用于文件选择和展示已选文件。`el-form` 用于填写版本相关信息。
- **API调用**: 点击确认上传后，将文件和表单数据通过 `FormData` 提交到 `POST /api/admin/app/upload`。
- **交互**: 上传中显示进度，上传成功或失败给出提示。

### 3.5 编辑版本信息弹窗/组件 (`components/EditVersionDialog.vue`)
- **触发**: 在版本管理页面点击某个版本的 "编辑" 按钮。
- **功能**: 允许用户修改已上传版本的某些信息。
  - 表单编辑：
    - `updateDescription`: String
    - `forceUpdate`: Boolean
- **UI**: `el-dialog` 作为弹窗。`el-form` 用于编辑。
- **API调用**: 点击确认修改后，提交表单数据到 `PUT /api/admin/app/version/{versionId}`。
- **交互**: 修改成功或失败给出提示。

## 4. API 请求封装 (`src/utils/request.ts`)

建议创建一个 Axios 实例，并进行统一配置：
```typescript
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 从 .env 文件读取API基础路径
  timeout: 10000, // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 可以在这里添加Token到请求头 (如果需要认证)
    // const token = localStorage.getItem('token');
    // if (token && config.headers) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    // 假设服务端返回的 code 为 0 表示成功
    if (res.code !== 0) {
      ElMessage.error(res.message || 'Error');
      // 可以根据具体业务错误码进行特定处理
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res.data; // 返回 res.data，而不是整个response
  },
  (error) => {
    ElMessage.error(error.message || 'Request Error');
    return Promise.reject(error);
  }
);

export default service;
```
在 `api` 目录下的各模块中使用此 `service` 实例发起请求。

## 5. 状态管理 (Pinia - `src/store/`)

如果需要全局共享状态，例如：
- 当前正在管理的应用的 `appInfoId` 和 `appName` (在版本管理页面使用)。
- 用户登录信息和权限 (如果实现登录功能)。

可以创建相应的 `store`模块：
```typescript
// src/store/modules/app.ts (示例)
import { defineStore } from 'pinia';

interface AppState {
  currentAppInfoId: number | null;
  currentAppName: string | null;
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    currentAppInfoId: null,
    currentAppName: null,
  }),
  actions: {
    setCurrentApp(appInfoId: number, appName: string) {
      this.currentAppInfoId = appInfoId;
      this.currentAppName = appName;
    },
    clearCurrentApp() {
      this.currentAppInfoId = null;
      this.currentAppName = null;
    },
  },
});
```

## 6. 路由配置 (`src/router/index.ts`)

定义应用的路由规则：
```typescript
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import AppList from '../views/AppList.vue';
import VersionManagement from '../views/VersionManagement.vue';
// import LoginPage from '../views/LoginPage.vue'; // 如果有登录页
// import AdminLayout from '../layouts/AdminLayout.vue'; // 如果有统一布局

const routes: Array<RouteRecordRaw> = [
  // {
  //   path: '/login',
  //   name: 'Login',
  //   component: LoginPage,
  // },
  {
    path: '/', // 或者用一个布局组件包裹后台页面
    redirect: '/apps',
    // component: AdminLayout, // 示例布局
    // children: [
      // {
      //   path: '', 
      //   redirect: '/apps'
      // },
      {
        path: 'apps',
        name: 'AppList',
        component: AppList,
        meta: { title: '应用列表' }, // 可选，用于面包屑或标签页标题
      },
      {
        path: 'app/:appInfoId/versions',
        name: 'VersionManagement',
        component: VersionManagement,
        props: true, // 将路由参数 :appInfoId作为props传递给组件
        meta: { title: '版本管理' }, 
      },
    // ],
  },
  // 其他路由...
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫 (如果需要登录校验)
// router.beforeEach((to, from, next) => {
//   const token = localStorage.getItem('token');
//   if (to.name !== 'Login' && !token) {
//     next({ name: 'Login' });
//   } else {
//     next();
//   }
// });

export default router;
```

## 7. 环境变量 (`.env.*`)

- `.env.development`:
  ```
  VITE_APP_TITLE = App Update Management (Dev)
  VITE_API_BASE_URL = /api # 开发时可通过Vite proxy代理到后端
  ```
- `.env.production`:
  ```
  VITE_APP_TITLE = App Update Management
  VITE_API_BASE_URL = http://your-production-server.com/api # 生产环境后端API地址
  ```
在 `vite.config.ts` 中配置开发时的代理：
```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 开发时后端服务地址
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果后端接口没有 /api 前缀
      },
    },
  },
});
```

## 8. 构建与部署

- **构建**: `npm run build` 或 `yarn build`，生成静态文件到 `dist` 目录。
- **部署**: 将 `dist` 目录下的所有文件部署到任何静态文件服务器 (如 Nginx, Apache, GitHub Pages, Vercel, Netlify 等)。
  - 如果使用Nginx，需要配置history模式路由的重定向：
    ```nginx
    location / {
      try_files $uri $uri/ /index.html;
    }
    ```

确保服务端已正确配置CORS，允许前端域名的跨域请求，特别是在开发环境和生产环境前端域名与后端API域名不一致时。 