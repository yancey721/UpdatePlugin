import api, { updateBaseURL } from './api'
import type { ApiResponse } from './api'

/**
 * 验证API密钥
 * @param apiKey API密钥
 * @param serverUrl 服务器地址（可选）
 * @returns 是否验证成功
 */
export const validateApiKey = async (apiKey: string, serverUrl?: string): Promise<boolean> => {
  try {
    // 如果提供了服务器地址，更新baseURL
    if (serverUrl) {
      updateBaseURL(serverUrl)
    }
    
    // 临时设置API密钥到请求头
    const response = await api.get<ApiResponse<string>>('/api/admin/app/ping', {
      headers: {
        'X-API-KEY': apiKey
      }
    })
    
    // 检查响应是否成功
    return response.data.code === 200
  } catch (error: any) {
    console.error('API密钥验证失败:', error)
    
    // 如果是401或403错误，说明密钥无效
    if (error.response?.status === 401 || error.response?.status === 403) {
      return false
    }
    
    // 其他错误也视为验证失败
    throw new Error(error.response?.data?.message || '服务器连接失败，请检查服务器地址')
  }
}

/**
 * 获取应用列表
 * @param page 页码
 * @param size 每页大小
 * @param appNameQuery 应用名称查询条件
 * @returns 应用列表
 */
export const getAppList = async (page: number = 0, size: number = 10, appNameQuery?: string) => {
  const params: any = { page, size }
  if (appNameQuery) {
    params.appNameQuery = appNameQuery
  }
  
  const response = await api.get<ApiResponse<any>>('/api/admin/app/apps', { params })
  return response.data
}

/**
 * 上传APK文件
 * @param file APK文件
 * @param appId 应用ID
 * @param updateDescription 更新说明
 * @param forceUpdate 是否强制更新
 * @returns 上传结果
 */
export const uploadApk = async (
  file: File, 
  appId: string, 
  updateDescription?: string, 
  forceUpdate: boolean = false
) => {
  const formData = new FormData()
  formData.append('apkFile', file)
  formData.append('appId', appId)
  if (updateDescription) {
    formData.append('updateDescription', updateDescription)
  }
  formData.append('forceUpdate', forceUpdate.toString())
  
  const response = await api.post<ApiResponse<any>>('/api/admin/app/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

/**
 * 获取应用版本列表
 * @param appId 应用ID
 * @param page 页码
 * @param size 每页大小
 * @returns 版本列表
 */
export const getAppVersions = async (appId: string, page: number = 0, size: number = 10) => {
  const response = await api.get<ApiResponse<any>>(`/api/admin/app/${appId}/versions`, {
    params: { page, size }
  })
  return response.data
}

/**
 * 更新版本状态
 * @param versionId 版本ID
 * @param status 状态（0-禁用，1-启用）
 * @returns 更新结果
 */
export const updateVersionStatus = async (versionId: number, status: number) => {
  const response = await api.put<ApiResponse<any>>(`/api/admin/app/version/${versionId}/status`, {
    status
  })
  return response.data
}

/**
 * 删除版本
 * @param versionId 版本ID
 * @returns 删除结果
 */
export const deleteVersion = async (versionId: number) => {
  const response = await api.delete<ApiResponse<any>>(`/api/admin/app/version/${versionId}`)
  return response.data
} 