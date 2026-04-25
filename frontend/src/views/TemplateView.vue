<template>
  <div class="template">
    <div class="template-header">
      <h2>模板管理</h2>
      <div class="template-actions">
        <a-button type="primary" @click="createModalVisible = true">
          <template #icon><PlusOutlined /></template>
          新建模板
        </a-button>
      </div>
    </div>

    <a-table
      :columns="columns"
      :data-source="templates"
      :loading="loading"
      row-key="templateCode"
    >
      <template #headerCell="{ column }">
        <div class="table-header">{{ column.title }}</div>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ record.status === 'DEPLOYED' ? '已部署' : record.status === 'CREATED' ? '已创建' : record.status }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="action-buttons">
            <a-button size="small" @click="handleView(record)">
              <template #icon><EyeOutlined /></template>
              查看
            </a-button>
            <a-button size="small" @click="handleEdit(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button 
              size="small" 
              :loading="deploying === record.templateCode"
              v-if="record.status !== 'DEPLOYED'"
              @click="handleDeploy(record)"
            >
              <template #icon><CloudUploadOutlined /></template>
              部署
            </a-button>
            <a-button 
              size="small" 
              danger 
              @click="handleDelete(record)"
            >
              <template #icon><DeleteOutlined /></template>
              删除
            </a-button>
          </div>
        </template>
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="createModalVisible" title="新建模板" @ok="handleCreate" @cancel="createModalVisible = false">
      <a-form :model="createForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="模板编码" name="templateCode">
          <a-input v-model:value="createForm.templateCode" placeholder="请输入模板编码" />
        </a-form-item>
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="createForm.templateName" placeholder="请输入模板名称" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="editModalVisible" title="编辑模板" @ok="handleUpdate" @cancel="editModalVisible = false">
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="editForm.templateName" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="配置JSON" name="configJson">
          <a-textarea v-model:value="editForm.configJson" :rows="10" placeholder="请输入流程配置JSON" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="viewModalVisible" :title="'流程预览: ' + viewTemplateName" width="1400px" @ok="viewModalVisible = false" :footer="null">
      <div class="flow-preview">
        <div class="flow-canvas">
          <svg class="flow-svg" width="100%" height="600">
            <g class="connections">
              <template v-for="(connection, index) in viewConnections" :key="'view-conn-' + index">
                <path
                  :d="connection.path"
                  :stroke="connection.color"
                  stroke-width="2"
                  fill="none"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  :d="connection.arrow"
                  :stroke="connection.color"
                  stroke-width="2"
                  fill="none"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <text
                  v-if="connection.label"
                  :x="connection.labelX"
                  :y="connection.labelY"
                  font-size="11"
                  :fill="connection.color"
                  text-anchor="middle"
                  font-weight="500"
                  style="text-shadow: 1px 1px 2px rgba(255,255,255,0.8)"
                >{{ connection.label }}</text>
              </template>
            </g>
          </svg>
          <div class="flow-nodes">
            <div
              v-for="node in viewNodes"
              :key="node.id"
              class="flow-node"
              :class="[node.type]"
              :style="{ left: node.x + 'px', top: node.y + 'px' }"
            >
              <div class="flow-node-content">
                <div class="flow-node-header">
                  <div class="flow-node-icon" :class="node.type">
                    <template v-if="node.type === 'startEvent'">
                      <div class="event-circle"></div>
                    </template>
                    <template v-else-if="node.type === 'endEvent'">
                      <div class="event-circle">
                        <div class="end-marker"></div>
                      </div>
                    </template>
                    <template v-else-if="node.type === 'userTask'">
                      <div class="task-rectangle">👤</div>
                    </template>
                    <template v-else-if="node.type === 'exclusiveGateway'">
                      <div class="gateway-diamond"></div>
                    </template>
                  </div>
                  <div class="flow-node-title">{{ node.name }}</div>
                </div>
                <div class="flow-node-body">
                  {{ getNodeTypeName(node.type) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  CloudUploadOutlined, 
  EyeOutlined 
} from '@ant-design/icons-vue'
import { layoutFlowNodes, calculateViewConnections } from '../utils/flowLayout.js'

const templates = ref([])
const loading = ref(false)
const deploying = ref(null)
const deleting = ref(null)
const creating = ref(false)
const updating = ref(false)
const createModalVisible = ref(false)
const editModalVisible = ref(false)
const viewModalVisible = ref(false)
const viewTemplateName = ref('')
const viewNodes = ref([])
const viewConnections = ref([])

const createForm = reactive({
  templateCode: '',
  templateName: ''
})

const editForm = reactive({
  templateCode: '',
  templateName: '',
  configJson: ''
})

const columns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode' },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName' },
  { title: '版本', dataIndex: 'version', key: 'version' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' }
]

const API_BASE = '/api/approval/template'

const getStatusColor = (status) => {
  switch (status) {
    case 'CREATED': return 'blue'
    case 'DEPLOYED': return 'green'
    case 'UPDATED': return 'orange'
    default: return 'default'
  }
}

const getNodeTypeName = (type) => {
  switch (type) {
    case 'startEvent': return '开始事件'
    case 'endEvent': return '结束事件'
    case 'userTask': return '用户任务'
    case 'exclusiveGateway': return '排他网关'
    default: return '未知节点'
  }
}

