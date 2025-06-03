<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo和标题区域 -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-blue-600 rounded-lg flex items-center justify-center mb-4">
          <el-icon class="text-white text-2xl"><Lock /></el-icon>
        </div>
        <h2 class="text-3xl font-extrabold text-gray-900">
          应用更新管理系统
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          请输入API密钥登录
        </p>
      </div>
      
      <!-- 登录表单卡片 -->
      <el-card class="login-card shadow-lg">
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          @submit.prevent="handleLogin"
          label-position="top"
          class="space-y-4"
        >
          <!-- API密钥输入 -->
          <el-form-item label="API密钥" prop="apiKey">
            <el-input
              v-model="loginForm.apiKey"
              type="password"
              placeholder="请输入API密钥"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="text-gray-400"><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <!-- 服务器地址输入 -->
          <el-form-item label="服务器地址 (可选)" prop="serverUrl">
            <el-input
              v-model="loginForm.serverUrl"
              placeholder="http://localhost:8080"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="text-gray-400"><Monitor /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <!-- 登录按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="w-full login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <el-icon class="mr-2"><Check /></el-icon>
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 底部提示 -->
      <div class="text-center">
        <div class="flex items-center justify-center text-sm text-amber-600 bg-amber-50 px-4 py-3 rounded-lg">
          <el-icon class="mr-2"><Warning /></el-icon>
          <span>提示：API密钥在服务端配置文件中设置</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Key, Lock, Monitor, Check, Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { validateApiKey } from '../services/appApi'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  apiKey: '',
  serverUrl: 'http://localhost:8080'
})

const rules: FormRules = {
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' },
    { min: 10, message: 'API密钥长度不能少于10位', trigger: 'blur' }
  ],
  serverUrl: [
    { 
      pattern: /^https?:\/\/.+/, 
      message: '请输入有效的服务器地址', 
      trigger: 'blur' 
    }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 更新axios的baseURL（如果需要）
    if (loginForm.serverUrl && loginForm.serverUrl !== 'http://localhost:8080') {
      localStorage.setItem('serverUrl', loginForm.serverUrl)
    }
    
    // 真正的API密钥验证
    const isValidApiKey = await validateApiKey(loginForm.apiKey, loginForm.serverUrl)
    
    if (isValidApiKey) {
      authStore.login(loginForm.apiKey)
      ElMessage.success('登录成功')
      router.push('/admin/apps')
    } else {
      ElMessage.error('API密钥无效，请检查密钥是否正确')
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    // 显示具体的错误信息
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}

// 组件挂载时恢复保存的服务器地址
onMounted(() => {
  const savedServerUrl = localStorage.getItem('serverUrl')
  if (savedServerUrl) {
    loginForm.serverUrl = savedServerUrl
  }
})
</script>

<style scoped>
.login-card {
  border: none;
  border-radius: 12px;
}

.login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}
</style> 