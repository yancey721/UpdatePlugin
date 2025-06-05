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
                      v-if="row.isReleased" 
                      type="success" 
                      size="small" 
                      class="latest-tag"
                    >
                      当前发布版本
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
              <el-table-column label="操作" width="280" align="left" header-align="left">
                <template #default="{ row }">
                  <div class="action-buttons">
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
                    <el-button 
                      v-if="!row.isReleased"
                      type="success" 
                      link
                      class="release-button"
                      @click="handleSetVersionAsRelease(row)"
                    >
                      <el-icon class="release-icon">
                        <Promotion />
                      </el-icon>
                      设为发布版本
                    </el-button>
                  </div>
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
      :title="isEditMode ? '编辑版本' : (showApkInfoStep ? '确认发布版本' : '上传新版本')"
      width="600px"
      :before-close="handleUploadDialogClose"
    >
      <div class="upload-container">
        <!-- 步骤1：文件上传 -->
        <div v-if="!showApkInfoStep">
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

        <!-- 步骤2：APK信息确认和发布 -->
        <div v-if="showApkInfoStep && uploadedApkInfo" class="apk-info-step">
          <!-- APK解析信息展示 -->
          <div class="apk-info-section">
            <h3 class="info-title">
              <el-icon class="title-icon" style="color: #10B981;">
                <Check />
              </el-icon>
              APK上传成功！
            </h3>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">包名：</span>
                <span class="info-value">{{ uploadedApkInfo.packageName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">版本号：</span>
                <span class="info-value">{{ uploadedApkInfo.versionCode }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">版本名称：</span>
                <span class="info-value">{{ uploadedApkInfo.versionName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">文件大小：</span>
                <span class="info-value">{{ formatFileSize(uploadedApkInfo.fileSize) }}</span>
              </div>
              <div v-if="!isEditMode" class="info-item">
                <span class="info-label">MD5：</span>
                <span class="info-value code-text">{{ uploadedApkInfo.md5 }}</span>
              </div>
            </div>
          </div>

          <!-- 版本更新说明输入框 -->
          <div class="release-description-section">
            <h4 class="section-title">
              {{ isEditMode ? '编辑版本说明' : '版本更新说明' }} 
              <span class="required">*</span>
            </h4>
            <el-input
              v-model="releaseDescription"
              type="textarea"
              :rows="4"
              :placeholder="isEditMode ? '修改版本更新说明...' : '请详细描述本次更新的内容，这些信息将展示给用户...'"
              maxlength="500"
              show-word-limit
              class="description-input"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <!-- 文件上传步骤的按钮 -->
          <template v-if="!showApkInfoStep">
            <el-button @click="handleUploadDialogClose">取消</el-button>
            <el-button 
              type="primary" 
              @click="handleStartUpload"
              :disabled="!selectedFile || uploading"
              :loading="uploading"
            >
              {{ uploading ? '上传中...' : '开始上传' }}
            </el-button>
          </template>

          <!-- APK信息确认步骤的按钮 -->
          <template v-if="showApkInfoStep">
            <!-- 编辑模式下的删除按钮单独在左侧 -->
            <el-button 
              v-if="showDeleteButton"
              type="danger" 
              @click="handleDeleteVersion"
              :loading="deleting"
              class="delete-button-left"
            >
              {{ deleting ? '删除中...' : '删除版本' }}
            </el-button>
            
            <div class="button-group">
              <el-button 
                type="success"
                @click="handlePublishVersion"
                :disabled="!canSave"
                :loading="publishing"
                class="save-button"
              >
                {{ publishing ? '保存中...' : '保存' }}
              </el-button>
              <el-button 
                type="primary" 
                @click="handleSetAsReleaseVersion"
                :disabled="!canPublish"
                :loading="publishing"
              >
                {{ publishing ? '设置中...' : '发布版本' }}
              </el-button>
            </div>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 新建应用对话框 -->
    <el-dialog
      v-model="createAppDialogVisible"
      width="520px"
      :before-close="handleCreateAppDialogClose"
      align-center
      destroy-on-close
      class="create-app-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-icon">
            <el-icon :size="24" color="#6366F1">
              <Plus />
            </el-icon>
          </div>
          <div class="header-content">
            <h3 class="header-title">创建新应用</h3>
            <p class="header-subtitle">快速创建一个新的移动应用项目</p>
          </div>
        </div>
      </template>
      
      <div class="dialog-body">
        <el-form 
          :model="createAppForm" 
          :rules="createAppRules"
          ref="createAppFormRef"
          label-position="top"
          size="large"
          class="create-app-form"
        >
          <div class="form-field">
            <el-form-item label="应用名称" prop="appName">
              <div class="input-wrapper">
                <el-icon class="input-icon" :size="18" color="#9CA3AF">
                  <Document />
                </el-icon>
                <el-input
                  v-model="createAppForm.appName"
                  placeholder="如：我的超级应用"
                  clearable
                  maxlength="50"
                  show-word-limit
                  class="styled-input"
                />
              </div>
              <div class="field-hint">
                <el-icon :size="14" color="#10B981">
                  <Check />
                </el-icon>
                用户在设备上看到的应用名称
              </div>
            </el-form-item>
          </div>

          <div class="form-field">
            <el-form-item label="包名 (Package Name)" prop="packageName">
              <div class="input-wrapper">
                <el-icon class="input-icon" :size="18" color="#9CA3AF">
                  <Folder />
                </el-icon>
                <el-input
                  v-model="createAppForm.packageName"
                  placeholder="com.company.appname"
                  clearable
                  class="styled-input package-input"
                  @input="validatePackageName"
                />
              </div>
              <div class="field-hint">
                <el-icon :size="14" color="#F59E0B">
                  <SwitchButton />
                </el-icon>
                应用的唯一标识符，创建后不可修改
              </div>
              <div v-if="packageNameError" class="field-error">
                <el-icon :size="14" color="#EF4444">
                  <Close />
                </el-icon>
                {{ packageNameError }}
              </div>
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button 
            @click="handleCreateAppDialogClose"
            size="large"
            class="cancel-btn"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSubmitCreateApp"
            :loading="creatingApp"
            :disabled="!canSubmitCreateApp"
            size="large"
            class="submit-btn"
          >
            <el-icon v-if="!creatingApp" :size="16" style="margin-right: 6px;">
              <Plus />
            </el-icon>
            {{ creatingApp ? '创建中...' : '创建应用' }}
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
  Close,
  Check,
  Promotion
} from '@element-plus/icons-vue'
import { 
  updateAppForceUpdate, 
  setReleaseVersion,
  uploadApk,
  getAppList,
  getAppVersions,
  deleteAppVersion,
  updateAppVersion,
  createApp
} from '../services/appApi'

// 应用数据类型
interface AppVersion {
  id: string
  version: string
  build: number
  size: string
  downloads: number
  updateTime: string
  description: string
  isReleased: boolean
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
const apps = ref<AppInfo[]>([])
const loading = ref(false)

// 加载应用数据
const loadApps = async () => {
  try {
    loading.value = true
    const response = await getAppList(0, 100) // 获取前100个应用
    
    // 转换数据格式
    apps.value = response.content.map(item => ({
      id: item.id ? item.id.toString() : item.appId, // 如果id为null，使用appId作为id
      name: item.appName,
      packageId: item.appId,
      color: generateColor(item.appName), // 生成颜色
      forceUpdate: item.forceUpdate,
      versions: [] // 暂时为空，需要时再加载
    }))
    
    // 默认选择第一个应用
    if (apps.value.length > 0) {
      await selectApp(apps.value[0])
    }
  } catch (error) {
    console.error('加载应用数据失败:', error)
    ElMessage.error('加载应用数据失败，请重试')
  } finally {
    loading.value = false
  }
}

// 生成应用颜色
const generateColor = (name: string): string => {
  const colors = [
    '#3B82F6', '#10B981', '#F59E0B', '#EF4444', 
    '#8B5CF6', '#06B6D4', '#84CC16', '#F97316'
  ]
  const index = name.charCodeAt(0) % colors.length
  return colors[index]
}

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
const selectApp = async (app: AppInfo) => {
  try {
    selectedApp.value = app
    
    // 加载该应用的版本数据
    const versionsResponse = await getAppVersions(app.packageId, 0, 100)
    
    // 转换版本数据格式
    const versions = versionsResponse.content.map(version => ({
      id: version.id.toString(),
      version: version.versionName,
      build: version.versionCode,
      size: formatFileSize(version.fileSize),
      downloads: 0, // 下载次数默认为0
      updateTime: version.updateTime.replace('T', ' ').slice(0, 16),
      description: version.updateDescription || '暂无更新说明',
      isReleased: version.isReleased
    }))
    
    // 按更新时间降序排列（从晚到早，最新的在前面）
    app.versions = versions.sort((a, b) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
  } catch (error) {
    console.error('加载版本数据失败:', error)
    ElMessage.error('加载版本数据失败')
  }
}

const handleCreateApp = () => {
  createAppDialogVisible.value = true
  // 重置表单
  createAppForm.value = {
    packageName: '',
    appName: ''
  }
  packageNameError.value = ''
}

const handleCreateAppDialogClose = () => {
  createAppDialogVisible.value = false
  // 重置表单
  createAppForm.value = {
    packageName: '',
    appName: ''
  }
  packageNameError.value = ''
  creatingApp.value = false
  // 重置表单验证
  if (createAppFormRef.value) {
    createAppFormRef.value.resetFields()
  }
}

const handleSubmitCreateApp = async () => {
  // 基础验证
  if (!createAppForm.value.packageName.trim()) {
    ElMessage.error('请输入包名')
    return
  }
  if (!createAppForm.value.appName.trim()) {
    ElMessage.error('请输入应用名称')
    return
  }

  // 包名格式验证
  const packageNamePattern = /^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$/
  if (!packageNamePattern.test(createAppForm.value.packageName)) {
    ElMessage.error('包名格式不正确，应符合Java包名规范（如：com.example.myapp）')
    return
  }

  try {
    creatingApp.value = true
    
    await createApp({
      packageName: createAppForm.value.packageName,
      appName: createAppForm.value.appName,
      appDescription: undefined,
      forceUpdate: false
    })
    
    ElMessage.success('应用创建成功！')
    
    // 关闭对话框
    handleCreateAppDialogClose()
    
    // 重新加载应用列表
    await loadApps()
    
  } catch (error: any) {
    console.error('创建应用失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '创建应用失败，请重试'
    ElMessage.error(errorMessage)
  } finally {
    creatingApp.value = false
  }
}

const handleUploadVersion = () => {
  // 重置为上传模式
  isEditMode.value = false
  editingVersion.value = null
  uploadDialogVisible.value = true
}

const handleEditVersion = (version: AppVersion) => {
  // 设置编辑模式并直接进入APK信息步骤
  showApkInfoDialog(version, true)
}

// 从版本列表中设置发布版本
const handleSetVersionAsRelease = async (version: AppVersion) => {
  if (!selectedApp.value) {
    ElMessage.error('未选择应用')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将版本 ${version.version} (Build ${version.build}) 设为发布版本吗？这会取消之前的发布版本。`,
      '确认发布版本',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用API设置发布版本
    await setReleaseVersion(selectedApp.value.packageId, parseInt(version.id))
    
    ElMessage.success('发布版本设置成功')
    
    // 刷新应用版本列表
    await selectApp(selectedApp.value)
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('设置发布版本失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '设置发布版本失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 统一的APK信息对话框显示方法
const showApkInfoDialog = (versionData: any, editMode: boolean = false) => {
  // 设置模式
  isEditMode.value = editMode
  editingVersion.value = editMode ? versionData : null
  
  // 设置APK信息
  if (editMode) {
    // 编辑模式：从现有版本数据构造APK信息
    uploadedApkInfo.value = {
      id: parseInt(versionData.id),
      packageName: selectedApp.value?.packageId || '',
      versionCode: versionData.build,
      versionName: versionData.version,
      fileSize: parseFloat(versionData.size.replace(/[^\d.]/g, '')) * (versionData.size.includes('MB') ? 1024 * 1024 : 1024),
      md5: 'editing-mode-no-md5'
    }
    releaseDescription.value = versionData.description || ''
  } else {
    // 上传模式：使用上传返回的数据
    uploadedApkInfo.value = versionData
    releaseDescription.value = ''
  }
  
  // 显示APK信息步骤
  showApkInfoStep.value = true
  uploadDialogVisible.value = true
}

const handleForceUpdateChange = async (value: boolean) => {
  try {
    if (selectedApp.value) {
      await updateAppForceUpdate(selectedApp.value.packageId, value)
      ElMessage.success(`强制更新已${value ? '开启' : '关闭'}`)
    }
  } catch (error) {
    ElMessage.error('设置失败，请重试')
    // 回滚UI状态
    if (selectedApp.value) {
      selectedApp.value.forceUpdate = !value
    }
  }
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
  // 加载应用数据
  loadApps()
})

// 上传新版本对话框相关
const uploadDialogVisible = ref(false)
const isDragOver = ref(false)
const selectedFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadProgressText = ref('')

// 上传成功后的APK信息展示和发布功能
const uploadedApkInfo = ref<any>(null)
const showApkInfoStep = ref(false)
const releaseDescription = ref('')
const publishing = ref(false)

// 编辑模式相关
const isEditMode = ref(false)
const editingVersion = ref<AppVersion | null>(null)
const deleting = ref(false)

// 新建应用对话框相关
const createAppDialogVisible = ref(false)
const createAppFormRef = ref()
const createAppForm = ref<{
  packageName: string
  appName: string
}>({
  packageName: '',
  appName: ''
})
const creatingApp = ref(false)
const packageNameError = ref('')

// 表单验证规则
const createAppRules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  packageName: [
    { required: true, message: '请输入包名', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$/,
      message: '包名格式不正确，应符合Java包名规范',
      trigger: 'blur'
    }
  ]
}

// 包名实时验证
const validatePackageName = () => {
  const packageName = createAppForm.value.packageName
  if (!packageName) {
    packageNameError.value = ''
    return
  }
  
  const pattern = /^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$/
  if (!pattern.test(packageName)) {
    packageNameError.value = '包名格式不正确，应以字母开头，用点分隔各部分'
  } else {
    packageNameError.value = ''
  }
}

// 表单提交验证
const canSubmitCreateApp = computed(() => {
  return createAppForm.value.packageName.trim() && 
         createAppForm.value.appName.trim() && 
         !packageNameError.value
})

const handleUploadDialogClose = () => {
  uploadDialogVisible.value = false
  selectedFile.value = null
  isDragOver.value = false
  
  // 重置APK信息展示相关状态
  uploadedApkInfo.value = null
  showApkInfoStep.value = false
  releaseDescription.value = ''
  publishing.value = false
  
  // 重置编辑模式相关状态
  isEditMode.value = false
  editingVersion.value = null
  deleting.value = false
  
  // 重置上传状态
  uploading.value = false
  uploadProgress.value = 0
  uploadProgressText.value = ''
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

const handleStartUpload = async () => {
  if (!selectedFile.value || !selectedApp.value) {
    ElMessage.error('请先选择APK文件')
    return
  }
  
  try {
    uploading.value = true
    uploadProgress.value = 0
    uploadProgressText.value = '准备上传...'
    
    // 模拟上传进度更新
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
        uploadProgressText.value = `上传中... ${uploadProgress.value}%`
      }
    }, 200)
    
    // 调用真实的上传API
    const uploadedVersion = await uploadApk(
      selectedFile.value,
      selectedApp.value.packageId,
      '通过前端上传', // 临时描述，用户可以在下一步修改
      false
    )
    
    // 完成上传进度
    clearInterval(progressInterval)
    uploadProgress.value = 100
    uploadProgressText.value = '上传完成'
    
    ElMessage.success('APK文件上传成功！')
    
    // 使用统一的方法显示APK信息步骤
    showApkInfoDialog(uploadedVersion, false)
    
    // 重置上传状态
    uploading.value = false
    uploadProgress.value = 0
    uploadProgressText.value = ''
    
    // 刷新当前应用的版本列表
    await selectApp(selectedApp.value)
    
  } catch (error: any) {
    console.error('上传失败:', error)
    uploading.value = false
    uploadProgress.value = 0
    uploadProgressText.value = ''
    
    const errorMessage = error.response?.data?.message || error.message || '上传失败，请重试'
    ElMessage.error(errorMessage)
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleSetAsReleaseVersion = async () => {
  if (!uploadedApkInfo.value || !selectedApp.value) {
    ElMessage.error('版本信息丢失，请重新操作')
    return
  }
  
  try {
    publishing.value = true
    
    // 先执行保存逻辑（更新版本说明）- 不管是编辑模式还是上传模式都要更新说明
    if (releaseDescription.value.trim()) {
      await updateAppVersion(uploadedApkInfo.value.id, releaseDescription.value)
      ElMessage.success('版本信息已保存')
    }
    
    // 再执行发布逻辑（设为当前发布版本）
    await setReleaseVersion(selectedApp.value.packageId, uploadedApkInfo.value.id)
    
    ElMessage.success('已设为当前发布版本！')
    
    // 刷新版本列表
    await selectApp(selectedApp.value)
    
    // 关闭对话框
    handleUploadDialogClose()
    
  } catch (error: any) {
    console.error('设置发布版本失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '设置发布版本失败，请重试'
    ElMessage.error(errorMessage)
  } finally {
    publishing.value = false
  }
}

// 处理发布版本
const handlePublishVersion = async () => {
  if (!uploadedApkInfo.value || !selectedApp.value) {
    ElMessage.error('上传信息丢失，请重新上传')
    return
  }
  
  try {
    publishing.value = true
    
    if (isEditMode.value) {
      // 编辑模式：更新版本说明（如果有输入的话）
      if (releaseDescription.value.trim()) {
        await updateAppVersion(uploadedApkInfo.value.id, releaseDescription.value)
        ElMessage.success('版本信息已成功更新！')
      } else {
        ElMessage.success('版本信息保存成功（无更新说明）')
      }
    } else {
      // 上传模式：更新版本说明（必须先更新说明再发布）
      if (releaseDescription.value.trim()) {
        await updateAppVersion(uploadedApkInfo.value.id, releaseDescription.value)
        ElMessage.success('版本说明已保存！')
      } else {
        ElMessage.success('版本信息保存成功（无更新说明）')
      }
    }
    
    // 刷新版本列表
    await selectApp(selectedApp.value)
    
    // 关闭对话框
    handleUploadDialogClose()
    
  } catch (error: any) {
    console.error(isEditMode.value ? '更新失败:' : '保存失败:', error)
    const errorMessage = error.response?.data?.message || error.message || (isEditMode.value ? '更新失败，请重试' : '保存失败，请重试')
    ElMessage.error(errorMessage)
  } finally {
    publishing.value = false
  }
}

// 取消发布，直接关闭对话框
const handleCancelPublish = () => {
  handleUploadDialogClose()
}

// 删除版本
const handleDeleteVersion = async () => {
  if (!editingVersion.value) {
    ElMessage.error('编辑信息丢失，请重新操作')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 ${editingVersion.value.version} 吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }
    )
    
    deleting.value = true
    
    // 调用删除API
    await deleteAppVersion(parseInt(editingVersion.value.id), false)
    
    ElMessage.success('版本删除成功！')
    
    // 刷新版本列表
    if (selectedApp.value) {
      await selectApp(selectedApp.value)
    }
    
    // 关闭对话框
    handleUploadDialogClose()
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '删除失败，请重试'
      ElMessage.error(errorMessage)
    }
  } finally {
    deleting.value = false
  }
}

// 按钮状态计算
const canSave = computed(() => !publishing.value)
const canPublish = computed(() => !publishing.value && releaseDescription.value.trim())
const showDeleteButton = computed(() => isEditMode.value)
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
  background-color: #F8F9FA;
  border: 1px solid #E9ECEF;
}

:deep(.search-input .el-input__wrapper:hover) {
  background-color: #F1F3F4;
  border-color: #DEE2E6;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  background-color: #FFFFFF;
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  margin: 24px 0 0 0;
  width: 100%;
}

.button-group {
  display: flex;
  gap: 12px;
}

.delete-button-left {
  background: #EF4444;
  border-color: #EF4444;
  color: white;
  margin: 0;
  padding: 8px 16px;
}

.delete-button-left:hover {
  background: #DC2626;
  border-color: #DC2626;
}

.delete-button {
  background: #EF4444;
  border-color: #EF4444;
  color: white;
}

.delete-button:hover {
  background: #DC2626;
  border-color: #DC2626;
}

.apk-info-step {
  padding: 0;
}

.apk-info-section {
  background: #F8FAFC;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.info-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 8px;
  font-size: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.info-label {
  font-size: 14px;
  color: #6B7280;
  font-weight: 500;
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  color: #111827;
  font-weight: 600;
  word-break: break-all;
}

.code-text {
  font-family: 'Monaco', 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  color: #6B7280;
  background: #F3F4F6;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 400;
}

.release-description-section {
  margin-top: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.description-input {
  width: 100%;
}

:deep(.description-input .el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  font-size: 14px;
  line-height: 1.5;
}

:deep(.description-input .el-textarea__inner:focus) {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.required {
  color: #EF4444;
  font-weight: 600;
}

:deep(.el-dialog__footer) {
  padding: 0 24px 24px 24px;
}

.save-button {
  background: #10B981;
  border-color: #10B981;
  color: white;
}

.save-button:hover {
  background: #059669;
  border-color: #059669;
}

.save-button:disabled {
  background: #9CA3AF;
  border-color: #9CA3AF;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.release-button {
  color: #10B981;
  font-weight: 500;
}

.release-button:hover {
  color: #059669;
}

.release-icon {
  margin-right: 4px;
  font-size: 14px;
}

.edit-button {
  color: #3B82F6;
  font-weight: 500;
}

.edit-button:hover {
  color: #2563EB;
}

/* 新建应用弹窗样式 */
:deep(.create-app-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.create-app-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

:deep(.create-app-dialog .el-dialog__body) {
  padding: 32px 32px 24px 32px;
}

:deep(.create-app-dialog .el-dialog__footer) {
  padding: 0 32px 32px 32px;
  margin: 0;
}

.dialog-header {
  padding: 32px 32px 24px 32px;
  color: white;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-content {
  flex: 1;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: white;
}

.header-subtitle {
  font-size: 14px;
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

.dialog-body {
  background: #FAFBFF;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.create-app-form {
  margin: 0;
}

.form-field {
  margin-bottom: 24px;
}

.form-field:last-child {
  margin-bottom: 0;
}

:deep(.form-field .el-form-item) {
  margin-bottom: 0;
}

:deep(.form-field .el-form-item__label) {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  line-height: 1.5;
  margin-bottom: 8px;
  padding: 0;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  z-index: 10;
  pointer-events: none;
}

:deep(.styled-input .el-input__wrapper) {
  padding-left: 40px;
  border-radius: 10px;
  border: 2px solid #E5E7EB;
  background: white;
  transition: all 0.3s ease;
  min-height: 44px;
}

:deep(.styled-input .el-input__wrapper:hover) {
  border-color: #C7D2FE;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
}

:deep(.styled-input.is-focus .el-input__wrapper) {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

:deep(.package-input .el-input__inner) {
  font-family: 'Monaco', 'Consolas', 'Courier New', monospace;
  font-size: 14px;
}

.field-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

.field-error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: #EF4444;
  font-weight: 500;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  min-width: 100px;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  background: #F3F4F6;
  border: 2px solid #E5E7EB;
  color: #6B7280;
}

.cancel-btn:hover {
  background: #E5E7EB;
  border-color: #D1D5DB;
  color: #374151;
}

.submit-btn {
  min-width: 120px;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.submit-btn:disabled {
  background: #D1D5DB;
  box-shadow: none;
  transform: none;
  cursor: not-allowed;
}

.edit-icon {
  margin-right: 4px;
  font-size: 14px;
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}
</style> 