const fetchTemplates = async () => {
  loading.value = true
  try {
    const response = await axios.get(API_BASE)
    if (response.data.code === 200) {
      templates.value = response.data.data
    } else {
      message.error(response.data.message || '获取模板列表失败')
    }
  } catch (error) {
    message.error('获取模板列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createForm.templateCode || !createForm.templateName) {
    message.warning('请填写模板编码和名称')
    return
  }
  creating.value = true
  try {
    const response = await axios.post(API_BASE, {
      templateCode: createForm.templateCode,
      templateName: createForm.templateName
    })
    if (response.data.code === 200) {
      message.success('创建成功')
      createModalVisible.value = false
      createForm.templateCode = ''
      createForm.templateName = ''
      fetchTemplates()
    } else {
      message.error(response.data.message || '创建失败')
    }
  } catch (error) {
    message.error('创建失败: ' + (error.response?.data?.message || error.message))
  } finally {
    creating.value = false
  }
}

const handleEdit = (record) => {
  editForm.templateCode = record.templateCode
  editForm.templateName = record.templateName
  editForm.configJson = record.configJson || ''
  editModalVisible.value = true
}

const handleUpdate = async () => {
  if (!editForm.templateName) {
    message.warning('请填写模板名称')
    return
  }
  updating.value = true
  try {
    let configJson = editForm.configJson
    if (configJson) {
      JSON.parse(configJson)
    }
    const response = await axios.put(`${API_BASE}/${editForm.templateCode}`, {
      templateName: editForm.templateName,
      version: 1,
      configJson: configJson
    })
    if (response.data.code === 200) {
      message.success('更新成功')
      editModalVisible.value = false
      fetchTemplates()
    } else {
      message.error(response.data.message || '更新失败')
    }
  } catch (error) {
    message.error('更新失败: ' + (error.response?.data?.message || error.message))
  } finally {
    updating.value = false
  }
}

const handleDeploy = async (record) => {
  deploying.value = record.templateCode
  try {
    const response = await axios.post(`${API_BASE}/${record.templateCode}/deploy`)
    if (response.data.code === 200) {
      message.success('部署成功')
      fetchTemplates()
    } else {
      message.error(response.data.message || '部署失败')
    }
  } catch (error) {
    message.error('部署失败: ' + (error.response?.data?.message || error.message))
  } finally {
    deploying.value = null
  }
}

const handleDelete = async (record) => {
  try {
    const response = await axios.delete(`${API_BASE}/${record.templateCode}`)
    if (response.data.code === 200) {
      message.success('删除成功')
      fetchTemplates()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    message.error('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

const handleView = (record) => {
  viewTemplateName.value = record.templateName
  viewNodes.value = []
  viewConnections.value = []

  if (record.configJson) {
    try {
      const config = JSON.parse(record.configJson)
      if (config.nodes && Array.isArray(config.nodes)) {
        const layoutNodes = layoutFlowNodes(config.nodes)
        viewNodes.value = layoutNodes
        viewConnections.value = calculateViewConnections(layoutNodes)
      }
    } catch (error) {
      console.error('解析流程配置失败:', error)
      message.error('解析流程配置失败')
    }
  }

  viewModalVisible.value = true
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.template {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: 100vh;
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.template-header h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
}

.template-actions {
  display: flex;
  gap: 12px;
}

.table-header {
  font-weight: 600;
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.flow-preview {
  padding: 20px;
  background: var(--bg-primary);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.flow-canvas {
  position: relative;
  width: 100%;
  height: 600px;
  overflow: auto;
  background: var(--bg-secondary);
  border-radius: 4px;
}

.flow-svg {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.flow-nodes {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
}

.flow-node {
  position: absolute;
  width: 150px;
  height: 80px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.flow-node:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.flow-node-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  box-sizing: border-box;
}

.flow-node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.flow-node-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: var(--bg-secondary);
}

.flow-node-icon .event-circle {
  width: 24px;
  height: 24px;
  border: 2px solid #1890ff;
  border-radius: 50%;
  background: #e6f7ff;
}

.flow-node-icon .end-marker {
  width: 12px;
  height: 12px;
  background: #ff4d4f;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.flow-node-icon .task-rectangle {
  width: 32px;
  height: 24px;
  border: 2px solid #52c41a;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  background: #f6ffed;
}

.flow-node-icon .gateway-diamond {
  width: 24px;
  height: 24px;
  border: 2px solid #fa8c16;
  transform: rotate(45deg);
  background: #fff7e6;
}

.flow-node-icon.startEvent .event-circle {
  border-color: #52c41a;
  background: #f6ffed;
}

.flow-node-icon.endEvent .event-circle {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.flow-node-icon.userTask .task-rectangle {
  border-color: #1890ff;
  background: #e6f7ff;
}

.flow-node-icon.exclusiveGateway .gateway-diamond {
  border-color: #fa8c16;
  background: #fff7e6;
}

.flow-node-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
}

.flow-node-body {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .template {
    padding: 12px;
  }

  .template-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .template-actions {
    justify-content: center;
  }
}
</style>