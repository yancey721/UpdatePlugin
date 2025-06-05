import api, { updateBaseURL } from './api'
import type { ApiResponse } from './api'
import type { 
  AppInfo, 
  AppVersion, 
  AppInfoWithLatestVersion, 
  UpdateForceUpdateRequest, 
  CreateAppRequest,
  PageResponse 
} from '../types/app'

/**
 * 验证API密钥
 * @param apiKey API密钥
 * @param serverUrl 服务器地址（可选）
 * @returns 是否验证成功
 */
export const validateApiKey = async (apiKey: string, serverUrl?: string): Promise<boolean> => {
  // 预先验证API密钥
  if (!apiKey || apiKey.trim() === '') {
    throw new Error('请输入API密钥')
  }
  
  if (apiKey.trim().length < 10) {
    throw new Error('API密钥长度不能少于10位')
  }
  
  try {
    // 如果提供了服务器地址，更新baseURL
    if (serverUrl) {
      updateBaseURL(serverUrl)
    }
    
    // 临时设置API密钥到请求头
    const response = await api.get<ApiResponse<string>>('/api/admin/app/ping', {
      headers: {
        'X-API-KEY': apiKey.trim()
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
export const getAppList = async (
  page: number = 0, 
  size: number = 10, 
  appNameQuery?: string
): Promise<PageResponse<AppInfoWithLatestVersion>> => {
  const params: any = { page, size }
  if (appNameQuery) {
    params.appNameQuery = appNameQuery
  }
  
  const response = await api.get<ApiResponse<PageResponse<AppInfoWithLatestVersion>>>('/api/admin/app/apps', { params })
  return response.data.data
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
): Promise<AppVersion> => {
  const formData = new FormData()
  formData.append('apkFile', file)
  formData.append('appId', appId)
  if (updateDescription) {
    formData.append('updateDescription', updateDescription)
  }
  formData.append('forceUpdate', forceUpdate.toString())
  
  const response = await api.post<ApiResponse<AppVersion>>('/api/admin/app/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data.data
}

/**
 * 获取应用版本列表
 * @param appId 应用ID
 * @param page 页码
 * @param size 每页大小
 * @returns 版本列表
 */
export const getAppVersions = async (
  appId: string, 
  page: number = 0, 
  size: number = 10
): Promise<PageResponse<AppVersion>> => {
  const response = await api.get<ApiResponse<PageResponse<AppVersion>>>(`/api/admin/app/app/${appId}/versions`, {
    params: { page, size }
  })
  return response.data.data
}

// ===========================================
// 发布版本管理相关API（新增）
// ===========================================

/**
 * 设置发布版本
 * @param appId 应用ID
 * @param versionId 版本ID
 * @returns 设置结果
 */
export const setReleaseVersion = async (appId: string, versionId: number): Promise<AppVersion> => {
  const response = await api.put<ApiResponse<AppVersion>>(`/api/admin/app/${appId}/release-version/${versionId}`)
  return response.data.data
}

/**
 * 更新应用的强制更新设置
 * @param appId 应用ID
 * @param forceUpdate 是否强制更新
 * @returns 更新结果
 */
export const updateAppForceUpdate = async (appId: string, forceUpdate: boolean): Promise<AppInfo> => {
  const response = await api.put<ApiResponse<AppInfo>>(`/api/admin/app/${appId}/force-update`, {
    forceUpdate
  })
  return response.data.data
}

/**
 * 获取当前发布版本
 * @param appId 应用ID
 * @returns 当前发布版本信息
 */
export const getCurrentReleaseVersion = async (appId: string): Promise<AppVersion | null> => {
  const response = await api.get<ApiResponse<AppVersion | null>>(`/api/admin/app/${appId}/release-version`)
  return response.data.data
}

/**
 * 创建新应用
 * @param createRequest 创建应用请求
 * @returns 创建的应用信息
 */
export const createApp = async (createRequest: CreateAppRequest): Promise<AppInfo> => {
  const response = await api.post<ApiResponse<AppInfo>>('/api/admin/app/create', createRequest)
  return response.data.data
}

// ===========================================
// 兼容性方法（保留但标记为废弃）
// ===========================================

/**
 * 更新版本状态
 * @param versionId 版本ID
 * @param status 状态（0-禁用，1-启用）
 * @returns 更新结果
 * @deprecated 使用 setReleaseVersion 替代
 */
export const updateVersionStatus = async (versionId: number, status: number): Promise<AppVersion> => {
  const response = await api.put<ApiResponse<AppVersion>>(`/api/admin/app/version/${versionId}/status`, {
    status
  })
  return response.data.data
}

/**
 * 删除版本
 * @param versionId 版本ID
 * @returns 删除结果
 */
export const deleteVersion = async (versionId: number): Promise<void> => {
  const response = await api.delete<ApiResponse<void>>(`/api/admin/app/version/${versionId}`)
  return response.data.data
}

/**
 * 删除应用版本
 * @param versionId 版本ID
 * @param forceDelete 是否强制删除
 * @returns 删除结果
 */
export const deleteAppVersion = async (
  versionId: number,
  forceDelete: boolean = false
): Promise<void> => {
  await api.delete(`/api/admin/app/version/${versionId}`, {
    params: { forceDelete }
  })
}

/**
 * 更新版本信息
 * @param versionId 版本ID
 * @param updateDescription 更新说明
 * @returns 更新结果
 */
export const updateAppVersion = async (
  versionId: number,
  updateDescription: string
): Promise<AppVersion> => {
  const response = await api.put<ApiResponse<AppVersion>>(`/api/admin/app/version/${versionId}`, {
    updateDescription
  })
  return response.data.data
} 