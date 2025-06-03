<template>
  <div class="app-list-view">
    <!-- 页面标题和操作栏 -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">应用列表</h1>
        <p class="text-gray-600 mt-1">管理您的应用版本</p>
      </div>
      
      <div class="flex space-x-3">
        <el-button
          type="primary"
          @click="showUploadDialog = true"
        >
          <el-icon><Upload /></el-icon>
          上传APK
        </el-button>
      </div>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center space-x-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索应用名称..."
          style="width: 320px"
          @input="handleSearch"
          clearable
          size="default"
        >
          <template #prefix>
            <el-icon class="text-gray-400"><Search /></el-icon>
          </template>
        </el-input>
        
        <el-button
          type="default"
          @click="loadApps"
          :loading="loading"
        >
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
      
      <div class="flex space-x-3">
        <el-button
          type="success"
          @click="showUploadDialog = true"
        >
          <el-icon><Upload /></el-icon>
          上传新应用
        </el-button>
      </div>
    </div>

    <!-- 应用列表 -->
    <el-table
      :data="apps"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <!-- 应用图标列 -->
      <el-table-column label="应用图标" width="80" align="center">
        <template #default="{ row }">
          <AppIcon 
            :app-name="row.appName" 
            :package-name="row.packageName"
            :size="32"
          />
        </template>
      </el-table-column>
      
      <el-table-column prop="appName" label="应用名称" min-width="120">
        <template #default="{ row }">
          <div class="flex items-center">
            <span class="font-medium">{{ row.appName }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="packageName" label="包名 (AppId)" min-width="180" />
      
      <el-table-column label="最新版本" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.latestVersion">{{ row.latestVersion.versionName }}</span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="版本代码" width="80" align="center">
        <template #default="{ row }">
          <span v-if="row.latestVersion">{{ row.latestVersion.versionCode }}</span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="更新时间" width="150">
        <template #default="{ row }">
          {{ formatDate(row.updateTime || row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag 
            v-if="row.latestVersion" 
            :type="row.latestVersion.status === 1 ? 'success' : 'danger'"
            size="small"
          >
            {{ row.latestVersion.status === 1 ? '已启用' : '已禁用' }}
          </el-tag>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="viewVersions(row)"
          >
            查看版本
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="uploadNewVersion(row)"
          >
            上传版本
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 上传APK对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传APK文件"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".apk"
        drag
        @change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将APK文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传APK文件，且不超过100MB
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
            :disabled="!selectedFile"
          >
            {{ uploading ? '上传中...' : '确定上传' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type UploadInstance, type UploadFile } from 'element-plus'
import {
  Upload,
  Search,
  Cellphone,
  UploadFilled
} from '@element-plus/icons-vue'
import { useAppStore, type AppInfo } from '../stores/app'
import { getApps, uploadApk } from '../services/appApi'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const appStore = useAppStore()

// 响应式数据
const loading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const searchQuery = ref('')
const apps = ref<AppInfo[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)

// 加载应用列表
const loadApps = async () => {
  try {
    loading.value = true
    const response = await getApps({
      appNameQuery: searchQuery.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    apps.value = response.content
    total.value = response.totalElements
    appStore.setApps(response.content)
  } catch (error) {
    console.error('加载应用列表失败:', error)
    ElMessage.error('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadApps()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadApps()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadApps()
}

// 查看版本管理
const viewVersions = (app: AppInfo) => {
  appStore.setCurrentApp(app)
  router.push(`/admin/app/${app.appId}/versions`)
}

// 上传新版本
const uploadNewVersion = (app: AppInfo) => {
  appStore.setCurrentApp(app)
  showUploadDialog.value = true
}

// 文件选择处理
const handleFileChange = (file: UploadFile) => {
  if (file.raw) {
    selectedFile.value = file.raw
  }
}

// 上传APK
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择APK文件')
    return
  }

  try {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    
    await uploadApk(formData)
    ElMessage.success('APK上传成功')
    showUploadDialog.value = false
    selectedFile.value = null
    uploadRef.value?.clearFiles()
    
    // 重新加载应用列表
    loadApps()
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.app-list-view {
  padding: 24px;
}

.el-upload__tip {
  color: #606266;
  font-size: 12px;
  margin-top: 7px;
}
</style> 