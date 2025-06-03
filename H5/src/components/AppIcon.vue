<template>
  <div class="app-icon" :style="iconStyle">
    <!-- 如果有图标URL则显示图标 -->
    <img 
      v-if="iconUrl" 
      :src="iconUrl" 
      :alt="appName"
      class="w-full h-full object-cover rounded-lg"
      @error="handleImageError"
    />
    <!-- 否则显示首字母或预设图标 -->
    <div v-else class="flex items-center justify-center w-full h-full text-white font-bold rounded-lg">
      <!-- 知名应用的预设图标 -->
      <component 
        v-if="presetIcon" 
        :is="presetIcon" 
        class="w-6 h-6"
      />
      <!-- 首字母显示 -->
      <span v-else class="text-lg">{{ firstLetter }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { 
  ChatDotRound, 
  Money, 
  ShoppingBag, 
  Monitor,
  Cellphone
} from '@element-plus/icons-vue'

interface Props {
  appName: string
  packageName?: string
  iconUrl?: string
  size?: number
}

const props = withDefaults(defineProps<Props>(), {
  size: 40
})

const imageError = ref(false)

// 处理图片加载错误
const handleImageError = () => {
  imageError.value = true
}

// 计算首字母
const firstLetter = computed(() => {
  return props.appName ? props.appName.charAt(0).toUpperCase() : '?'
})

// 根据包名或应用名生成背景颜色
const backgroundColor = computed(() => {
  const colors = [
    '#f87171', '#fb923c', '#fbbf24', '#facc15', 
    '#a3e635', '#4ade80', '#34d399', '#22d3ee',
    '#60a5fa', '#818cf8', '#a78bfa', '#c084fc',
    '#f472b6', '#fb7185'
  ]
  
  const key = props.packageName || props.appName || ''
  let hash = 0
  for (let i = 0; i < key.length; i++) {
    hash = key.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
})

// 获取预设图标组件
const presetIcon = computed(() => {
  const packageName = props.packageName?.toLowerCase() || ''
  const appName = props.appName?.toLowerCase() || ''
  
  // 微信相关
  if (packageName.includes('tencent') || appName.includes('微信')) {
    return ChatDotRound
  }
  // 支付宝相关
  if (packageName.includes('alipay') || appName.includes('支付')) {
    return Money
  }
  // 淘宝/购物相关
  if (packageName.includes('taobao') || appName.includes('淘宝') || appName.includes('购物')) {
    return ShoppingBag
  }
  // 内部应用
  if (packageName.includes('company') || packageName.includes('internal')) {
    return Monitor
  }
  // 通用移动应用
  if (packageName.includes('android') || packageName.includes('mobile')) {
    return Cellphone
  }
  
  return null
})

// 计算图标样式
const iconStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  backgroundColor: !props.iconUrl || imageError.value ? backgroundColor.value : 'transparent',
  borderRadius: '8px',
  flexShrink: 0
}))
</script>

<style scoped>
.app-icon {
  display: inline-block;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.1);
}
</style> 