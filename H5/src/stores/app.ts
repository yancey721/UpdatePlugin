import { defineStore } from 'pinia'
import { ref } from 'vue'

// 应用信息接口
export interface AppInfo {
  id: number
  appId: string
  appName: string
  packageName: string
  createTime: string
  updateTime: string
  latestVersion?: AppVersion
  totalVersions: number
}

// 应用版本接口
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
  status: number
  statusDescription: string
  createTime: string
  updateTime: string
}

// 分页响应接口
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export const useAppStore = defineStore('app', () => {
  const apps = ref<AppInfo[]>([])
  const currentApp = ref<AppInfo | null>(null)
  const versions = ref<AppVersion[]>([])
  const loading = ref(false)

  // 设置应用列表
  const setApps = (appList: AppInfo[]) => {
    apps.value = appList
  }

  // 设置当前应用
  const setCurrentApp = (app: AppInfo) => {
    currentApp.value = app
  }

  // 设置版本列表
  const setVersions = (versionList: AppVersion[]) => {
    versions.value = versionList
  }

  // 设置加载状态
  const setLoading = (state: boolean) => {
    loading.value = state
  }

  return {
    apps,
    currentApp,
    versions,
    loading,
    setApps,
    setCurrentApp,
    setVersions,
    setLoading
  }
}) 