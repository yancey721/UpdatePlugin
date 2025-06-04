<script setup lang="ts">
import { StagewiseToolbar } from '@stagewise/toolbar-vue'

// 应用主组件，作为路由容器

// stagewise工具栏配置
const stagewiseConfig = {
  plugins: []
}

// 仅在开发模式下显示工具栏
const isDevelopment = import.meta.env.DEV
</script>

<template>
  <div id="app">
    <router-view />
    <!-- stagewise工具栏 - 仅在开发模式下显示 -->
    <StagewiseToolbar v-if="isDevelopment" :config="stagewiseConfig" />
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>
