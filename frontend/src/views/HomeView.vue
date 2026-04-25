<template>
  <div class="home">
    <div class="breadcrumb">
      <span class="breadcrumb-item">首页</span>
    </div>

    <div class="welcome-section">
      <h1>欢迎使用通用审批系统</h1>
      <p class="welcome-description">高效、智能的审批流程管理平台，让审批更简单</p>
      <div class="welcome-actions">
        <button class="btn btn-secondary" @click="fetchSystemStatus" :disabled="isLoading">
          {{ isLoading ? '刷新中...' : '刷新状态' }}
        </button>
        <span v-if="lastUpdated" class="updated-at">最近更新：{{ lastUpdated }}</span>
      </div>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </div>

    <div class="quick-filter">
      <input v-model="keyword" type="text" placeholder="搜索功能（如：流程/模板/任务）" />
    </div>

    <div class="feature-cards">
      <div class="feature-card" v-for="feature in filteredFeatures" :key="feature.path">
        <div class="feature-icon">{{ feature.icon }}</div>
        <h3>{{ feature.title }}</h3>
        <p>{{ feature.description }}</p>
        <router-link :to="feature.path" class="btn btn-primary">{{ feature.action }}</router-link>
      </div>
    </div>

    <div class="status-section">
      <h2>系统状态</h2>
      <div v-if="isLoading" class="status-cards">
        <div class="status-card" v-for="item in 3" :key="item">
          <div class="status-value">--</div>
          <div class="status-label">加载中</div>
        </div>
      </div>
      <div v-else class="status-cards">
        <div class="status-card">
          <div class="status-value">{{ templateCount }}</div>
          <div class="status-label">已部署模板</div>
        </div>
        <div class="status-card">
          <div class="status-value">{{ taskCount }}</div>
          <div class="status-label">待办任务</div>
        </div>
        <div class="status-card">
          <div class="status-value">{{ processCount }}</div>
          <div class="status-label">运行中流程</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'

const templateCount = ref(0)
const taskCount = ref(0)
const processCount = ref(0)
const isLoading = ref(false)
const errorMessage = ref('')
const lastUpdated = ref('')
const keyword = ref('')

const features = [
  { icon: '📋', title: '模板管理', description: '创建和管理审批流程模板', path: '/template', action: '进入管理' },
  { icon: '✅', title: '任务管理', description: '处理待办审批任务', path: '/task', action: '查看任务' },
  { icon: '📊', title: '流程管理', description: '发起审批和管理流程实例', path: '/process', action: '发起审批' },
  { icon: '🎨', title: '流程设计', description: '可视化设计审批流程', path: '/designer', action: '开始设计' }
]

const filteredFeatures = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  if (!term) {
    return features
  }
  return features.filter((item) =>
    `${item.title}${item.description}`.toLowerCase().includes(term)
  )
})

const fetchSystemStatus = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [templateResponse, taskResponse, processResponse] = await Promise.all([
      axios.get('/api/approval/template'),
      axios.get('/api/approval/task'),
      axios.get('/api/approval/process')
    ])

    if (templateResponse.data.code === 200) {
      templateCount.value = templateResponse.data.data?.filter(t => t.status === 'DEPLOYED').length || 0
    }

    if (taskResponse.data.code === 200) {
      taskCount.value = taskResponse.data.data?.length || 0
    }

    if (processResponse.data.code === 200) {
      processCount.value = processResponse.data.data?.length || 0
    }

    lastUpdated.value = new Date().toLocaleString('zh-CN', { hour12: false })
  } catch (error) {
    errorMessage.value = '获取系统状态失败，请稍后重试。'
    console.log('获取系统状态失败:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchSystemStatus()
})
</script>

<style scoped>
.home {
  width: 100%;
  margin: 0;
}

.welcome-section {
  margin-bottom: var(--space-8);
  text-align: center;
  padding: var(--space-10) var(--space-4);
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border-radius: var(--radius-xl);
  color: white;
  box-shadow: var(--shadow-lg);
}

.welcome-actions {
  margin-top: var(--space-4);
  display: flex;
  gap: var(--space-3);
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.updated-at {
  font-size: 0.875rem;
  opacity: 0.9;
}

.error-message {
  margin-top: var(--space-3);
  color: #fee2e2;
  font-weight: 600;
}

.quick-filter {
  margin-bottom: var(--space-6);
}

.quick-filter input {
  width: 100%;
  max-width: 520px;
  border: 1px solid var(--border-dark);
  border-radius: var(--radius-md);
  padding: 0.65rem 0.8rem;
}

.welcome-section h1 {
  font-size: 2.5rem;
  margin-bottom: var(--space-4);
  font-weight: 700;
}

.welcome-description {
  font-size: 1.25rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.feature-card {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  transition: all var(--transition-normal);
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-light);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: var(--space-4);
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: var(--space-2);
  color: var(--text-primary);
}

.feature-card p {
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.status-section {
  margin-bottom: var(--space-8);
}

.status-section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: var(--space-4);
  color: var(--text-primary);
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
}

.status-card {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  text-align: center;
}

.status-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: var(--space-2);
}

.status-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .welcome-section h1 {
    font-size: 2rem;
  }

  .feature-cards {
    grid-template-columns: 1fr;
  }

  .status-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .status-cards {
    grid-template-columns: 1fr;
  }
}
</style>
