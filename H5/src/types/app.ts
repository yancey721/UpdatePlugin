/**
 * 应用信息接口
 */
export interface AppInfo {
  id: number
  appId: string
  appName: string
  packageName: string
  forceUpdate: boolean  // 新增：应用级别强制更新设置
  createTime: string
  updateTime: string
}

/**
 * 应用版本接口
 */
export interface AppVersion {
  id: number
  appId: string
  appName: string
  packageName: string
  versionCode: number
  versionName: string
  fileSize: number
  md5: string
  apkPath: string
  downloadUrl: string
  updateDescription: string
  forceUpdate: boolean
  isReleased: boolean  // 新增：是否为发布版本
  createTime: string
  updateTime: string
}

/**
 * 应用信息及最新版本接口
 */
export interface AppInfoWithLatestVersion {
  id: number
  appId: string
  appName: string
  packageName: string
  forceUpdate: boolean  // 新增：应用级别强制更新设置
  createTime: string
  updateTime: string
  
  // 最新发布版本信息
  latestVersionId?: number
  latestVersionCode?: number
  latestVersionName?: string
  latestFileSize?: number
  latestUpdateDescription?: string
  latestForceUpdate?: boolean
  latestIsReleased?: boolean
  latestVersionCreateTime?: string
  
  totalVersions: number
}

/**
 * 更新强制更新设置请求接口
 */
export interface UpdateForceUpdateRequest {
  forceUpdate: boolean
}

/**
 * 创建应用请求接口
 */
export interface CreateAppRequest {
  packageName: string
  appName: string
  appDescription?: string
  forceUpdate?: boolean
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

/**
 * API响应接口
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
} 