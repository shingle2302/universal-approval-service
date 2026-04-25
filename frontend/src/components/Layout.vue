<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': isCollapsed, 'mobile-menu-open': isMobileMenuOpen }">
    <div v-if="isMobileMenuOpen" class="mobile-overlay" @click="closeMobileMenu"></div>

    <aside class="sidebar" :aria-hidden="isMobile && !isMobileMenuOpen">
      <div class="sidebar-header">
        <div class="sidebar-logo">U</div>
        <div class="sidebar-title" v-show="!isCollapsed">审批系统</div>
      </div>

      <nav class="sidebar-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="sidebar-menu-item"
          :class="{ active: $route.path === item.path }"
          @click="closeMobileMenu"
        >
          <span class="sidebar-menu-icon">{{ item.icon }}</span>
          <span v-show="!isCollapsed">{{ item.name }}</span>
        </router-link>
      </nav>
    </aside>

    <div class="main-content">
      <header class="top-nav">
        <div class="top-nav-left">
          <button class="icon-btn" @click="toggleSidebar" aria-label="切换侧边栏">
            {{ isMobile ? '☰' : (isCollapsed ? '➡️' : '⬅️') }}
          </button>
          <h1 class="top-nav-title">{{ currentTitle }}</h1>
        </div>
        <div class="top-nav-right">
          <span class="user-info">管理员</span>
        </div>
      </header>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapsed = ref(false)
const isMobile = ref(false)
const isMobileMenuOpen = ref(false)

const checkViewport = () => {
  isMobile.value = window.innerWidth <= 960
  if (!isMobile.value) {
    isMobileMenuOpen.value = false
  }
}

const toggleSidebar = () => {
  if (isMobile.value) {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
    return
  }
  isCollapsed.value = !isCollapsed.value
}

const closeMobileMenu = () => {
  if (isMobile.value) {
    isMobileMenuOpen.value = false
  }
}

onMounted(() => {
  checkViewport()
  window.addEventListener('resize', checkViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkViewport)
})

const menuItems = [
  { name: '首页', path: '/', icon: '🏠' },
  { name: '模板管理', path: '/template', icon: '📋' },
  { name: '任务管理', path: '/task', icon: '✅' },
  { name: '流程管理', path: '/process', icon: '📊' },
  { name: '流程设计', path: '/designer', icon: '🎨' }
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
.user-info {
  color: var(--text-secondary);
  font-size: 14px;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  background-color: var(--bg-tertiary);
}

.icon-btn {
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 0.4rem 0.55rem;
  cursor: pointer;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.45);
  z-index: 20;
}
</style>
