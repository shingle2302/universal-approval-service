<template>
  <div class="process-management">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <router-link to="/" class="breadcrumb-item">首页</router-link>
      <span class="breadcrumb-separator">/</span>
      <span class="breadcrumb-item active">流程管理</span>
    </div>
    
    <!-- 页面标题 -->
    <h2 class="page-title">流程管理</h2>
    
    <!-- 标签页 -->
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.name }}
      </button>
    </div>
    
    <!-- 发起审批 -->
    <div v-if="activeTab === 'start'" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">发起新审批流程</h3>
        </div>
        <div class="card-body">
          <form @submit.prevent="handleStartProcess" class="form">
            <div class="form-group">
              <label class="form-label">业务键</label>
              <input 
                type="text" 
                v-model="startForm.businessKey" 
                class="form-control"
                placeholder="请输入业务键，如：LEAVE_123456"
              />
            </div>
            <div class="form-group">
              <label class="form-label">审批模板 *</label>
              <select
                v-model="startForm.templateCode"
                class="form-control"
                @change="onTemplateChange"
                required
              >
                <option value="">请选择审批模板</option>
                <option v-for="t in templates" :key="t.templateCode" :value="t.templateCode">
                  {{ t.templateName }} ({{ t.templateCode }})
                </option>
              </select>
              <div v-if="templates.length === 0" style="color: #666; font-size: 12px; margin-top: 4px;">
                暂无已部署的模板
              </div>
              <div style="color: #999; font-size: 11px; margin-top: 4px;">
                当前有 {{ templates.length }} 个已部署模板
              </div>
            </div>
            <div v-if="startForm.templateCode === 'LEAVE_001'" class="leave-form">
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">请假天数</label>
                  <input 
                    type="number" 
                    v-model.number="startForm.variables.leave_days" 
                    class="form-control"
                    min="1"
                    max="30"
                  />
                </div>
                <div class="form-group">
                  <label class="form-label">直接主管</label>
                  <input 
                    type="text" 
                    v-model="startForm.variables.direct_manager" 
                    class="form-control"
                    placeholder="如：manager1"
                  />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">上级主管</label>
                  <input 
                    type="text" 
                    v-model="startForm.variables.senior_manager" 
                    class="form-control"
                    placeholder="如：director1"
                  />
                </div>
                <div class="form-group">
                  <label class="form-label">HR</label>
                  <input 
                    type="text" 
                    v-model="startForm.variables.hr" 
                    class="form-control"
                    placeholder="如：hr1"
                  />
                </div>
              </div>
            </div>
            <div class="form-actions">
              <button 
                type="submit" 
                class="btn btn-primary"
                :disabled="!startForm.templateCode || starting"
              >
                {{ starting ? '发起中...' : '发起审批' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 审批记录 -->
    <div v-if="activeTab === 'history'" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">查询审批记录</h3>
        </div>
        <div class="card-body">
          <div class="search-form">
            <div class="form-group">
              <label class="form-label">业务键</label>
              <input 
                type="text" 
                v-model="historyBusinessKey" 
                class="form-control"
                placeholder="请输入业务键"
                style="width: 300px"
              />
            </div>
            <button 
              class="btn btn-primary" 
              @click="fetchHistory"
              style="align-self: end"
            >
              查询
            </button>
          </div>
          <div class="history-records" style="margin-top: var(--space-6)">
            <div v-if="historyLoading" class="loading">
              加载中...
            </div>
            <div v-else-if="historyRecords.length === 0" class="empty">
              <div class="empty-icon">📋</div>
              <div class="empty-title">暂无审批记录</div>
              <div class="empty-description">请输入业务键查询审批记录</div>
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>操作</th>
                  <th>状态</th>
                  <th>操作人</th>
                  <th>意见</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in historyRecords" :key="record.id">
                  <td>{{ record.id }}</td>
                  <td>
                    <span :class="['tag', getOperationTagClass(record.operation)]">
                      {{ record.operation }}
                    </span>
                  </td>
                  <td>
                    <span :class="['tag', getStatusTagClass(record.status)]">
                      {{ record.status }}
                    </span>
                  </td>
                  <td>{{ record.operator || '系统' }}</td>
                  <td>{{ record.comments || '-' }}</td>
                  <td>{{ formatDate(record.createTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 流程实例 -->
    <div v-if="activeTab === 'instance'" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">流程实例管理</h3>
        </div>
        <div class="card-body">
          <div class="search-form">
            <div class="form-group">
              <label class="form-label">业务键</label>
              <input 
                type="text" 
                v-model="processBusinessKey" 
                class="form-control"
                placeholder="请输入业务键"
                style="width: 300px"
              />
            </div>
            <div class="search-buttons">
              <button 
                class="btn btn-primary" 
                @click="fetchProcessInstance"
              >
                按业务键查询
              </button>
              <button 
                class="btn btn-secondary" 
                @click="fetchAllProcessInstances"
                style="margin-left: var(--space-2)"
              >
                查看所有实例
              </button>
            </div>
          </div>
          <div class="process-instances" style="margin-top: var(--space-6)">
            <div v-if="processLoading" class="loading">
              加载中...
            </div>
            <div v-else-if="processInstances.length === 0" class="empty">
              <div class="empty-icon">📊</div>
              <div class="empty-title">暂无流程实例</div>
              <div class="empty-description">当前没有运行中的流程实例</div>
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>流程实例ID</th>
                  <th>业务键</th>
                  <th>名称</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="instance in processInstances" :key="instance.id">
                  <td>{{ instance.id }}</td>
                  <td>{{ instance.businessKey }}</td>
                  <td>{{ instance.name }}</td>
                  <td>
                    <span class="tag tag-info">{{ instance.status || '运行中' }}</span>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button 
                        class="btn btn-secondary" 
                        @click="viewProcessVariables(instance)"
                      >
                        查看变量
                      </button>
                      <button 
                        class="btn btn-primary" 
                        @click="viewProcessDetail(instance)"
                        style="margin-left: var(--space-2)"
                      >
                        查看详情
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 变量模态框 -->
    <div v-if="variablesModalVisible" class="modal-overlay" @click="variablesModalVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>流程变量</h3>
          <button class="modal-close" @click="variablesModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="Object.keys(processVariables).length === 0" class="empty">
            <div class="empty-title">暂无变量</div>
          </div>
          <div v-else class="variables-list">
            <div v-for="(value, key) in processVariables" :key="key" class="variable-item">
              <div class="variable-key">{{ key }}</div>
              <div class="variable-value">{{ value }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="variablesModalVisible = false">
            关闭
          </button>
        </div>
      </div>
    </div>
    
    <!-- 详情模态框 -->
    <div v-if="detailModalVisible" class="modal-overlay" @click="detailModalVisible = false">
      <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h3>流程实例详情</h3>
          <button class="modal-close" @click="detailModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <div class="detail-label">流程实例ID</div>
                <div class="detail-value">{{ selectedProcess?.id }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">业务键</div>
                <div class="detail-value">{{ selectedProcess?.businessKey }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">流程定义ID</div>
                <div class="detail-value">{{ selectedProcess?.processDefinitionId }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">流程名称</div>
                <div class="detail-value">{{ selectedProcess?.name }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">状态</div>
                <div class="detail-value">
                  <span class="tag tag-info">{{ selectedProcess?.status || '运行中' }}</span>
                </div>
              </div>
              <div class="detail-item">
                <div class="detail-label">当前处理人</div>
                <div class="detail-value">{{ currentAssignee || '无' }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-section" style="margin-top: var(--space-6)">
            <h4>流程变量</h4>
            <div v-if="Object.keys(selectedProcessVariables).length === 0" class="empty">
              <div class="empty-title">暂无变量</div>
            </div>
            <div v-else class="variables-list">
              <div v-for="(value, key) in selectedProcessVariables" :key="key" class="variable-item">
                <div class="variable-key">{{ key }}</div>
                <div class="variable-value">{{ value }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-section" style="margin-top: var(--space-6)">
            <h4>审批历史</h4>
            <div v-if="selectedProcessHistory.length === 0" class="empty">
              <div class="empty-title">暂无审批记录</div>
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>操作</th>
                  <th>状态</th>
                  <th>操作人</th>
                  <th>意见</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in selectedProcessHistory" :key="record.id">
                  <td>{{ record.id }}</td>
                  <td>
                    <span :class="['tag', getOperationTagClass(record.operation)]">
                      {{ record.operation }}
                    </span>
                  </td>
                  <td>
                    <span :class="['tag', getStatusTagClass(record.status)]">
                      {{ record.status }}
                    </span>
                  </td>
                  <td>{{ record.operator || '系统' }}</td>
                  <td>{{ record.comments || '-' }}</td>
                  <td>{{ formatDate(record.createTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="detailModalVisible = false">
            关闭
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

const activeTab = ref('start')
const templates = ref([])

const startForm = ref({
  businessKey: '',
  templateCode: '',
  variables: {
    leave_days: 3,
    direct_manager: 'manager1',
    senior_manager: 'director1',
    hr: 'hr1'
  }
})

const starting = ref(false)

const historyBusinessKey = ref('')
const historyRecords = ref([])
const historyLoading = ref(false)

const processBusinessKey = ref('')
const processInstances = ref([])
const processLoading = ref(false)

const variablesModalVisible = ref(false)
const processVariables = ref({})

const detailModalVisible = ref(false)
const selectedProcess = ref(null)
const selectedProcessVariables = ref({})
const selectedProcessHistory = ref([])
const currentAssignee = ref('')

const tabs = [
  { key: 'start', name: '发起审批' },
  { key: 'history', name: '审批记录' },
  { key: 'instance', name: '流程实例' }
]

const API_BASE = '/api/approval'

const fetchTemplates = async () => {
  console.log('fetchTemplates called')
  try {
    const response = await axios.get(`${API_BASE}/template`)
    console.log('API response:', response.data)
    if (response.data.code === 200) {
      const deployedTemplates = (response.data.data || []).filter(template => template.status === 'DEPLOYED')
      console.log('Deployed templates:', deployedTemplates)
      templates.value = deployedTemplates
    }
  } catch (error) {
    console.error('Error fetching templates:', error)
    message.error('获取模板列表失败: ' + error.message)
  }
}

const onTemplateChange = (event) => {
  const value = event.target.value
  if (value === 'LEAVE_001') {
    startForm.value.businessKey = 'LEAVE_' + Date.now()
  }
}

const handleStartProcess = async () => {
  if (!startForm.value.businessKey) {
    message.warning('请输入业务键')
    return
  }
  if (!startForm.value.templateCode) {
    message.warning('请选择审批模板')
    return
  }
  starting.value = true
  try {
    const response = await axios.post(`${API_BASE}/process`, {
      templateCode: startForm.value.templateCode,
      businessKey: startForm.value.businessKey,
      variables: startForm.value.variables
    })
    if (response.data.code === 200) {
      message.success('流程发起成功！流程实例ID: ' + response.data.data)
      startForm.value.businessKey = ''
    } else {
      message.error(response.data.message || '流程发起失败')
    }
  } catch (error) {
    message.error('流程发起失败: ' + (error.response?.data?.message || error.message))
  } finally {
    starting.value = false
  }
}

const fetchHistory = async () => {
  if (!historyBusinessKey.value) {
    message.warning('请输入业务键')
    return
  }
  historyLoading.value = true
  try {
    const response = await axios.get(`${API_BASE}/history/log`, {
      params: { businessKey: historyBusinessKey.value }
    })
    if (response.data.code === 200) {
      historyRecords.value = response.data.data || []
      if (historyRecords.value.length === 0) {
        message.info('未找到审批记录')
      }
    } else {
      message.error(response.data.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败: ' + error.message)
  } finally {
    historyLoading.value = false
  }
}

const fetchProcessInstance = async () => {
  if (!processBusinessKey.value) {
    message.warning('请输入业务键')
    return
  }
  processLoading.value = true
  try {
    let url = `${API_BASE}/process`
    if (processBusinessKey.value) {
      url += `?businessKey=${encodeURIComponent(processBusinessKey.value)}`
    }
    const response = await axios.get(url)
    if (response.data.code === 200) {
      processInstances.value = response.data.data || []
      // 添加状态信息和名称处理
      processInstances.value.forEach(instance => {
        instance.status = 'RUNNING' // 默认状态
        // 如果没有名称，使用业务键作为名称
        if (!instance.name) {
          instance.name = instance.businessKey || '未命名流程'
        }
      })
      if (processInstances.value.length === 0) {
        message.info('未找到流程实例')
      }
    } else {
      message.error(response.data.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败: ' + error.message)
  } finally {
    processLoading.value = false
  }
}

const fetchAllProcessInstances = async () => {
  processLoading.value = true
  try {
    // 调用不带参数的API获取所有流程实例
    const response = await axios.get(`${API_BASE}/process`)
    if (response.data.code === 200) {
      processInstances.value = response.data.data || []
      // 添加状态信息和名称处理
      processInstances.value.forEach(instance => {
        instance.status = 'RUNNING' // 默认状态
        // 如果没有名称，使用业务键作为名称
        if (!instance.name) {
          instance.name = instance.businessKey || '未命名流程'
        }
      })
      if (processInstances.value.length === 0) {
        message.info('未找到流程实例')
      }
    } else {
      message.error(response.data.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败: ' + error.message)
  } finally {
    processLoading.value = false
  }
}

const viewProcessVariables = async (record) => {
  try {
    const response = await axios.get(`${API_BASE}/process/${record.id}/variables`)
    if (response.data.code === 200) {
      processVariables.value = response.data.data || {}
      variablesModalVisible.value = true
    } else {
      message.error(response.data.message || '获取变量失败')
    }
  } catch (error) {
    message.error('获取变量失败: ' + error.message)
  }
}

const viewProcessDetail = async (record) => {
  selectedProcess.value = record
  // 确保有名称
  if (!record.name) {
    record.name = record.businessKey || '未命名流程'
  }
  detailModalVisible.value = true
  currentAssignee.value = ''
  
  try {
    // 获取流程变量
    const variablesResponse = await axios.get(`${API_BASE}/process/${record.id}/variables`)
    if (variablesResponse.data.code === 200) {
      selectedProcessVariables.value = variablesResponse.data.data || {}
    }
    
    // 获取审批历史
    if (record.businessKey) {
      const historyResponse = await axios.get(`${API_BASE}/history/log`, {
        params: { businessKey: record.businessKey }
      })
      if (historyResponse.data.code === 200) {
        selectedProcessHistory.value = historyResponse.data.data || []
      }
    }
    
    // 获取当前处理人
    try {
      const tasksResponse = await axios.get(`${API_BASE}/task`)
      if (tasksResponse.data.code === 200) {
        const tasks = tasksResponse.data.data || []
        const processTasks = tasks.filter(task => task.processInstanceId === record.id)
        if (processTasks.length > 0) {
          currentAssignee.value = processTasks[0].assignee || '无'
        }
      }
    } catch (error) {
      console.log('获取当前处理人失败:', error)
    }
  } catch (error) {
    message.error('获取详情失败: ' + error.message)
  }
}

const getStatusTagClass = (status) => {
  switch (status) {
    case 'APPROVE': return 'tag-success'
    case 'REJECT': return 'tag-error'
    case 'DELEGATED': return 'tag-info'
    case 'ROLLBACK': return 'tag-warning'
    default: return 'tag-info'
  }
}

const getOperationTagClass = (operation) => {
  switch (operation) {
    case 'START': return 'tag-info'
    case 'COMPLETE': return 'tag-success'
    case 'DELEGATE': return 'tag-info'
    case 'ROLLBACK': return 'tag-warning'
    default: return 'tag-info'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.process-management {
  width: 100%;
  margin: 0;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-6);
}

/* 标签页 */
.tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  border-bottom: 1px solid var(--border);
}

.tab-button {
  padding: var(--space-3) var(--space-4);
  border: none;
  background: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.tab-button:hover {
  color: var(--text-primary);
  background-color: var(--bg-tertiary);
}

.tab-button.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  background-color: var(--bg-primary);
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.form-row .form-group {
  flex: 1;
}

.form-actions {
  margin-top: var(--space-6);
  display: flex;
  justify-content: flex-end;
}

/* 搜索表单 */
.search-form {
  display: flex;
  gap: var(--space-4);
  align-items: end;
}

.search-buttons {
  display: flex;
  gap: var(--space-2);
}

/* 变量列表 */
.variables-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.variable-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-2) var(--space-3);
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
}

.variable-key {
  font-weight: 500;
  color: var(--text-primary);
}

.variable-value {
  color: var(--text-secondary);
}

/* 详情样式 */
.detail-section {
  margin-bottom: var(--space-6);
}

.detail-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--space-4);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.detail-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.detail-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  word-break: break-all;
}

/* 模态框 */
.modal-large {
  max-width: 800px;
  width: 95%;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .form-group {
    width: 100%;
  }
  
  .search-buttons {
    flex-direction: column;
  }
  
  .search-buttons .btn {
    width: 100%;
  }
  
  .search-buttons .btn:not(:first-child) {
    margin-left: 0 !important;
    margin-top: var(--space-2);
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
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>