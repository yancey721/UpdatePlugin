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
    // 已登录用户访问登录页，应该跳转到他们登录后应该去的页面
    // 由于其他页面已删除，暂时也跳转到 /login，或者可以考虑显示一个已登录提示
    // 为了简单起见，暂时不做重定向，允许已登录用户再次看到登录页
    // 若要强制跳转，可以设置一个登录后的默认页，比如 /dashboard (如果未来会添加)
    // next('/dashboard'); // 示例
    next() 
  } else {
    next()
  }
})

export default router 