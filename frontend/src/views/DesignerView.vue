<template>
  <div class="designer">
        <div class="page-title">
          <div class="page-title-left">
            <div class="page-title-main">流程设计器</div>
            <div class="page-title-description">可视化设计审批流程，支持 BPMN 2.0 标准</div>
          </div>
          <div class="page-title-right">
            <div class="breadcrumb">
              <a href="/" class="breadcrumb-item">首页</a>
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item active">流程设计</span>
            </div>
          </div>
        </div>
        
        <div class="page-content">
          <div class="card">
            <div class="card-body">
              <div class="designer-controls">
                <button class="btn btn-primary" @click="clearFlow">清空画布</button>
                <button class="btn btn-secondary" @click="saveTemplate" style="margin-left: 8px">保存为模板</button>
                <button class="btn btn-secondary" @click="showLoadTemplateModal" style="margin-left: 8px">选择模板</button>
                <button class="btn btn-secondary" @click="showAddNodeModal" style="margin-left: 8px" :disabled="selectedNodeIndex === -1">
                  <span>➕</span> 添加节点
                </button>
                <div class="zoom-controls" style="display: inline-flex; align-items: center; margin-left: 16px">
                  <button class="btn btn-secondary" @click="zoomOut" style="margin-left: 0">-</button>
                  <span class="zoom-level" style="margin: 0 8px; font-size: 14px">{{ Math.round(scale * 100) }}%</span>
                  <button class="btn btn-secondary" @click="zoomIn">+</button>
                  <button class="btn btn-secondary" @click="resetZoom" style="margin-left: 8px">重置缩放</button>
                  <button class="btn btn-secondary" @click="() => { panX.value = 0; panY.value = 0 }" style="margin-left: 8px">重置位置</button>
                </div>
                <button class="btn btn-info" @click="saveAsImage" style="margin-left: 16px">保存为图片</button>
              </div>
              
              <div class="designer-content">
                <div class="designer-canvas" 
                     @drop="onDrop"
                     @dragover="onDragOver"
                     @dragenter="onDragEnter"
                     @dragleave="onDragLeave"
                     @mousedown="startPan"
                >
                  <div class="bpmn-canvas" :style="{ transform: `translate(${panX}px, ${panY}px) scale(${scale})`, transformOrigin: 'center center' }">
                    <svg class="bpmn-svg" width="100%" height="100%">
                      <g class="connections">
                        <template v-for="(connection, index) in connections" :key="'connection-' + index">
                          <path 
                            :d="connection.path" 
                            :stroke="connection.color" 
                            stroke-width="2" 
                            fill="none"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            style="cursor: move"
                            @mousedown="startConnectionDrag(index, $event)"
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
                            :x="connection.labelX" 
                            :y="connection.labelY" 
                            font-size="12" 
                            :fill="connection.color" 
                            text-anchor="middle"
                            font-weight="500"
                            style="text-shadow: 1px 1px 2px rgba(255,255,255,0.8)"
                          >{{ connection.label }}</text>
                        </template>
                      </g>
                    </svg>
                    <div class="nodes">
                      <div 
                        v-for="(node, index) in flowNodes" 
                        :key="node.id"
                        class="bpmn-node"
                        :class="[node.type, { selected: selectedNodeIndex === index }]"
                        :style="{ left: node.x + 'px', top: node.y + 'px' }"
                        @click="selectNode(index, $event)"
                        @mousedown="startDrag(index, $event)"
                      >
                        <!-- 连接点 -->
                        <div class="connection-points">
                          <div 
                            class="connection-point top"
                            @mousedown="startConnectionPointDrag(index, 'top', $event)"
                          ></div>
                          <div 
                            class="connection-point bottom"
                            @mousedown="startConnectionPointDrag(index, 'bottom', $event)"
                          ></div>
                          <div 
                            class="connection-point left"
                            @mousedown="startConnectionPointDrag(index, 'left', $event)"
                          ></div>
                          <div 
                            class="connection-point right"
                            @mousedown="startConnectionPointDrag(index, 'right', $event)"
                          ></div>
                        </div>
                        <div class="node-content">
                          <div class="node-header">
                            <div class="node-icon" :class="node.type">
                              <template v-if="node.type === 'startEvent'">
                                <div class="event-circle">
                                  <span class="icon">▶</span>
                                </div>
                              </template>
                              <template v-else-if="node.type === 'endEvent'">
                                <div class="event-circle">
                                  <span class="icon">■</span>
                                </div>
                              </template>
                              <template v-else-if="node.type === 'userTask'">
                                <div class="task-rectangle">
                                  <span class="icon">👤</span>
                                </div>
                              </template>
                              <template v-else-if="node.type === 'exclusiveGateway'">
                                <div class="gateway-diamond">
                                  <span class="icon">❓</span>
                                </div>
                              </template>
                            </div>
                            <div class="node-title">{{ node.name }}</div>
                          </div>
                        </div>
                        <div class="node-actions">
                          <button @click.stop="editNode(index)" class="action-btn" title="编辑">
                            ✏️
                          </button>
                          <button @click.stop="deleteNode(index)" class="action-btn" title="删除">
                            🗑️
                          </button>
                        </div>
                        <div class="node-ports">
                          <div class="port port-top" @click.stop="addConnection(index, 'top')"></div>
                          <div class="port port-right" @click.stop="addConnection(index, 'right')"></div>
                          <div class="port port-bottom" @click.stop="addConnection(index, 'bottom')"></div>
                          <div class="port port-left" @click.stop="addConnection(index, 'left')"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="designer-sidebar">
                  <div class="sidebar-section">
                    <h4>节点信息</h4>
                    <div v-if="selectedNodeIndex === -1" class="no-selection">
                      <p>请选择一个节点查看详情</p>
                    </div>
                    <div v-else class="node-details">
                      <div class="form-group">
                        <label>节点名称</label>
                        <input 
                          type="text" 
                          v-model="flowNodes[selectedNodeIndex].name" 
                          class="form-control"
                          @input="updateConnections"
                        />
                      </div>
                      <div class="form-group">
                        <label>节点类型</label>
                        <input 
                          type="text" 
                          :value="getNodeTypeName(flowNodes[selectedNodeIndex].type)" 
                          class="form-control" 
                          disabled
                        />
                      </div>
                      <div v-if="flowNodes[selectedNodeIndex].type === 'userTask'" class="form-group">
                        <label>审批人</label>
                        <input 
                          type="text" 
                          v-model="flowNodes[selectedNodeIndex].assignee" 
                          class="form-control"
                        />
                      </div>
                      <div class="form-group">
                        <label>位置</label>
                        <div class="position-inputs">
                          <input 
                            type="number" 
                            v-model.number="flowNodes[selectedNodeIndex].x" 
                            class="form-control"
                            @input="updateConnections"
                          />
                          <input 
                            type="number" 
                            v-model.number="flowNodes[selectedNodeIndex].y" 
                            class="form-control"
                            @input="updateConnections"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="sidebar-section">
                    <h4>工具栏</h4>
                    <div class="palette-items">
                      <div 
                        class="palette-item" 
                        draggable="true" 
                        @dragstart="onPaletteDragStart('startEvent')"
                      >
                        <div class="palette-item-icon">
                          <div class="event-circle">
                            <span class="icon">▶</span>
                          </div>
                        </div>
                        <div class="palette-item-label">开始事件</div>
                      </div>
                      <div 
                        class="palette-item" 
                        draggable="true" 
                        @dragstart="onPaletteDragStart('endEvent')"
                      >
                        <div class="palette-item-icon">
                          <div class="event-circle">
                            <span class="icon">■</span>
                          </div>
                        </div>
                        <div class="palette-item-label">结束事件</div>
                      </div>
                      <div 
                        class="palette-item" 
                        draggable="true" 
                        @dragstart="onPaletteDragStart('userTask')"
                      >
                        <div class="palette-item-icon">
                          <div class="task-rectangle">
                            <span class="icon">👤</span>
                          </div>
                        </div>
                        <div class="palette-item-label">用户任务</div>
                      </div>
                      <div 
                        class="palette-item" 
                        draggable="true" 
                        @dragstart="onPaletteDragStart('exclusiveGateway')"
                      >
                        <div class="palette-item-icon">
                          <div class="gateway-diamond">
                            <span class="icon">❓</span>
                          </div>
                        </div>
                        <div class="palette-item-label">排他网关</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
    
    <!-- 添加节点模态框 -->
    <div v-if="addNodeModalVisible" class="modal-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="closeAddNodeModal">
      <div class="modal-content" style="background:#ffffff;border-radius:8px;width:90%;max-width:480px;max-height:90vh;display:flex;flex-direction:column;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.2);" @click.stop>
        <div class="modal-header" style="display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid #e5e7eb;">
          <h3 style="margin:0;font-size:18px;font-weight:600;color:#111827;">选择节点类型</h3>
          <button class="modal-close" @click="closeAddNodeModal">×</button>
        </div>
        <div class="modal-body" style="padding:20px;overflow-y:auto;flex:1;min-height:100px;">
          <div class="node-type-selector">
            <div 
              v-for="type in nodeTypes" 
              :key="type.value"
              class="node-type-option"
              :class="{ selected: selectedNodeType === type.value }"
              @click="selectNodeType(type.value)"
            >
              <div class="node-type-icon">
                <div :class="type.iconClass">
                  <span class="icon">{{ type.icon }}</span>
                </div>
              </div>
              <div class="node-type-label">{{ type.label }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="display:flex;align-items:center;justify-content:flex-end;padding:12px 20px;border-top:1px solid #e5e7eb;background:#f9fafb;gap:8px;">
          <button class="btn btn-secondary" @click="closeAddNodeModal">取消</button>
          <button class="btn btn-primary" @click="confirmAddNode">确定</button>
        </div>
      </div>
    </div>
    
    <!-- 选择模板模态框 -->
    <div v-if="loadTemplateModalVisible" class="modal-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="closeLoadTemplateModal">
      <div class="modal-content" style="background:#ffffff;border-radius:8px;width:90%;max-width:480px;max-height:90vh;display:flex;flex-direction:column;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.2);" @click.stop>
        <div class="modal-header" style="display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid #e5e7eb;">
          <h3 style="margin:0;font-size:18px;font-weight:600;color:#111827;">选择模板</h3>
          <button class="modal-close" @click="closeLoadTemplateModal">×</button>
        </div>
        <div class="modal-body" style="padding:20px;overflow-y:auto;flex:1;min-height:100px;">
          <div class="template-selector">
            <div 
              v-for="template in templates" 
              :key="template.id"
              class="template-option"
              :class="{ selected: selectedTemplateId === template.id }"
              @click="selectTemplate(template.id)"
            >
              <div class="template-info">
                <h4 class="template-name" style="font-size:16px;font-weight:600;color:#111827;margin:0 0 8px 0;">{{ template.name }}</h4>
                <p class="template-description" style="font-size:14px;color:#6b7280;margin:0;">{{ template.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加节点模态框 -->
    <div v-if="addNodeModalVisible" class="modal-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="closeAddNodeModal">
      <div class="modal-content" style="background:#ffffff;border-radius:8px;width:90%;max-width:480px;max-height:90vh;display:flex;flex-direction:column;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.2);" @click.stop>
        <div class="modal-header" style="display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid #e5e7eb;">
          <h3 style="margin:0;font-size:18px;font-weight:600;color:#111827;">选择节点类型</h3>
          <button class="modal-close" @click="closeAddNodeModal">×</button>
        </div>
        <div class="modal-body" style="padding:20px;overflow-y:auto;flex:1;min-height:100px;">
          <div class="node-type-selector">
            <div v-for="type in nodeTypes" :key="type.value" class="node-type-option" :class="{ selected: selectedNodeType === type.value }" @click="selectNodeType(type.value)">
              <div class="node-type-icon">
                <div :class="type.iconClass"><span class="icon">{{ type.icon }}</span></div>
              </div>
              <div class="node-type-label">{{ type.label }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="display:flex;align-items:center;justify-content:flex-end;padding:12px 20px;border-top:1px solid #e5e7eb;background:#f9fafb;gap:8px;">
          <button class="btn btn-secondary" @click="closeAddNodeModal">取消</button>
          <button class="btn btn-primary" @click="confirmAddNode">确定</button>
        </div>
      </div>
    </div>
    
    <!-- 选择模板模态框 -->
    <div v-if="loadTemplateModalVisible" class="modal-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;z-index:1000;" @click="closeLoadTemplateModal">
      <div class="modal-content" style="background:#ffffff;border-radius:8px;width:90%;max-width:480px;max-height:90vh;display:flex;flex-direction:column;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.2);" @click.stop>
        <div class="modal-header" style="display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid #e5e7eb;">
          <h3 style="margin:0;font-size:18px;font-weight:600;color:#111827;">选择模板</h3>
          <button class="modal-close" @click="closeLoadTemplateModal">×</button>
        </div>
        <div class="modal-body" style="padding:20px;overflow-y:auto;flex:1;min-height:100px;">
          <div class="template-selector">
            <div v-for="template in templates" :key="template.id" class="template-option" :class="{ selected: selectedTemplateId === template.id }" @click="selectTemplate(template.id)">
              <div class="template-info">
                <h4 class="template-name" style="font-size:16px;font-weight:600;color:#111827;margin:0 0 8px 0;">{{ template.name }}</h4>
                <p class="template-description" style="font-size:14px;color:#6b7280;margin:0;">{{ template.description }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="display:flex;align-items:center;justify-content:flex-end;padding:12px 20px;border-top:1px solid #e5e7eb;background:#f9fafb;gap:8px;">
          <button class="btn btn-secondary" @click="closeLoadTemplateModal">取消</button>
          <button class="btn btn-primary" @click="confirmLoadTemplate" :disabled="!selectedTemplateId">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import html2canvas from 'html2canvas'

const flowNodes = ref([])

// 缩放和平移相关状态
const scale = ref(1)
const minScale = 0.5
const maxScale = 2
const panX = ref(0)
const panY = ref(0)
const isPanning = ref(false)
const panStartPos = ref({ x: 0, y: 0 })
const connections = ref([])
const selectedNodeIndex = ref(-1)
const connectionMode = ref(false)
const connectionSourceIndex = ref(-1)
const draggedNodeType = ref(null)
const currentDragIndex = ref(-1)
const draggingConnectionIndex = ref(-1)
const dragStartPos = ref({ x: 0, y: 0 })
const addNodeModalVisible = ref(false)
const selectedNodeType = ref('userTask')
const loadTemplateModalVisible = ref(false)
const selectedTemplateId = ref(null)

// 模板数据
const templates = [
  {
    id: 1,
    name: '请假审批流程',
    description: '标准的请假审批流程，包含天数检查'
  },
  {
    id: 2,
    name: '报销审批流程',
    description: '报销申请审批流程'
  },
  {
    id: 3,
    name: '加班审批流程',
    description: '加班申请审批流程'
  }
]

// 节点类型数据
const nodeTypes = [
  {
    value: 'userTask',
    label: '用户任务',
    icon: '👤',
    iconClass: 'task-rectangle'
  },
  {
    value: 'exclusiveGateway',
    label: '排他网关',
    icon: '❓',
    iconClass: 'gateway-diamond'
  },
  {
    value: 'endEvent',
    label: '结束事件',
    icon: '■',
    iconClass: 'event-circle'
  }
]

const selectNode = (index, event) => {
  event.stopPropagation()
  selectedNodeIndex.value = index
  // 点击节点时显示编辑弹窗
  editNode(index)
}

const startDrag = (index, event) => {
  event.stopPropagation()
  currentDragIndex.value = index
  const canvas = document.querySelector('.bpmn-canvas')
  const rect = canvas.getBoundingClientRect()
  dragStartPos.value = {
    x: (event.clientX - rect.left - flowNodes.value[index].x) / scale.value,
    y: (event.clientY - rect.top - flowNodes.value[index].y) / scale.value
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  event.preventDefault()
}

// 为节点添加连接点
const getConnectionPoints = (node) => {
  const nodeWidth = 220 // 增加节点宽度，确保文字完整显示
  const nodeHeight = 90 // 增加节点高度，确保文字完整显示
  return {
    top: { x: node.x + nodeWidth / 2, y: node.y },
    bottom: { x: node.x + nodeWidth / 2, y: node.y + nodeHeight },
    left: { x: node.x, y: node.y + nodeHeight / 2 },
    right: { x: node.x + nodeWidth, y: node.y + nodeHeight / 2 }
  }
}

const startConnectionDrag = (index, event) => {
  event.stopPropagation()
  draggingConnectionIndex.value = index
  const canvas = document.querySelector('.bpmn-canvas')
  const rect = canvas.getBoundingClientRect()
  dragStartPos.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  event.preventDefault()
}

const startConnectionPointDrag = (nodeIndex, pointName, event) => {
  event.stopPropagation()
  // 这里可以实现连接点的拖拽逻辑
  // 由于连接点是节点的一部分，拖拽连接点实际上是在调整节点的位置
  // 我们可以通过连接点的位置来计算节点的新位置
  const canvas = document.querySelector('.bpmn-canvas')
  const rect = canvas.getBoundingClientRect()
  const node = flowNodes.value[nodeIndex]
  
  // 计算连接点的绝对位置
  const points = getConnectionPoints(node)
  const point = points[pointName]
  
  dragStartPos.value = {
    x: event.clientX - rect.left - point.x,
    y: event.clientY - rect.top - point.y
  }
  
  // 使用当前节点拖拽来处理
  currentDragIndex.value = nodeIndex
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  event.preventDefault()
}

const onMouseMove = (event) => {
  if (currentDragIndex.value >= 0) {
    const canvas = document.querySelector('.bpmn-canvas')
    const rect = canvas.getBoundingClientRect()
    
    let x = (event.clientX - rect.left) / scale.value - dragStartPos.value.x
    let y = (event.clientY - rect.top) / scale.value - dragStartPos.value.y
    
    x = Math.max(0, x)
    y = Math.max(0, y)
    
    flowNodes.value[currentDragIndex.value].x = x
    flowNodes.value[currentDragIndex.value].y = y
    updateConnections()
  } else if (draggingConnectionIndex.value >= 0) {
    // 连接线拖拽逻辑
    const canvas = document.querySelector('.bpmn-canvas')
    const rect = canvas.getBoundingClientRect()
    
    const mouseX = (event.clientX - rect.left) / scale.value
    const mouseY = (event.clientY - rect.top) / scale.value
    
    // 找到最近的节点和连接点
    let closestNode = null
    let closestDistance = Infinity
    let closestPoint = null
    
    flowNodes.value.forEach((node, nodeIndex) => {
      const points = getConnectionPoints(node)
      
      // 检查四个连接点
      Object.entries(points).forEach(([pointName, point]) => {
        const distance = Math.sqrt(Math.pow(mouseX - point.x, 2) + Math.pow(mouseY - point.y, 2))
        if (distance < closestDistance && distance < 50) {
          closestDistance = distance
          closestNode = node
          closestPoint = pointName
        }
      })
    })
    
    if (closestNode) {
      // 找到连接线对应的源节点和目标节点
      const connection = connections.value[draggingConnectionIndex.value]
      if (connection) {
        // 这里需要根据实际的连接关系找到源节点和目标节点
        // 简化处理，假设连接的是第一个和第二个节点
        if (flowNodes.value.length >= 2) {
          // 移动目标节点到最近的连接点
          const targetNodeIndex = flowNodes.value.findIndex(node => node.id === closestNode.id)
          if (targetNodeIndex >= 0) {
            const points = getConnectionPoints(closestNode)
            const targetPoint = points[closestPoint]
            
            // 计算节点位置，使连接点对齐鼠标位置
            const nodeWidth = 180
            const nodeHeight = 80
            
            let newX = targetPoint.x
            let newY = targetPoint.y
            
            // 根据连接点类型调整节点位置
            switch (closestPoint) {
              case 'top':
                newX -= nodeWidth / 2
                break
              case 'bottom':
                newX -= nodeWidth / 2
                newY -= nodeHeight
                break
              case 'left':
                newY -= nodeHeight / 2
                break
              case 'right':
                newX -= nodeWidth
                newY -= nodeHeight / 2
                break
            }
            
            flowNodes.value[targetNodeIndex].x = newX
            flowNodes.value[targetNodeIndex].y = newY
            updateConnections()
          }
        }
      }
    }
  }
}

const onMouseUp = () => {
  currentDragIndex.value = -1
  draggingConnectionIndex.value = -1
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

// 缩放功能
const zoomIn = () => {
  if (scale.value < maxScale) {
    scale.value += 0.1
  }
}

const zoomOut = () => {
  if (scale.value > minScale) {
    scale.value -= 0.1
  }
}

const resetZoom = () => {
  scale.value = 1
  // 重置平移
  panX.value = 0
  panY.value = 0
}

// 平移功能
const startPan = (event) => {
  // 允许用鼠标左键拖动画布
  if (event.button === 0) {
    // 检查是否点击在空白区域，避免与节点拖拽冲突
    const target = event.target
    if (target.classList.contains('designer-canvas') || target.classList.contains('bpmn-canvas') || target.classList.contains('bpmn-svg')) {
      event.preventDefault()
      isPanning.value = true
      panStartPos.value = {
        x: event.clientX - panX.value,
        y: event.clientY - panY.value
      }
      document.addEventListener('mousemove', onPanMove)
      document.addEventListener('mouseup', onPanEnd)
    }
  }
}

const onPanMove = (event) => {
  if (isPanning.value) {
    event.preventDefault()
    panX.value = event.clientX - panStartPos.value.x
    panY.value = event.clientY - panStartPos.value.y
  }
}

const onPanEnd = () => {
  isPanning.value = false
  document.removeEventListener('mousemove', onPanMove)
  document.removeEventListener('mouseup', onPanEnd)
}

// 保存为图片功能
const saveAsImage = () => {
  const canvasElement = document.querySelector('.bpmn-canvas')
  if (canvasElement) {
    // 保存当前的变换状态
    const originalTransform = canvasElement.style.transform
    
    // 重置变换，以便保存完整的画布
    canvasElement.style.transform = 'scale(1)'
    
    // 使用更简单的方法保存图片
    html2canvas(canvasElement, {
      backgroundColor: '#ffffff',
      scale: 2, // 提高图片质量
      useCORS: true
    }).then(canvas => {
      // 转换为Blob对象，然后创建URL
      canvas.toBlob(blob => {
        if (blob) {
          const url = URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.download = `flow-chart-${Date.now()}.png`
          link.href = url
          link.style.display = 'none'
          document.body.appendChild(link)
          
          // 使用setTimeout确保链接被正确添加到DOM
          setTimeout(() => {
            link.click()
            document.body.removeChild(link)
            URL.revokeObjectURL(url)
          }, 100)
        }
      })
      
      // 恢复原始变换
      setTimeout(() => {
        canvasElement.style.transform = originalTransform
      }, 200)
    }).catch(error => {
      console.error('Error saving image:', error)
      // 恢复原始变换
      canvasElement.style.transform = originalTransform
    })
  }
}

const onPaletteDragStart = (nodeType) => {
  draggedNodeType.value = nodeType
}

const onDrop = (event) => {
  event.preventDefault()
  if (draggedNodeType.value) {
    const canvas = document.querySelector('.bpmn-canvas')
    const rect = canvas.getBoundingClientRect()
    
    const x = event.clientX - rect.left - 90
    const y = event.clientY - rect.top - 50
    
    const newNode = {
      id: `node_${Date.now()}`,
      name: getDefaultNodeName(draggedNodeType.value),
      type: draggedNodeType.value,
      x: Math.max(0, x),
      y: Math.max(0, y),
      nextNodes: {}
    }
    
    if (newNode.type === 'userTask') {
      newNode.assignee = ''
      newNode.assigneeType = 'user'
    }
    
    flowNodes.value.push(newNode)
    updateConnections()
  }
  draggedNodeType.value = null
}

const onDragOver = (event) => {
  event.preventDefault()
}

const onDragEnter = (event) => {
  event.preventDefault()
}

const onDragLeave = (event) => {
  event.preventDefault()
}

const getDefaultNodeName = (type) => {
  const names = {
    startEvent: '开始',
    endEvent: '结束',
    userTask: '审批任务',
    exclusiveGateway: '条件判断'
  }
  return names[type] || '节点'
}

const getNodeTypeName = (type) => {
  const names = {
    startEvent: '开始事件',
    endEvent: '结束事件',
    userTask: '用户任务',
    exclusiveGateway: '排他网关'
  }
  return names[type] || type
}

const addConnection = (nodeIndex, port) => {
  if (connectionMode.value) {
    if (connectionSourceIndex.value !== nodeIndex) {
      const sourceNode = flowNodes.value[connectionSourceIndex.value]
      const targetNode = flowNodes.value[nodeIndex]
      
      const defaultKey = getDefaultConnectionKey(sourceNode.type, targetNode.type)
      sourceNode.nextNodes[defaultKey] = targetNode.id
      updateConnections()
    }
    connectionMode.value = false
    connectionSourceIndex.value = -1
  } else {
    connectionMode.value = true
    connectionSourceIndex.value = nodeIndex
  }
}

const getDefaultConnectionKey = (sourceType, targetType) => {
  if (sourceType === 'exclusiveGateway') {
    return Object.keys(flowNodes.value[connectionSourceIndex.value].nextNodes).length === 0 ? 'APPROVE' : 'REJECT'
  }
  return 'next'
}

const updateConnections = () => {
  const newConnections = []
  
  flowNodes.value.forEach((node) => {
    if (node.nextNodes) {
      Object.entries(node.nextNodes).forEach(([key, nextNodeId]) => {
        const nextNode = flowNodes.value.find(n => n.id === nextNodeId)
        if (nextNode) {
          const connection = createConnection(node, nextNode, key)
          newConnections.push(connection)
        }
      })
    }
  })
  
  connections.value = newConnections
}

const createConnection = (sourceNode, targetNode, key) => {
  const nodeWidth = 220 // 增加节点宽度，确保文字完整显示
  const nodeHeight = 90 // 增加节点高度，确保文字完整显示
  const cornerDistance = 80 // 增加拐角距离，使线条更美观

  // 获取连接点
  const sourcePoints = getConnectionPoints(sourceNode)
  const targetPoints = getConnectionPoints(targetNode)

  let sourceX, sourceY, targetX, targetY, path, arrow, label, color

  // 计算节点中心
  const sourceCenterX = sourceNode.x + nodeWidth / 2
  const sourceCenterY = sourceNode.y + nodeHeight / 2
  const targetCenterX = targetNode.x + nodeWidth / 2
  const targetCenterY = targetNode.y + nodeHeight / 2

  // 计算节点间的相对位置
  const dx = targetCenterX - sourceCenterX
  const dy = targetCenterY - sourceCenterY

  // 确定源节点的连接点
  if (sourceNode.type === 'userTask') {
    if (key === 'APPROVE') {
      // 同意从右侧出来
      sourceX = sourcePoints.right.x
      sourceY = sourcePoints.right.y
    } else if (key === 'REJECT') {
      // 拒绝从左侧出来
      sourceX = sourcePoints.left.x
      sourceY = sourcePoints.left.y
    } else {
      // 其他情况使用默认逻辑
      if (Math.abs(dx) > Math.abs(dy)) {
        sourceX = dx > 0 ? sourcePoints.right.x : sourcePoints.left.x
        sourceY = sourcePoints.right.y
      } else {
        sourceX = sourcePoints.bottom.x
        sourceY = dy > 0 ? sourcePoints.bottom.y : sourcePoints.top.y
      }
    }
  } else if (sourceNode.type === 'exclusiveGateway') {
    // 网关根据连接类型选择不同的连接点
    if (key === 'short' || key === 'SHORT' || key === 'LESS' || key === '<=3天') {
      sourceX = sourcePoints.left.x
      sourceY = sourcePoints.left.y
    } else if (key === 'long' || key === 'LONG' || key === 'MORE' || key === '>3天') {
      sourceX = sourcePoints.right.x
      sourceY = sourcePoints.right.y
    } else if (key === 'APPROVE') {
      sourceX = sourcePoints.bottom.x
      sourceY = sourcePoints.bottom.y
    } else if (key === 'REJECT') {
      sourceX = sourcePoints.left.x
      sourceY = sourcePoints.left.y
    } else {
      // 默认使用底部连接点
      sourceX = sourcePoints.bottom.x
      sourceY = sourcePoints.bottom.y
    }
  } else {
    // 其他节点类型使用默认逻辑
    if (Math.abs(dx) > Math.abs(dy)) {
      sourceX = dx > 0 ? sourcePoints.right.x : sourcePoints.left.x
      sourceY = sourcePoints.right.y
    } else {
      sourceX = sourcePoints.bottom.x
      sourceY = dy > 0 ? sourcePoints.bottom.y : sourcePoints.top.y
    }
  }

  // 确定目标节点的连接点
  if (Math.abs(dx) > Math.abs(dy)) {
    targetX = dx > 0 ? targetPoints.left.x : targetPoints.right.x
    targetY = targetPoints.left.y
  } else {
    targetX = targetPoints.top.x
    targetY = dy > 0 ? targetPoints.top.y : targetPoints.bottom.y
  }

  // 特殊处理特定节点的连接点
  if (targetNode.name === 'HR审批' && (key === 'short' || key === 'SHORT' || key === 'LESS' || key === '<=3天')) {
    targetX = targetPoints.left.x
    targetY = targetPoints.left.y
  }

  if (targetNode.name === '上级主管审批' && (key === 'long' || key === 'LONG' || key === 'MORE' || key === '>3天')) {
    targetX = targetPoints.top.x
    targetY = targetPoints.top.y
  }

  if (targetNode.name === '结束' && sourceNode.name === 'HR审批') {
    targetX = targetPoints.top.x
    targetY = targetPoints.top.y
  }

  if (targetNode.name === '结束' && sourceNode.name === '上级主管审批') {
    targetX = targetPoints.right.x
    targetY = targetPoints.right.y
  }

  if (targetNode.name === '结束' && sourceNode.name === '直接主管审批') {
    targetX = targetPoints.left.x
    targetY = targetPoints.left.y
  }

  // 计算连接方向和路径 - 采用更美观的矩形折线
  const connectionDx = targetX - sourceX
  const connectionDy = targetY - sourceY

  // 为了避免线条交叉，为不同类型的连线设置不同的偏移
  let offsetX = 0
  let offsetY = 0

  // 根据具体的节点和连接类型设置不同的偏移
  if (sourceNode.type === 'userTask') {
    if (key === 'APPROVE') {
      offsetX = 15
    } else if (key === 'REJECT') {
      offsetX = -15
    }
  } else if (sourceNode.type === 'exclusiveGateway') {
    if (key === 'short' || key === 'SHORT' || key === 'LESS' || key === '<=3天') {
      offsetX = -15
    } else if (key === 'long' || key === 'LONG' || key === 'MORE' || key === '>3天') {
      offsetX = 15
    }
  }

  // 生成美观的矩形折线路径
  if (connectionDx > 0 && connectionDy > 0) {
    // 右下连接
    const controlX = sourceX + cornerDistance + offsetX
    const controlY = targetY + offsetY
    path = `M${sourceX},${sourceY} L${controlX},${sourceY} L${controlX},${controlY} L${targetX},${controlY}`
  } else if (connectionDx > 0 && connectionDy < 0) {
    // 右上连接
    const controlX = sourceX + cornerDistance + offsetX
    const controlY = targetY + offsetY
    path = `M${sourceX},${sourceY} L${controlX},${sourceY} L${controlX},${controlY} L${targetX},${controlY}`
  } else if (connectionDx < 0 && connectionDy > 0) {
    // 左下连接
    const controlX = sourceX - cornerDistance + offsetX
    const controlY = targetY + offsetY
    path = `M${sourceX},${sourceY} L${controlX},${sourceY} L${controlX},${controlY} L${targetX},${controlY}`
  } else if (connectionDx < 0 && connectionDy < 0) {
    // 左上连接
    const controlX = sourceX - cornerDistance + offsetX
    const controlY = targetY + offsetY
    path = `M${sourceX},${sourceY} L${controlX},${sourceY} L${controlX},${controlY} L${targetX},${controlY}`
  } else if (connectionDx > 0) {
    // 水平向右连接
    path = `M${sourceX},${sourceY + offsetY} L${targetX},${targetY + offsetY}`
  } else if (connectionDx < 0) {
    // 水平向左连接
    path = `M${sourceX},${sourceY + offsetY} L${targetX},${targetY + offsetY}`
  } else if (connectionDy > 0) {
    // 垂直向下连接
    path = `M${sourceX + offsetX},${sourceY} L${targetX + offsetX},${targetY}`
  } else if (connectionDy < 0) {
    // 垂直向上连接
    path = `M${sourceX + offsetX},${sourceY} L${targetX + offsetX},${targetY}`
  } else {
    // 相同位置
    path = `M${sourceX},${sourceY} L${targetX},${targetY}`
  }

  // 处理网关的特殊连线
  if (sourceNode.type === 'exclusiveGateway') {
    if (key === 'APPROVE' || key === 'SHORT' || key === 'LESS' || key === 'short') {
      label = key === 'APPROVE' ? '通过' : '≤3天'
      color = '#52c41a'
    } else if (key === 'REJECT' || key === 'LONG' || key === 'MORE' || key === 'long') {
      label = key === 'REJECT' ? '拒绝' : '>3天'
      color = '#ff4d4f'
    } else {
      label = key
      color = '#1890ff'
    }
  } else if (sourceNode.type === 'userTask') {
    if (key === 'APPROVE') {
      label = '同意'
      color = '#52c41a'
    } else if (key === 'REJECT') {
      label = '拒绝'
      color = '#ff4d4f'
    } else {
      label = key
      color = '#1890ff'
    }
  } else {
    label = ''
    color = '#1890ff'
  }

  // 计算箭头 - 只在最后一段添加箭头
  const lastDashIndex = path.lastIndexOf('L')
  const lastPoint = path.substring(lastDashIndex + 1).trim()
  const [lastX, lastY] = lastPoint.split(',').map(Number)
  
  const secondLastDashIndex = path.lastIndexOf('L', lastDashIndex - 1)
  const secondLastPoint = path.substring(secondLastDashIndex + 1, lastDashIndex).trim()
  const [secondLastX, secondLastY] = secondLastPoint.split(',').map(Number)
  
  const angle = Math.atan2(lastY - secondLastY, lastX - secondLastX)
  const arrowLen = 12
  const arrowWidth = 6
  arrow = `M${lastX - arrowLen * Math.cos(angle) + arrowWidth * Math.sin(angle)},${lastY - arrowLen * Math.sin(angle) - arrowWidth * Math.cos(angle)} L${lastX},${lastY} L${lastX - arrowLen * Math.cos(angle) - arrowWidth * Math.sin(angle)},${lastY - arrowLen * Math.sin(angle) + arrowWidth * Math.cos(angle)}`

  // 计算标签位置，确保标签不与线条重叠
  let labelX, labelY
  if (Math.abs(connectionDx) > Math.abs(connectionDy)) {
    // 水平连接
    labelX = (sourceX + targetX) / 2
    labelY = (sourceY + targetY) / 2 - 15 // 增加距离，避免重叠
  } else {
    // 垂直连接
    labelX = (sourceX + targetX) / 2 + 20 // 增加距离，避免重叠
    labelY = (sourceY + targetY) / 2
  }

  return {
    path,
    arrow,
    label,
    color,
    labelX,
    labelY,
    sourcePoint: { x: sourceX, y: sourceY },
    targetPoint: { x: targetX, y: targetY }
  }
}

const clearFlow = () => {
  flowNodes.value = []
  connections.value = []
  selectedNodeIndex.value = -1
}

const saveTemplate = () => {
  if (flowNodes.value.length === 0) {
    return
  }
  
  const templateData = {
    nodes: flowNodes.value.map(node => ({
      nodeId: node.id,
      nodeName: node.name,
      nodeType: node.type,
      assigneeValue: node.assignee,
      assigneeType: node.assigneeType,
      nextNodes: node.nextNodes
    }))
  }
  
  console.log('保存模板:', templateData)
}

const showLoadTemplateModal = () => {
  selectedTemplateId.value = null
  loadTemplateModalVisible.value = true
}

const closeLoadTemplateModal = () => {
  loadTemplateModalVisible.value = false
}

const selectTemplate = (id) => {
  selectedTemplateId.value = id
  console.log('选择了模板ID:', id)
}

const confirmLoadTemplate = () => {
  if (!selectedTemplateId.value) return
  
  console.log('选择的模板ID:', selectedTemplateId.value)
  const selectedTemplate = templates.find(t => t.id === selectedTemplateId.value)
  if (selectedTemplate) {
    console.log('选择的模板:', selectedTemplate.name)
    loadTemplateByType(selectedTemplateId.value)
  }
  loadTemplateModalVisible.value = false
}

const loadTemplateByType = (templateId) => {
  if (templateId === 1) {
    loadLeaveTemplate()
  } else if (templateId === 2) {
    loadReimbursementTemplate()
  } else if (templateId === 3) {
    loadOvertimeTemplate()
  }
}

const loadLeaveTemplate = () => {
  const sampleTemplate = {
    nodes: [
      {
        id: 'Start',
        name: '开始',
        type: 'startEvent',
        x: 300,
        y: 50,
        nextNodes: { next: 'DirectManager' }
      },
      {
        id: 'DirectManager',
        name: '直接主管审批',
        type: 'userTask',
        assignee: '${direct_manager}',
        x: 300,
        y: 180,
        nextNodes: { APPROVE: 'DaysCheck', REJECT: 'End' }
      },
      {
        id: 'DaysCheck',
        name: '请假天数检查',
        type: 'exclusiveGateway',
        x: 300,
        y: 310,
        nextNodes: { short: 'HR', long: 'SeniorManager' }
      },
      {
        id: 'SeniorManager',
        name: '上级主管审批',
        type: 'userTask',
        assignee: '${senior_manager}',
        x: 480,
        y: 440,
        nextNodes: { APPROVE: 'HR', REJECT: 'End' }
      },
      {
        id: 'HR',
        name: 'HR审批',
        type: 'userTask',
        assignee: '${hr}',
        x: 300,
        y: 440,
        nextNodes: { next: 'End' }
      },
      {
        id: 'End',
        name: '结束',
        type: 'endEvent',
        x: 300,
        y: 570,
        nextNodes: {}
      }
    ]
  }
  
  flowNodes.value = sampleTemplate.nodes
  updateConnections()
}

const loadReimbursementTemplate = () => {
  const sampleTemplate = {
    nodes: [
      {
        id: 'Start',
        name: '开始',
        type: 'startEvent',
        x: 300,
        y: 50,
        nextNodes: { next: 'DirectManager' }
      },
      {
        id: 'DirectManager',
        name: '直接主管审批',
        type: 'userTask',
        assignee: '${direct_manager}',
        x: 300,
        y: 180,
        nextNodes: { APPROVE: 'Finance', REJECT: 'End' }
      },
      {
        id: 'Finance',
        name: '财务审批',
        type: 'userTask',
        assignee: '${finance}',
        x: 300,
        y: 310,
        nextNodes: { APPROVE: 'End', REJECT: 'DirectManager' }
      },
      {
        id: 'End',
        name: '结束',
        type: 'endEvent',
        x: 300,
        y: 440,
        nextNodes: {}
      }
    ]
  }
  
  flowNodes.value = sampleTemplate.nodes
  updateConnections()
}

const loadOvertimeTemplate = () => {
  const sampleTemplate = {
    nodes: [
      {
        id: 'Start',
        name: '开始',
        type: 'startEvent',
        x: 300,
        y: 50,
        nextNodes: { next: 'DirectManager' }
      },
      {
        id: 'DirectManager',
        name: '直接主管审批',
        type: 'userTask',
        assignee: '${direct_manager}',
        x: 300,
        y: 180,
        nextNodes: { APPROVE: 'HR', REJECT: 'End' }
      },
      {
        id: 'HR',
        name: 'HR审批',
        type: 'userTask',
        assignee: '${hr}',
        x: 300,
        y: 310,
        nextNodes: { APPROVE: 'End', REJECT: 'DirectManager' }
      },
      {
        id: 'End',
        name: '结束',
        type: 'endEvent',
        x: 300,
        y: 440,
        nextNodes: {}
      }
    ]
  }
  
  flowNodes.value = sampleTemplate.nodes
  updateConnections()
}

const editNode = (index) => {
  // 选择节点，右侧面板会自动显示节点信息
  selectedNodeIndex.value = index
}

const deleteNode = (index) => {
  flowNodes.value.splice(index, 1)
  updateConnections()
  if (selectedNodeIndex.value === index) {
    selectedNodeIndex.value = -1
  }
}

const showAddNodeModal = () => {
  if (selectedNodeIndex.value === -1) return
  addNodeModalVisible.value = true
}

const closeAddNodeModal = () => {
  addNodeModalVisible.value = false
}

const selectNodeType = (type) => {
  selectedNodeType.value = type
  console.log('选择了节点类型:', type)
}

const confirmAddNode = () => {
  if (selectedNodeIndex.value === -1) return
  
  const selectedNode = flowNodes.value[selectedNodeIndex.value]
  
  const newNode = {
    id: `node_${Date.now()}`,
    name: getDefaultNodeName(selectedNodeType.value),
    type: selectedNodeType.value,
    x: selectedNode.x + 200,
    y: selectedNode.y,
    nextNodes: {}
  }
  
  if (newNode.type === 'userTask') {
    newNode.assignee = ''
    newNode.assigneeType = 'user'
  }
  
  flowNodes.value.push(newNode)
  
  if (selectedNode.nextNodes) {
    selectedNode.nextNodes.next = newNode.id
  } else {
    selectedNode.nextNodes = { next: newNode.id }
  }
  
  updateConnections()
  selectedNodeIndex.value = flowNodes.value.length - 1
  addNodeModalVisible.value = false
}

onMounted(() => {
  // 不默认加载任何模板
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
})
</script>

<style scoped>
.designer {
  width: 100%;
  height: 100%;
}

/* 页面标题区 */
.page-title {
  padding: var(--space-4) var(--space-6);
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.page-title-main {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.page-title-description {
  font-size: 14px;
  color: var(--text-secondary);
}

.page-title-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  margin-bottom: var(--space-2);
}

.breadcrumb-item {
  color: var(--text-secondary);
  text-decoration: none;
}

.breadcrumb-item:hover {
  color: var(--primary);
}

.breadcrumb-item.active {
  color: var(--text-primary);
  font-weight: 500;
}

.breadcrumb-separator {
  color: var(--text-tertiary);
}

/* 卡片 */
.card {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  overflow: hidden;
  transition: box-shadow var(--transition-normal);
}

.card:hover {
  box-shadow: var(--shadow-md);
}

.card-body {
  padding: var(--space-5);
}

/* 按钮 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid transparent;
  text-decoration: none;
  white-space: nowrap;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.btn-primary:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
  color: #111827;
}

/* 设计器控制栏 */
.designer-controls {
  margin-bottom: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

/* 设计器画布 */
.designer-canvas {
  width: 100%;
  height: 600px;
  border: 2px dashed var(--border);
  border-radius: var(--radius-lg);
  position: relative;
  background: linear-gradient(45deg, #f8fafc 25%, transparent 25%), 
              linear-gradient(-45deg, #f8fafc 25%, transparent 25%), 
              linear-gradient(45deg, transparent 75%, #f8fafc 75%), 
              linear-gradient(-45deg, transparent 75%, #f8fafc 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
  overflow: auto;
  margin-bottom: var(--space-6);
}

.bpmn-canvas {
  width: 2000px;
  height: 1000px;
  position: relative;
}

.bpmn-svg {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.nodes {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
}

/* 节点样式 */
.bpmn-node {
  position: absolute;
  width: 220px;
  height: 90px;
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  border: 2px solid var(--border);
  overflow: visible;
  z-index: 3;
  display: flex;
  align-items: center;
  padding: 0 16px;
}

.bpmn-node:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  border-color: var(--primary-light);
}

.bpmn-node.selected {
  border: 2px solid var(--primary);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}

.bpmn-node.startEvent {
  border-left: 4px solid var(--success);
  background: linear-gradient(135deg, var(--success-light), var(--bg-primary));
}

.bpmn-node.endEvent {
  border-left: 4px solid var(--error);
  background: linear-gradient(135deg, var(--error-light), var(--bg-primary));
}

.bpmn-node.userTask {
  border-left: 4px solid var(--primary);
  background: linear-gradient(135deg, var(--primary-light), var(--bg-primary));
}

.bpmn-node.exclusiveGateway {
  border-left: 4px solid var(--warning);
  background: linear-gradient(135deg, var(--warning-light), var(--bg-primary));
}

.node-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  flex: 1;
  overflow: visible;
}

.node-header {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: visible;
}

.node-icon {
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.node-icon .event-circle {
  width: 28px;
  height: 28px;
  border: 2px solid var(--primary);
  border-radius: 50%;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.node-icon .task-rectangle {
  width: 32px;
  height: 24px;
  border: 2px solid var(--success);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.node-icon .gateway-diamond {
  width: 24px;
  height: 24px;
  border: 2px solid var(--warning);
  transform: rotate(45deg);
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.node-icon .icon {
  font-size: 14px;
  font-weight: bold;
  position: relative;
  z-index: 1;
}

.node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  line-height: 1.3;
}

.node-body {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.node-type {
  font-weight: 500;
}

.node-assignee {
  opacity: 0.8;
}

/* 连接点 */
.connection-points {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 2;
}

.connection-point {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #1890ff;
  border: 2px solid white;
  border-radius: 50%;
  cursor: crosshair;
  pointer-events: all;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.connection-point:hover {
  transform: scale(1.2);
  background: #40a9ff;
}

.connection-point.top {
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
}

.connection-point.bottom {
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
}

.connection-point.left {
  left: -4px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.right {
  right: -4px;
  top: 50%;
  transform: translateY(-50%);
}

/* 节点操作按钮 */
.node-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  gap: 4px;
  z-index: 4;
}

.bpmn-node:hover .node-actions {
  opacity: 1;
}

.action-btn {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  background: #1890ff;
  color: white;
  transform: scale(1.1);
}

/* 节点端口 */
.node-ports {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 2;
}

.port {
  position: absolute;
  width: 12px;
  height: 12px;
  background: rgba(59, 130, 246, 0.7);
  border-radius: 50%;
  cursor: crosshair;
  pointer-events: auto;
  transition: all 0.2s;
  border: 2px solid var(--primary);
  z-index: 5;
}

.port:hover {
  background: var(--primary);
  transform: scale(1.3);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2);
}

.port-top {
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.port-right {
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.port-bottom {
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.port-left {
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
}

/* 设计器内容区 */
.designer-content {
  display: flex;
  gap: var(--space-6);
  height: calc(100vh - 200px);
  overflow: hidden;
  width: 100%;
  min-width: 900px;
}

.card-body {
  padding: var(--space-6);
  overflow: hidden;
}

.page-content {
  flex: 1;
  padding: var(--space-6);
  overflow: hidden;
  background-color: var(--bg-secondary);
}

.designer-canvas {
  flex: 1;
  border: 2px dashed var(--border);
  border-radius: var(--radius-lg);
  position: relative;
  background: linear-gradient(45deg, #f8fafc 25%, transparent 25%), 
              linear-gradient(-45deg, #f8fafc 25%, transparent 25%), 
              linear-gradient(45deg, transparent 75%, #f8fafc 75%), 
              linear-gradient(-45deg, transparent 75%, #f8fafc 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
  overflow: auto;
}

/* 右侧边栏 */
.designer-sidebar {
  width: 300px;
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  flex-shrink: 0;
}

.sidebar-section {
  border-bottom: 1px solid var(--border);
  padding-bottom: var(--space-4);
}

.sidebar-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.sidebar-section h4 {
  margin-bottom: var(--space-3);
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.no-selection {
  text-align: center;
  padding: var(--space-6) var(--space-4);
  color: var(--text-secondary);
  font-size: 14px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.node-details {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.form-control {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-control:disabled {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  cursor: not-allowed;
}

.position-inputs {
  display: flex;
  gap: var(--space-2);
}

.position-inputs .form-control {
  flex: 1;
}

/* 工具栏 */
.designer-palette {
  margin-top: var(--space-6);
  padding: var(--space-4);
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.designer-palette h4 {
  margin-bottom: var(--space-4);
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}

.palette-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.palette-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: move;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  transition: all 0.3s;
  border: 2px dashed var(--border);
  background: var(--bg-primary);
  text-align: left;
}

.palette-item:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.palette-item-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.palette-item-icon .event-circle {
  width: 28px;
  height: 28px;
  border: 2px solid var(--primary);
  border-radius: 50%;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.palette-item-icon .task-rectangle {
  width: 32px;
  height: 24px;
  border: 2px solid var(--success);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.palette-item-icon .gateway-diamond {
  width: 24px;
  height: 24px;
  border: 2px solid var(--warning);
  transform: rotate(45deg);
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.palette-item-icon .icon {
  font-size: 14px;
  font-weight: bold;
}

.palette-item-label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 480px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #9ca3af;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.modal-close:hover {
  color: #111827;
  background: #f3f4f6;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 100px;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 20px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  gap: 8px;
}

/* 节点类型选择器 */
.node-type-selector {
  padding: 8px 0;
}

.node-type-option {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 8px;
  border: 2px solid transparent;
  background: #f9fafb;
  transition: all 0.2s;
}

.node-type-option:hover {
  background: #f3f4f6;
  border-color: #e5e7eb;
}

.node-type-option.selected {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
}

.node-type-icon {
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.node-type-icon .event-circle {
  width: 36px;
  height: 36px;
  border: 2px solid #3b82f6;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-type-icon .task-rectangle {
  width: 44px;
  height: 32px;
  border: 2px solid #10b981;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: #ffffff;
}

.node-type-icon .gateway-diamond {
  width: 36px;
  height: 36px;
  border: 2px solid #f59e0b;
  transform: rotate(45deg);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-type-icon .icon {
  font-size: 20px;
  font-weight: bold;
}

.node-type-label {
  font-size: 16px;
  color: #111827;
  font-weight: 500;
  flex: 1;
}

/* 模板选择器 */
.template-selector {
  padding: 8px 0;
}

.template-option {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 12px;
  border: 2px solid transparent;
  background: #f9fafb;
  transition: all 0.2s;
}

.template-option:hover {
  border-color: #e5e7eb;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.template-option.selected {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.template-info {
  flex: 1;
}

.template-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.template-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .designer-controls {
    flex-wrap: wrap;
  }
  
  .designer-canvas {
    height: 400px;
  }
  
  .bpmn-node {
    width: 150px;
    height: 80px;
  }
  
  .node-content {
    padding: 12px;
  }
  
  .node-icon {
    width: 40px;
    height: 40px;
  }
  
  .node-icon .event-circle {
    width: 28px;
    height: 28px;
  }
  
  .node-icon .task-rectangle {
    width: 36px;
    height: 28px;
  }
  
  .node-icon .gateway-diamond {
    width: 28px;
    height: 28px;
  }
  
  .node-title {
    font-size: 14px;
  }
}
</style>