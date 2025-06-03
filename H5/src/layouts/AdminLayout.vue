<template>
  <div class="admin-layout">
    <el-container class="min-h-screen">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="flex items-center justify-between h-full">
          <div class="flex items-center">
            <el-icon class="text-2xl mr-3 text-blue-600"><Monitor /></el-icon>
            <h1 class="text-xl font-bold text-gray-800">应用更新管理系统</h1>
          </div>
          
          <div class="flex items-center space-x-4">
            <el-button
              type="text"
              @click="refreshData"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            
            <el-dropdown @command="handleCommand">
              <span class="flex items-center cursor-pointer text-gray-600 hover:text-gray-800">
                <el-icon class="mr-2"><User /></el-icon>
                管理员
                <el-icon class="ml-1"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            router
            class="el-menu-vertical"
            background-color="#f8f9fa"
            text-color="#495057"
            active-text-color="#007bff"
          >
            <el-menu-item index="/admin/apps">
              <el-icon><Grid /></el-icon>
              <span>应用列表</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区域 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor,
  User,
  ArrowDown,
  SwitchButton,
  Refresh,
  Grid
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

const handleCommand = async (command: string) => {
  switch (command) {
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消操作
  }
}

const refreshData = () => {
  loading.value = true
  // 触发当前页面的数据刷新
  setTimeout(() => {
    loading.value = false
    ElMessage.success('数据已刷新')
  }, 1000)
}

onMounted(() => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  background-color: #f5f5f5;
}

.header {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
}

.sidebar {
  background: #f8f9fa;
  border-right: 1px solid #e5e7eb;
}

.main-content {
  background: white;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.el-menu-vertical {
  border-right: none;
}
</style> 