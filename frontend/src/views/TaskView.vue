<template>
  <div class="task-management">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <router-link to="/" class="breadcrumb-item">首页</router-link>
      <span class="breadcrumb-separator">/</span>
      <span class="breadcrumb-item active">任务管理</span>
    </div>
    
    <!-- 页面标题 -->
    <h2 class="page-title">任务管理</h2>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-form">
        <div class="form-group">
          <label class="form-label">处理人</label>
          <select 
            v-model="searchAssignee" 
            class="form-control"
            @change="fetchTasks"
          >
            <option value="">查看所有任务</option>
            <option value="manager1">manager1</option>
            <option value="manager2">manager2</option>
            <option value="director1">director1</option>
            <option value="hr1">hr1</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="fetchTasks">
          刷新任务
        </button>
      </div>
    </div>
    
    <!-- 任务列表 -->
    <div class="task-list">
      <div v-if="loading" class="loading">
        加载中...
      </div>
      <div v-else-if="tasks.length === 0" class="empty">
        <div class="empty-icon">📋</div>
        <div class="empty-title">暂无任务</div>
        <div class="empty-description">当前没有待处理的审批任务</div>
      </div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>任务ID</th>
            <th>任务名称</th>
            <th>处理人</th>
            <th>状态</th>
            <th>流程实例ID</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in tasks" :key="task.id">
            <td>{{ task.id }}</td>
            <td>{{ task.name }}</td>
            <td>{{ task.assignee || '未分配' }}</td>
            <td>
              <span class="tag tag-info">{{ task.status || 'ASSIGNED' }}</span>
            </td>
            <td>{{ task.processInstanceId }}</td>
            <td>{{ formatDate(task.createTime) }}</td>
            <td>
              <div class="action-buttons">
                <button 
                  class="btn btn-primary" 
                  @click="showCompleteModal(task)"
                >
                  审批
                </button>
                <button 
                  class="btn btn-secondary" 
                  @click="showDelegateModal(task)"
                  style="margin-left: var(--space-2)"
                >
                  转交
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 审批模态框 -->
    <div v-if="completeModalVisible" class="modal-overlay" @click="completeModalVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>审批任务</h3>
          <button class="modal-close" @click="completeModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">任务名称</label>
            <input 
              type="text" 
              class="form-control" 
              :value="selectedTask?.name" 
              disabled
            />
          </div>
          <div class="form-group">
            <label class="form-label">审批结果</label>
            <select v-model="completeForm.result" class="form-control">
              <option value="APPROVE">通过</option>
              <option value="REJECT">拒绝</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">审批意见</label>
            <textarea 
              v-model="completeForm.comments" 
              class="form-control" 
              rows="3"
              placeholder="请输入审批意见"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="completeModalVisible = false">
            取消
          </button>
          <button 
            class="btn btn-primary" 
            @click="handleCompleteTask"
            :disabled="completing"
          >
            {{ completing ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 转交模态框 -->
    <div v-if="delegateModalVisible" class="modal-overlay" @click="delegateModalVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>转交任务</h3>
          <button class="modal-close" @click="delegateModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">任务名称</label>
            <input 
              type="text" 
              class="form-control" 
              :value="selectedTask?.name" 
              disabled
            />
          </div>
          <div class="form-group">
            <label class="form-label">当前处理人</label>
            <input 
              type="text" 
              class="form-control" 
              :value="selectedTask?.assignee" 
              disabled
            />
          </div>
          <div class="form-group">
            <label class="form-label">转交对象</label>
            <select v-model="delegateForm.delegateTo" class="form-control">
              <option value="manager1">manager1</option>
              <option value="manager2">manager2</option>
              <option value="director1">director1</option>
              <option value="hr1">hr1</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="delegateModalVisible = false">
            取消
          </button>
          <button 
            class="btn btn-primary" 
            @click="handleDelegateTask"
            :disabled="delegating"
          >
            {{ delegating ? '转交流理中...' : '转交' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const tasks = ref([])
const loading = ref(false)
const searchAssignee = ref('')

const completeModalVisible = ref(false)
const delegateModalVisible = ref(false)
const selectedTask = ref(null)
const completing = ref(false)
const delegating = ref(false)

const completeForm = ref({
  result: 'APPROVE',
  comments: ''
})

const delegateForm = ref({
  delegateTo: 'manager1'
})

const API_BASE = '/api/approval/task'

const fetchTasks = async () => {
  loading.value = true
  try {
    let url = API_BASE
    if (searchAssignee.value) {
      url += `?assignee=${encodeURIComponent(searchAssignee.value)}`
    }
    const response = await axios.get(url)
    if (response.data.code === 200) {
      tasks.value = response.data.data || []
    } else {
      message.error(response.data.message || '获取任务列表失败')
    }
  } catch (error) {
    message.error('获取任务列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const showCompleteModal = (task) => {
  selectedTask.value = task
  completeForm.value = {
    result: 'APPROVE',
    comments: ''
  }
  completeModalVisible.value = true
}

const showDelegateModal = (task) => {
  selectedTask.value = task
  delegateForm.value = {
    delegateTo: task.assignee === 'manager1' ? 'manager2' : 'manager1'
  }
  delegateModalVisible.value = true
}

const handleCompleteTask = async () => {
  if (!selectedTask.value) return
  
  completing.value = true
  try {
    const response = await axios.post(`${API_BASE}/${selectedTask.value.id}`, {
      assignee: selectedTask.value.assignee,
      result: completeForm.value.result,
      comments: completeForm.value.comments
    })
    if (response.data.code === 200) {
      message.success('审批成功')
      completeModalVisible.value = false
      fetchTasks()
    } else {
      message.error(response.data.message || '审批失败')
    }
  } catch (error) {
    message.error('审批失败: ' + (error.response?.data?.message || error.message))
  } finally {
    completing.value = false
  }
}

const handleDelegateTask = async () => {
  if (!selectedTask.value) return
  
  delegating.value = true
  try {
    const response = await axios.post(`${API_BASE}/${selectedTask.value.id}/delegate`, {
      assignee: selectedTask.value.assignee,
      delegateTo: delegateForm.value.delegateTo
    })
    if (response.data.code === 200) {
      message.success('转交成功')
      delegateModalVisible.value = false
      fetchTasks()
    } else {
      message.error(response.data.message || '转交失败')
    }
  } catch (error) {
    message.error('转交失败: ' + (error.response?.data?.message || error.message))
  } finally {
    delegating.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.task-management {
  width: 100%;
  margin: 0;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-6);
}

.search-section {
  margin-bottom: var(--space-6);
}

.search-form {
  display: flex;
  gap: var(--space-4);
  align-items: end;
}

.search-form .form-group {
  flex: 1;
  max-width: 300px;
}

.task-list {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  overflow: hidden;
}

.action-buttons {
  display: flex;
  gap: var(--space-2);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 500px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}

.modal-close:hover {
  background-color: var(--bg-tertiary);
}

.modal-body {
  padding: var(--space-5);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border);
  background-color: var(--bg-tertiary);
  border-bottom-left-radius: var(--radius-lg);
  border-bottom-right-radius: var(--radius-lg);
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .form-group {
    max-width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .btn {
    width: 100%;
  }
  
  .action-buttons .btn:not(:first-child) {
    margin-left: 0 !important;
    margin-top: var(--space-2);
  }
}
</style>