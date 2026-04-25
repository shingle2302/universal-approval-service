<template>
  <div class="home">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span class="breadcrumb-item">首页</span>
    </div>
    
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1>欢迎使用通用审批系统</h1>
      <p class="welcome-description">
        高效、智能的审批流程管理平台，让审批更简单
      </p>
    </div>
    
    <!-- 功能卡片 -->
    <div class="feature-cards">
      <div class="feature-card">
        <div class="feature-icon">📋</div>
        <h3>模板管理</h3>
        <p>创建和管理审批流程模板</p>
        <router-link to="/template" class="btn btn-primary">
          进入管理
        </router-link>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">✅</div>
        <h3>任务管理</h3>
        <p>处理待办审批任务</p>
        <router-link to="/task" class="btn btn-primary">
          查看任务
        </router-link>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">📊</div>
        <h3>流程管理</h3>
        <p>发起审批和管理流程实例</p>
        <router-link to="/process" class="btn btn-primary">
          发起审批
        </router-link>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">🎨</div>
        <h3>流程设计</h3>
        <p>可视化设计审批流程</p>
        <router-link to="/designer" class="btn btn-primary">
          开始设计
        </router-link>
      </div>
    </div>
    
    <!-- 系统状态 -->
    <div class="status-section">
      <h2>系统状态</h2>
      <div class="status-cards">
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
import { ref, onMounted } from 'vue'
import axios from 'axios'

const templateCount = ref(0)
const taskCount = ref(0)
const processCount = ref(0)

const fetchSystemStatus = async () => {
  try {
    // 获取模板数量
    const templateResponse = await axios.get('/api/approval/template')
    if (templateResponse.data.code === 200) {
      templateCount.value = templateResponse.data.data?.filter(t => t.status === 'DEPLOYED').length || 0
    }
    
    // 获取任务数量
    const taskResponse = await axios.get('/api/approval/task')
    if (taskResponse.data.code === 200) {
      taskCount.value = taskResponse.data.data?.length || 0
    }
    
    // 获取流程实例数量
    const processResponse = await axios.get('/api/approval/process')
    if (processResponse.data.code === 200) {
      processCount.value = processResponse.data.data?.length || 0
    }
  } catch (error) {
    console.log('获取系统状态失败:', error)
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