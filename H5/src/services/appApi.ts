import api from './api'
import type { ApiResponse } from './api'
import type { AppInfo, AppVersion, PageResponse } from '../stores/app'
import axios from 'axios'

// 验证API密钥
export const validateApiKey = async (apiKey: string, serverUrl?: string): Promise<boolean> => {
  try {
    console.log('开始验证API密钥...')
    
    const baseURL = serverUrl || 'http://localhost:8080'
    
    // 临时创建一个axios实例来测试API密钥
    const testApi = axios.create({
      baseURL,
      timeout: 10000
    })
    
    // 先测试连接性（不需要API密钥）
    try {
      console.log('测试服务器连接...')
      await testApi.get('/api/public/ping')
      console.log('服务器连接正常')
    } catch (connectionError: any) {
      console.error('服务器连接失败:', connectionError.message)
      throw new Error('无法连接到服务器，请检查服务器地址和网络连接')
    }
    
    // 再测试API密钥验证
    console.log('验证API密钥...')
    const response = await testApi.get<ApiResponse<any>>('/api/admin/app/stats', {
      headers: {
        'X-API-KEY': apiKey
      }
    })
    
    console.log('API密钥验证成功')
    return response.data.code === 200
  } catch (error: any) {
    console.error('API密钥验证失败:', error.message)
    
    // 如果已经抛出了连接错误，直接重新抛出
    if (error.message.includes('无法连接到服务器')) {
      throw error
    }
    
    // 如果是401或403错误，说明API密钥无效
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.error('API密钥无效，状态码:', error.response.status)
      return false
    }
    
    // 其他错误也返回false
    console.error('其他验证错误:', error.response?.status || 'unknown')
    return false
  }
}

// 查询应用列表
export const getApps = async (params: {
  appNameQuery?: string
  page?: number
  size?: number
  sort?: string
}): Promise<PageResponse<AppInfo>> => {
  const response = await api.get<ApiResponse<PageResponse<AppInfo>>>('/api/admin/app/apps', {
    params: {
      appNameQuery: params.appNameQuery,
      page: params.page || 0,
      size: params.size || 10,
      sort: params.sort || 'createTime,desc'
    }
  })
  return response.data.data
}

// 查询应用版本列表
export const getAppVersions = async (
  appId: string,
  params: {
    page?: number
    size?: number
    sort?: string
  }
): Promise<PageResponse<AppVersion>> => {
  const response = await api.get<ApiResponse<PageResponse<AppVersion>>>(
    `/api/admin/app/app/${appId}/versions`,
    {
      params: {
        page: params.page || 0,
        size: params.size || 10,
        sort: params.sort || 'versionCode,desc'
      }
    }
  )
  return response.data.data
}

// 上传APK文件
export const uploadApk = async (formData: FormData): Promise<AppVersion> => {
  const response = await api.post<ApiResponse<AppVersion>>('/api/admin/app/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data.data
}

// 更新应用版本信息
export const updateAppVersion = async (
  versionId: number,
  data: {
    updateDescription?: string
    forceUpdate?: boolean
    status?: number
  }
): Promise<AppVersion> => {
  const response = await api.put<ApiResponse<AppVersion>>(
    `/api/admin/app/version/${versionId}`,
    data
  )
  return response.data.data
}

// 删除应用版本
export const deleteAppVersion = async (versionId: number, forceDelete = true): Promise<void> => {
  await api.delete(`/api/admin/app/version/${versionId}`, {
    params: { forceDelete }
  })
}

// 更新版本状态
export const updateVersionStatus = async (
  versionId: number,
  status: number
): Promise<AppVersion> => {
  const response = await api.put<ApiResponse<AppVersion>>(
    `/api/admin/app/version/${versionId}/status`,
    { status }
  )
  return response.data.data
}

// 获取统计信息
export const getStats = async (): Promise<{
  totalApps: number
  totalVersions: number
  totalSize: number
}> => {
  const response = await api.get<ApiResponse<{
    totalApps: number
    totalVersions: number
    totalSize: number
  }>>('/api/admin/app/stats')
  return response.data.data
} 