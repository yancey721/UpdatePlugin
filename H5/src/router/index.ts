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
    redirect: '/admin/apps'
  },
  {
    path: '/admin/apps',
    name: 'AppManagement',
    component: () => import('../views/AppManagementView.vue'),
    meta: {
      title: '应用管理',
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*', // Catch-all route for 404, redirect to login
    redirect: '/login'
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
    // 已登录用户访问登录页，跳转到应用管理页
    next('/admin/apps')
  } else {
    next()
  }
})

export default router 