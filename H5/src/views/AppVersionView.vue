<template>
  <div class="app-version-view">
    <!-- 页面标题和返回按钮 -->
    <div class="flex items-center mb-6">
      <el-button
        type="text"
        @click="goBack"
        class="mr-4"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回应用列表
      </el-button>
      
      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          {{ currentApp?.appName || '应用版本管理' }}
        </h1>
        <p class="text-gray-600 mt-1">
          包名: {{ currentApp?.packageName }}
        </p>
      </div>
    </div>

    <!-- 版本列表 -->
    <el-table
      :data="versions"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="versionName" label="版本号" width="120">
        <template #default="{ row }">
          <el-tag type="primary">{{ row.versionName }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="versionCode" label="版本代码" width="100" align="center" />
      
      <el-table-column label="文件大小" width="100" align="center">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="updateDescription" label="更新说明" min-width="200">
        <template #default="{ row }">
          <span v-if="row.updateDescription">{{ row.updateDescription }}</span>
          <span v-else class="text-gray-400">无更新说明</span>
        </template>
      </el-table-column>
      
      <el-table-column label="强制更新" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.forceUpdate ? 'danger' : 'success'">
            {{ row.forceUpdate ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ row.statusDescription }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="editVersion(row)"
          >
            编辑
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="downloadApk(row)"
          >
            下载
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="deleteVersion(row)"
          >
            删除
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

    <!-- 编辑版本对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑版本信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editFormRef"
        label-width="100px"
      >
        <el-form-item label="更新说明" prop="updateDescription">
          <el-input
            v-model="editForm.updateDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入更新说明"
          />
        </el-form-item>
        
        <el-form-item label="强制更新">
          <el-switch
            v-model="editForm.forceUpdate"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="updating"
            @click="handleUpdate"
          >
            {{ updating ? '更新中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAppStore, type AppVersion } from '../stores/app'
import { getAppVersions, updateAppVersion, deleteAppVersion } from '../services/appApi'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 响应式数据
const loading = ref(false)
const updating = ref(false)
const showEditDialog = ref(false)
const versions = ref<AppVersion[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editFormRef = ref<FormInstance>()
const currentEditVersion = ref<AppVersion | null>(null)

// 当前应用信息
const currentApp = computed(() => appStore.currentApp)

// 编辑表单
const editForm = reactive({
  updateDescription: '',
  forceUpdate: false,
  status: 1
})

// 表单验证规则
const editRules: FormRules = {
  updateDescription: [
    { max: 500, message: '更新说明不能超过500字符', trigger: 'blur' }
  ]
}

// 加载版本列表
const loadVersions = async () => {
  const appId = route.params.appId as string
  if (!appId) return

  try {
    loading.value = true
    const response = await getAppVersions(appId, {
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    versions.value = response.content
    total.value = response.totalElements
    appStore.setVersions(response.content)
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadVersions()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadVersions()
}

// 返回应用列表
const goBack = () => {
  router.push('/admin/apps')
}

// 编辑版本
const editVersion = (version: AppVersion) => {
  currentEditVersion.value = version
  editForm.updateDescription = version.updateDescription || ''
  editForm.forceUpdate = version.forceUpdate
  editForm.status = version.status
  showEditDialog.value = true
}

// 更新版本信息
const handleUpdate = async () => {
  if (!editFormRef.value || !currentEditVersion.value) return

  try {
    await editFormRef.value.validate()
    updating.value = true
    
    await updateAppVersion(currentEditVersion.value.id, {
      updateDescription: editForm.updateDescription,
      forceUpdate: editForm.forceUpdate,
      status: editForm.status
    })
    
    ElMessage.success('版本信息更新成功')
    showEditDialog.value = false
    loadVersions()
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 删除版本
const deleteVersion = async (version: AppVersion) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 ${version.versionName} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteAppVersion(version.id)
    ElMessage.success('版本删除成功')
    loadVersions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 下载APK
const downloadApk = (version: AppVersion) => {
  const downloadUrl = `http://localhost:8080${version.downloadUrl}`
  window.open(downloadUrl, '_blank')
}

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadVersions()
})
</script>

<style scoped>
.app-version-view {
  padding: 24px;
}
</style> 