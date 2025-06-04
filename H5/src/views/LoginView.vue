<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- Logo和标题区域 -->
      <div class="header-section">
        <div class="logo-container">
          <el-icon class="lock-icon">
            <Lock />
          </el-icon>
        </div>
        <h1 class="main-title">应用更新管理系统</h1>
        <p class="sub-title">请输入API密钥登录</p>
      </div>
      
      <!-- 登录表单卡片 -->
      <div class="form-card">
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          @submit.prevent="handleLogin"
          class="login-form"
        >
          <!-- API密钥输入 -->
          <div class="form-group">
            <label class="form-label">API密钥</label>
            <el-input
              v-model="loginForm.apiKey"
              type="password"
              placeholder="请输入API密钥"
              size="large"
              show-password
              class="form-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Key />
                </el-icon>
              </template>
            </el-input>
          </div>
          
          <!-- 服务器地址输入 -->
          <div class="form-group">
            <label class="form-label">服务器地址 (可选)</label>
            <el-input
              v-model="loginForm.serverUrl"
              placeholder="http://localhost:8080"
              size="large"
              class="form-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Monitor />
                </el-icon>
              </template>
            </el-input>
          </div>
          
          <!-- 登录按钮 -->
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
            block
          >
            <el-icon class="button-icon" v-if="!loading">
              <Check />
            </el-icon>
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form>
      </div>
      
      <!-- 底部提示 -->
      <div class="tip-section">
        <div class="tip-content">
          <el-icon class="tip-icon">
            <Warning />
          </el-icon>
          <span class="tip-text">提示：API密钥在服务端配置文件中设置</span>
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
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.login-wrapper {
  width: 100%;
  max-width: 440px;
  margin: 0 auto;
}

/* Header Section */
.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.lock-icon {
  font-size: 36px;
  color: white;
}

.main-title {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.sub-title {
  font-size: 16px;
  color: #718096;
  margin: 0;
  font-weight: 400;
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 28px;
}

.form-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
  letter-spacing: -0.1px;
}

.form-input {
  width: 100%;
}

:deep(.form-input .el-input__wrapper) {
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
  padding: 16px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fafbfc;
}

:deep(.form-input .el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  background: white;
}

:deep(.form-input .el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

:deep(.form-input .el-input__inner) {
  font-size: 16px;
  color: #2d3748;
  font-weight: 500;
}

:deep(.form-input .el-input__inner::placeholder) {
  color: #a0aec0;
  font-weight: 400;
}

.input-icon {
  color: #9ca3af;
  font-size: 18px;
  margin-right: 4px;
}

/* Login Button */
.login-button {
  width: 100%;
  height: 56px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-top: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.button-icon {
  margin-right: 8px;
  font-size: 18px;
}

/* Tip Section */
.tip-section {
  text-align: center;
}

.tip-content {
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #fef3c7, #f59e0b);
  color: #92400e;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.2);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.tip-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #d97706;
}

.tip-text {
  color: #92400e;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }
  
  .form-card {
    padding: 32px 24px;
    border-radius: 20px;
  }
  
  .main-title {
    font-size: 28px;
  }
  
  .logo-container {
    width: 72px;
    height: 72px;
  }
  
  .lock-icon {
    font-size: 32px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .login-container {
    background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  }
  
  .main-title {
    color: #f7fafc;
  }
  
  .sub-title {
    color: #a0aec0;
  }
  
  .form-card {
    background: #2d3748;
    border-color: #4a5568;
  }
  
  .form-label {
    color: #f7fafc;
  }
  
  :deep(.form-input .el-input__wrapper) {
    background: #374151;
    border-color: #4a5568;
  }
  
  :deep(.form-input .el-input__inner) {
    color: #f7fafc;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-wrapper {
  animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-card {
  animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

.tip-section {
  animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1) 0.4s both;
}
</style> 