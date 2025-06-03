import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: {
      title: '管理后台',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        redirect: '/admin/apps'
      },
      {
        path: 'apps',
        name: 'AppList',
        component: () => import('../views/AppListView.vue'),
        meta: {
          title: '应用列表',
          requiresAuth: true
        }
      },
      {
        path: 'app/:appId/versions',
        name: 'AppVersions',
        component: () => import('../views/AppVersionView.vue'),
        meta: {
          title: '版本管理',
          requiresAuth: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const apiKey = localStorage.getItem('apiKey')
  
  if (to.meta.requiresAuth && !apiKey) {
    // 需要认证但没有API Key，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && apiKey) {
    // 已登录用户访问登录页，跳转到应用列表
    next('/admin/apps')
  } else {
    next()
  }
})

export default router 