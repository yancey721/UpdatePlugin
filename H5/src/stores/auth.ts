import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const apiKey = ref<string>('')
  const isLoggedIn = ref<boolean>(false)

  // 初始化时从localStorage读取
  const initAuth = () => {
    const storedApiKey = localStorage.getItem('apiKey')
    if (storedApiKey) {
      apiKey.value = storedApiKey
      isLoggedIn.value = true
    }
  }

  // 登录
  const login = (key: string) => {
    apiKey.value = key
    isLoggedIn.value = true
    localStorage.setItem('apiKey', key)
  }

  // 登出
  const logout = () => {
    apiKey.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('apiKey')
  }

  return {
    apiKey,
    isLoggedIn,
    initAuth,
    login,
    logout
  }
}) 