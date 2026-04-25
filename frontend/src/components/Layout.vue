<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">U</div>
        <div class="sidebar-title">审批系统</div>
      </div>
      <nav class="sidebar-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          class="sidebar-menu-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="sidebar-menu-icon">{{ item.icon }}</span>
          <span>{{ item.name }}</span>
        </router-link>
      </nav>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="top-nav-left">
          <h1 class="top-nav-title">{{ currentTitle }}</h1>
        </div>
        <div class="top-nav-right">
          <span class="user-info">管理员</span>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  {
    name: '首页',
    path: '/',
    icon: '🏠'
  },
  {
    name: '模板管理',
    path: '/template',
    icon: '📋'
  },
  {
    name: '任务管理',
    path: '/task',
    icon: '✅'
  },
  {
    name: '流程管理',
    path: '/process',
    icon: '📊'
  },
  {
    name: '流程设计',
    path: '/designer',
    icon: '🎨'
  }
]

const currentTitle = computed(() => {
  const routeMap = {
    '/': '首页',
    '/template': '模板管理',
    '/task': '任务管理',
    '/process': '流程管理',
    '/designer': '流程设计'
  }
  return routeMap[route.path] || '审批系统'
})
</script>

<style scoped>
/* 组件特定样式 */
.user-info {
  color: var(--text-secondary);
  font-size: 14px;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  background-color: var(--bg-tertiary);
}
</style>