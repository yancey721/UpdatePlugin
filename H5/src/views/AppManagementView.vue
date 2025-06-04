<template>
  <div class="app-management-container">
    <!-- 退出登录按钮 -->
    <div class="logout-section">
      <el-button 
        type="danger" 
        plain
        class="logout-button"
        @click="handleLogout"
      >
        <el-icon class="logout-icon">
          <SwitchButton />
        </el-icon>
        退出登录
      </el-button>
    </div>

    <!-- 左侧应用列表 -->
    <div class="app-list-sidebar">
      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索您的应用"
          class="search-input"
          size="large"
          clearable
        >
          <template #prefix>
            <el-icon class="search-icon">
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>

      <!-- 新建应用按钮 -->
      <div class="create-section">
        <el-button 
          type="primary" 
          class="create-button"
          size="large"
          @click="handleCreateApp"
        >
          <el-icon class="button-icon">
            <Plus />
          </el-icon>
          新建应用
        </el-button>
      </div>

      <!-- 应用列表标题 -->
      <div class="list-header">
        <span class="list-title">所有应用</span>
      </div>

      <!-- 应用列表 -->
      <div class="app-list">
        <div 
          v-for="app in filteredApps" 
          :key="app.id"
          class="app-item"
          :class="{ active: selectedApp?.id === app.id }"
          @click="selectApp(app)"
        >
          <div class="app-avatar">
            <img :src="app.icon" :alt="app.name" v-if="app.icon" />
            <div v-else class="app-avatar-placeholder" :style="{ backgroundColor: app.color }">
              {{ app.name.charAt(0) }}
            </div>
          </div>
          <div class="app-info">
            <div class="app-name">{{ app.name }}</div>
            <div class="app-package">{{ app.packageId }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧应用详情 -->
    <div class="app-detail-content">
      <div v-if="selectedApp" class="app-detail">
        <!-- 应用头部信息 -->
        <div class="app-header">
          <div class="app-header-left">
            <div class="app-detail-avatar">
              <img :src="selectedApp.icon" :alt="selectedApp.name" v-if="selectedApp.icon" />
              <div v-else class="app-detail-avatar-placeholder" :style="{ backgroundColor: selectedApp.color }">
                {{ selectedApp.name.charAt(0) }}
              </div>
            </div>
            <div class="app-header-info">
              <h1 class="app-detail-name">{{ selectedApp.name }}</h1>
              <p class="app-detail-package">Package ID: {{ selectedApp.packageId }}</p>
            </div>
          </div>
          <div class="app-header-actions">
            <el-button 
              type="success" 
              class="upload-button"
              size="large"
              @click="handleUploadVersion"
            >
              <el-icon class="upload-icon">
                <Upload />
              </el-icon>
              上传新版本
            </el-button>
          </div>
        </div>

        <!-- 安装设置 -->
        <div class="settings-section">
          <h2 class="section-title">安装设置</h2>
          <div class="setting-item">
            <span class="setting-label">是否强制更新</span>
            <el-switch 
              v-model="selectedApp.forceUpdate" 
              class="setting-switch"
              size="large"
              @change="handleForceUpdateChange"
            />
          </div>
        </div>

        <!-- 版本列表 -->
        <div class="versions-section">
          <h2 class="section-title">版本列表</h2>
          <div class="versions-table-wrapper">
            <el-table 
              :data="selectedApp.versions" 
              class="versions-table"
              :header-cell-style="tableHeaderStyle"
              :row-style="{ height: '60px' }"
            >
              <el-table-column prop="version" label="版本" width="180">
                <template #default="{ row }">
                  <div class="version-cell">
                    <span class="version-number">{{ row.version }}</span>
                    <el-tag 
                      v-if="row.isLatest" 
                      type="success" 
                      size="small" 
                      class="latest-tag"
                    >
                      当前最新版本
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="build" label="Build" width="100" align="center">
                <template #default="{ row }">
                  <span class="build-number">{{ row.build }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="size" label="大小" width="100" align="center">
                <template #default="{ row }">
                  <span class="size-text">{{ row.size }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="downloads" label="下载次数" width="120" align="center">
                <template #default="{ row }">
                  <span class="downloads-text">{{ row.downloads.toLocaleString() }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="更新时间" width="160">
                <template #default="{ row }">
                  <span class="time-text">{{ row.updateTime }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="更新说明" min-width="100">
                <template #default="{ row }">
                  <div class="description-cell">
                    <div v-for="(line, index) in row.description.split('\n')" :key="index" class="description-line">
                      {{ line }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="left" header-align="left">
                <template #default="{ row }">
                  <el-button 
                    type="primary" 
                    link
                    class="edit-button"
                    @click="handleEditVersion(row)"
                  >
                    <el-icon class="edit-icon">
                      <Edit />
                    </el-icon>
                    编辑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <!-- 未选择应用时的提示 -->
      <div v-else class="empty-state">
        <el-icon class="empty-icon">
          <Folder />
        </el-icon>
        <p class="empty-text">请选择一个应用查看详情</p>
      </div>
    </div>

    <!-- 上传新版本对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传新版本"
      width="600px"
      :before-close="handleUploadDialogClose"
    >
      <div class="upload-container">
        <!-- 文件上传区域 -->
        <div 
          class="upload-area"
          :class="{ 'upload-area-dragover': isDragOver }"
          @drop="handleFileDrop"
          @dragover.prevent="handleDragOver"
          @dragleave="handleDragLeave"
          @click="triggerFileSelect"
        >
          <div class="upload-content">
            <el-icon class="upload-file-icon">
              <Document />
            </el-icon>
            <p class="upload-text">拖拽APK文件到此处，或点击选择文件</p>
            <p class="upload-hint">支持 .apk 格式，最大文件大小 500MB</p>
            <el-button type="primary" class="select-file-btn">
              <el-icon class="select-icon">
                <Upload />
              </el-icon>
              选择文件
            </el-button>
          </div>
        </div>

        <!-- 隐藏的文件输入框 -->
        <input
          ref="fileInputRef"
          type="file"
          accept=".apk"
          style="display: none"
          @change="handleFileSelect"
        />

        <!-- 已选择的文件信息 -->
        <div v-if="selectedFile" class="file-info">
          <div class="file-item">
            <el-icon class="file-icon">
              <Document />
            </el-icon>
            <div class="file-details">
              <div class="file-name">{{ selectedFile.name }}</div>
              <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
            </div>
            <el-button 
              type="danger" 
              text 
              @click="removeSelectedFile"
              class="remove-file-btn"
            >
              <el-icon>
                <Close />
              </el-icon>
            </el-button>
          </div>
        </div>

        <!-- 上传进度 -->
        <div v-if="uploading" class="upload-progress">
          <el-progress 
            :percentage="uploadProgress" 
            :status="uploadProgress === 100 ? 'success' : undefined"
          />
          <p class="progress-text">{{ uploadProgressText }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleUploadDialogClose">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleStartUpload"
            :disabled="!selectedFile || uploading"
            :loading="uploading"
          >
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  Upload, 
  Edit, 
  Folder, 
  SwitchButton,
  Document,
  Close
} from '@element-plus/icons-vue'

// 应用数据类型
interface AppVersion {
  id: string
  version: string
  build: number
  size: string
  downloads: number
  updateTime: string
  description: string
  isLatest: boolean
}

interface AppInfo {
  id: string
  name: string
  packageId: string
  icon?: string
  color: string
  forceUpdate: boolean
  versions: AppVersion[]
}

// 路由和数据
const router = useRouter()
const searchQuery = ref('')
const selectedApp = ref<AppInfo | null>(null)

// 模拟应用数据
const apps = ref<AppInfo[]>([
  {
    id: '1',
    name: '掌上信手书',
    packageId: 'cn.org.bjca.signet.unify.app',
    color: '#4F8EF7',
    forceUpdate: true,
    versions: [
      {
        id: '1',
        version: '1.0.8_yhca',
        build: 4,
        size: '43.4 MB',
        downloads: 6008,
        updateTime: '2024-05-30 19:46',
        description: '- 修复已知BUG\n- 优化用户体验',
        isLatest: true
      },
      {
        id: '2',
        version: '1.0.8',
        build: 3,
        size: '43.4 MB',
        downloads: 962,
        updateTime: '2024-03-22 09:59',
        description: '- 新增离私议功能',
        isLatest: false
      },
      {
        id: '3',
        version: '1.0.8',
        build: 2,
        size: '43.4 MB',
        downloads: 29,
        updateTime: '2024-03-14 10:37',
        description: '- 优化启动速度',
        isLatest: false
      },
      {
        id: '4',
        version: '1.0.8',
        build: 1,
        size: '43.4 MB',
        downloads: 23,
        updateTime: '2024-03-08 18:47',
        description: '- 首次发布',
        isLatest: false
      }
    ]
  },
  {
    id: '2',
    name: '移动办公',
    packageId: 'com.company.mobile.office',
    color: '#10B981',
    forceUpdate: false,
    versions: [
      {
        id: '5',
        version: '2.1.0',
        build: 10,
        size: '67.2 MB',
        downloads: 2315,
        updateTime: '2024-05-25 14:30',
        description: '- 新增文档协作功能\n- 修复已知问题',
        isLatest: true
      }
    ]
  },
  {
    id: '3',
    name: '人力资源管理',
    packageId: 'com.company.hr.management',
    color: '#F59E0B',
    forceUpdate: false,
    versions: [
      {
        id: '6',
        version: '1.5.2',
        build: 8,
        size: '52.1 MB',
        downloads: 891,
        updateTime: '2024-05-20 10:15',
        description: '- 优化考勤统计\n- 新增请假审批流程',
        isLatest: true
      }
    ]
  },
  {
    id: '4',
    name: '客户关系',
    packageId: 'com.company.crm.system',
    color: '#EF4444',
    forceUpdate: true,
    versions: [
      {
        id: '7',
        version: '3.0.1',
        build: 15,
        size: '78.5 MB',
        downloads: 1567,
        updateTime: '2024-05-18 16:45',
        description: '- 重构客户管理模块\n- 提升性能表现',
        isLatest: true
      }
    ]
  },
  {
    id: '5',
    name: '财务管理',
    packageId: 'com.company.finance.manager',
    color: '#8B5CF6',
    forceUpdate: false,
    versions: [
      {
        id: '8',
        version: '2.3.0',
        build: 12,
        size: '45.8 MB',
        downloads: 734,
        updateTime: '2024-05-15 09:20',
        description: '- 新增财务报表功能\n- 优化数据导出',
        isLatest: true
      }
    ]
  }
])

// 计算属性
const filteredApps = computed(() => {
  if (!searchQuery.value) return apps.value
  return apps.value.filter(app => 
    app.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    app.packageId.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 表格头部样式
const tableHeaderStyle = {
  backgroundColor: '#F8FAFC',
  color: '#1F2937',
  fontWeight: '600',
  fontSize: '14px',
  height: '50px'
}

// 方法
const selectApp = (app: AppInfo) => {
  selectedApp.value = app
}

const handleCreateApp = () => {
  ElMessage.info('新建应用功能开发中...')
}

const handleUploadVersion = () => {
  uploadDialogVisible.value = true
}

const handleEditVersion = (version: AppVersion) => {
  ElMessage.info(`编辑版本 ${version.version} 功能开发中...`)
}

const handleForceUpdateChange = (value: boolean) => {
  ElMessage.success(`强制更新已${value ? '开启' : '关闭'}`)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除localStorage中的apiKey并跳转到登录页
    localStorage.removeItem('apiKey')
    localStorage.removeItem('serverUrl')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 取消操作
  })
}

// 生命周期
onMounted(() => {
  // 默认选择第一个应用
  if (apps.value.length > 0) {
    selectedApp.value = apps.value[0]
  }
})

// 上传新版本对话框相关
const uploadDialogVisible = ref(false)
const isDragOver = ref(false)
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadProgressText = ref('')

const handleUploadDialogClose = () => {
  uploadDialogVisible.value = false
  selectedFile.value = null
  isDragOver.value = false
}

const handleFileDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  const dataTransfer = event.dataTransfer
  if (dataTransfer) {
    const files = dataTransfer.files
    if (files.length > 0) {
      const file = files[0]
      if (validateFile(file)) {
        selectedFile.value = file
      }
    }
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
}

const triggerFileSelect = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target && target.files && target.files.length > 0) {
    const file = target.files[0]
    if (validateFile(file)) {
      selectedFile.value = file
    }
  }
}

const validateFile = (file: File): boolean => {
  // 检查文件类型
  if (!file.name.toLowerCase().endsWith('.apk')) {
    ElMessage.error('请选择APK文件')
    return false
  }
  
  // 检查文件大小 (500MB = 500 * 1024 * 1024 bytes)
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过500MB')
    return false
  }
  
  return true
}

const removeSelectedFile = () => {
  selectedFile.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const handleStartUpload = () => {
  if (!selectedFile.value) {
    ElMessage.error('请先选择APK文件')
    return
  }
  
  uploading.value = true
  uploadProgress.value = 0
  uploadProgressText.value = '准备上传...'
  
  // 模拟上传进度
  const interval = setInterval(() => {
    uploadProgress.value += 10
    uploadProgressText.value = `上传中... ${uploadProgress.value}%`
    
    if (uploadProgress.value >= 100) {
      clearInterval(interval)
      uploadProgressText.value = '上传完成'
      ElMessage.success('APK文件上传成功！')
      setTimeout(() => {
        handleUploadDialogClose()
        uploading.value = false
        uploadProgress.value = 0
        uploadProgressText.value = ''
      }, 1500)
    }
  }, 200)
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.app-management-container {
  display: flex;
  height: 100vh;
  background-color: #F5F5F7;
  position: relative;
}

/* 左侧应用列表 */
.app-list-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #E5E7EB;
  display: flex;
  flex-direction: column;
}

.search-section {
  padding: 16px;
  border-bottom: 1px solid #F3F4F6;
}

.search-input {
  width: 100%;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-icon {
  color: #9CA3AF;
  font-size: 16px;
}

.create-section {
  padding: 16px;
  border-bottom: 1px solid #F3F4F6;
}

.create-button {
  width: 100%;
  height: 40px;
  border-radius: 8px;
  font-weight: 500;
}

.button-icon {
  margin-right: 6px;
  font-size: 16px;
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #F3F4F6;
}

.list-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.app-list {
  flex: 1;
  overflow-y: auto;
}

.app-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #F9FAFB;
}

.app-item:hover {
  background-color: #F8FAFC;
}

.app-item.active {
  background-color: #EFF6FF;
  border-right: 3px solid #3B82F6;
}

.app-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
}

.app-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.app-info {
  flex: 1;
  min-width: 0;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-package {
  font-size: 12px;
  color: #6B7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧应用详情 */
.app-detail-content {
  flex: 1;
  overflow-y: auto;
  background: white;
}

.app-detail {
  padding: 32px 40px;
}

.app-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 48px;
  padding-bottom: 32px;
  border-bottom: 1px solid #F3F4F6;
}

.app-header-left {
  display: flex;
  align-items: flex-start;
}

.app-detail-avatar {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  overflow: hidden;
  margin-right: 20px;
  flex-shrink: 0;
}

.app-detail-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-detail-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 28px;
}

.app-header-info {
  flex: 1;
  padding-top: 4px;
}

.app-detail-name {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.app-detail-package {
  font-size: 16px;
  color: #6B7280;
  margin: 0;
  font-weight: 400;
}

.app-header-actions {
  padding-top: 8px;
}

.upload-button {
  height: 48px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  background: #10B981;
  border-color: #10B981;
}

.upload-button:hover {
  background: #059669;
  border-color: #059669;
}

.upload-icon {
  margin-right: 8px;
  font-size: 18px;
}

.settings-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid #F3F4F6;
}

.setting-label {
  font-size: 16px;
  color: #374151;
  font-weight: 500;
}

.setting-switch {
  margin-left: 16px;
}

:deep(.setting-switch .el-switch__core) {
  background-color: #4F8EF7;
}

.versions-section {
  margin-bottom: 32px;
}

.versions-table-wrapper {
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

.versions-table {
  width: 100%;
  background: white;
}

:deep(.versions-table .el-table__header-wrapper) {
  background: #F8FAFC;
}

:deep(.versions-table .el-table__header th) {
  background: #F8FAFC;
  border-bottom: 1px solid #E5E7EB;
  padding: 16px 12px;
}

:deep(.versions-table .el-table__body .el-table__row) {
  height: 60px;
  transition: background-color 0.2s;
}

:deep(.versions-table .el-table__body .el-table__row:hover) {
  background-color: #F9FAFB;
}

:deep(.versions-table .el-table__body .el-table__cell) {
  padding: 16px 12px;
  border-bottom: 1px solid #F3F4F6;
}

:deep(.versions-table .el-table_1_column_7) {
  padding-left: 8px;
}

.version-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.version-number {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
  flex-shrink: 0;
}

.latest-tag {
  font-size: 12px;
  background: #DCFCE7;
  color: #166534;
  border: none;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.build-number {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.size-text {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.downloads-text {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.time-text {
  color: #6B7280;
  font-size: 14px;
}

.description-cell {
  line-height: 1.5;
}

.description-line {
  color: #374151;
  font-size: 14px;
  margin-bottom: 4px;
}

.description-line:last-child {
  margin-bottom: 0;
}

.edit-button {
  color: #4F8EF7;
  padding: 4px 8px;
  font-size: 14px;
  font-weight: 500;
}

.edit-button:hover {
  background: #EFF6FF;
  color: #3B82F6;
}

.edit-icon {
  margin-right: 4px;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9CA3AF;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-management-container {
    flex-direction: column;
  }
  
  .app-list-sidebar {
    width: 100%;
    height: 300px;
  }
  
  .app-detail-content {
    flex: 1;
  }
  
  .app-detail {
    padding: 24px 20px;
  }
  
  .app-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  
  .app-header-actions {
    width: 100%;
  }
  
  .upload-button {
    width: 100%;
  }

  .app-detail-name {
    font-size: 24px;
  }
  
  .app-detail-avatar {
    width: 60px;
    height: 60px;
  }
  
  .section-title {
    font-size: 20px;
  }
}

/* 退出登录按钮 */
.logout-section {
  position: absolute;
  bottom: 20px;
  left: 24px;
  z-index: 100;
}

.logout-button {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  border-color: #EF4444;
  color: #EF4444;
}

.logout-button:hover {
  background-color: #FEF2F2;
  border-color: #DC2626;
  color: #DC2626;
}

.logout-icon {
  margin-right: 6px;
  font-size: 16px;
}

/* 上传新版本对话框相关样式 */
.upload-container {
  padding: 20px;
}

.upload-area {
  border: 2px dashed #4F8EF7;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-area-dragover {
  background-color: #EFF6FF;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-file-icon {
  font-size: 48px;
  color: #4F8EF7;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #111827;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 16px;
}

.select-file-btn {
  background: #4F8EF7;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
}

.select-file-btn:hover {
  background: #3B82F6;
}

.select-icon {
  margin-right: 8px;
  font-size: 16px;
}

.file-info {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-item {
  display: flex;
  align-items: center;
}

.file-icon {
  font-size: 24px;
  color: #4F8EF7;
  margin-right: 8px;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.file-size {
  font-size: 12px;
  color: #6B7280;
}

.remove-file-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}

.upload-progress {
  margin-top: 20px;
}

.progress-text {
  font-size: 14px;
  color: #6B7280;
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
}
</style> 