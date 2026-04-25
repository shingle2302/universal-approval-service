// 流程布局和连接计算工具函数

/**
 * 检查是否为向下分支
 * @param {string} key - 分支键
 * @returns {boolean} 是否为向下分支
 */
export const isDownBranch = (key) => {
  return key === 'long' || key === 'LONG' || key === 'REJECT' || key === 'MORE'
}

/**
 * 布局流程节点
 * @param {Array} nodes - 流程节点数组
 * @returns {Array} 布局后的节点数组
 */
export const layoutFlowNodes = (nodes) => {
  const result = []
  const nodeMap = new Map()
  const horizontalGap = 250
  const verticalGap = 120

  nodes.forEach(node => {
    nodeMap.set(node.nodeId, node)
  })

  const nodePositions = new Map()

  const assignPositions = (nodeId, x, y) => {
    if (nodePositions.has(nodeId)) {
      return
    }

    const node = nodeMap.get(nodeId)
    if (!node) return

    nodePositions.set(nodeId, { x, y })

    result.push({
      id: node.nodeId,
      name: node.nodeName,
      type: node.nodeType,
      assignee: node.assigneeValue,
      assigneeType: node.assigneeType || 'user',
      approveExpression: node.conditions?.APPROVE?.expression,
      rejectExpression: node.conditions?.REJECT?.expression,
      nextNodes: node.nextNodes,
      x: x,
      y: y
    })

    if (node.nextNodes && Object.keys(node.nextNodes).length > 0) {
      const nextKeys = Object.keys(node.nextNodes)

      nextKeys.forEach(key => {
        const childY = isDownBranch(key) ? y + verticalGap : y
        assignPositions(node.nextNodes[key], x + horizontalGap, childY)
      })
    }
  }

  const startNode = nodes.find(n => n.nodeType === 'startEvent')
  if (startNode) {
    assignPositions(startNode.nodeId, 50, 150)
  }

  return result
}

/**
 * 创建连接对象
 * @param {Object} sourceNode - 源节点
 * @param {Object} targetNode - 目标节点
 * @param {string} key - 连接键
 * @returns {Object} 连接对象
 */
const createConnection = (sourceNode, targetNode, key) => {
  const nodeWidth = 150
  const nodeHeight = 80

  const sourceCenterX = sourceNode.x + nodeWidth / 2
  const sourceCenterY = sourceNode.y + nodeHeight / 2
  const targetCenterX = targetNode.x + nodeWidth / 2
  const targetCenterY = targetNode.y + nodeHeight / 2

  let sourceX, sourceY, targetX, targetY, path, arrow, label, color

  const dx = targetCenterX - sourceCenterX
  const dy = targetCenterY - sourceCenterY

  if (Math.abs(dx) > Math.abs(dy)) {
    if (dx > 0) {
      sourceX = sourceNode.x + nodeWidth
      sourceY = sourceCenterY
      targetX = targetNode.x
      targetY = targetCenterY
    } else {
      sourceX = sourceNode.x
      sourceY = sourceCenterY
      targetX = targetNode.x + nodeWidth
      targetY = targetCenterY
    }
  } else {
    if (dy > 0) {
      sourceX = sourceCenterX
      sourceY = sourceNode.y + nodeHeight
      targetX = targetCenterX
      targetY = targetNode.y
    } else {
      sourceX = sourceCenterX
      sourceY = sourceNode.y
      targetX = targetCenterX
      targetY = targetNode.y + nodeHeight
    }
  }

  if (sourceNode.type === 'exclusiveGateway') {
    if (key === 'APPROVE' || key === 'SHORT' || key === 'LESS' || key === 'short') {
      const ctrlX = sourceX + 60
      const ctrlY = sourceY - 60
      path = `M${sourceX},${sourceY} Q${ctrlX},${ctrlY} ${targetX},${targetY}`
      label = key === 'APPROVE' ? '通过' : '≤3天'
      color = '#52c41a'
    } else if (key === 'REJECT' || key === 'LONG' || key === 'MORE' || key === 'long') {
      const ctrlX = sourceX + 60
      const ctrlY = sourceY + 60
      path = `M${sourceX},${sourceY} Q${ctrlX},${ctrlY} ${targetX},${targetY}`
      label = key === 'REJECT' ? '拒绝' : '>3天'
      color = '#ff4d4f'
    } else {
      path = `M${sourceX},${sourceY} L${targetX},${targetY}`
      label = key
      color = '#1890ff'
    }
  } else {
    path = `M${sourceX},${sourceY} L${targetX},${targetY}`
    label = ''
    color = '#1890ff'
  }

  const angle = Math.atan2(targetY - sourceY, targetX - sourceX)
  const arrowLen = 12
  const arrowWidth = 6
  arrow = `M${targetX - arrowLen * Math.cos(angle) + arrowWidth * Math.sin(angle)},${targetY - arrowLen * Math.sin(angle) - arrowWidth * Math.cos(angle)} L${targetX},${targetY} L${targetX - arrowLen * Math.cos(angle) - arrowWidth * Math.sin(angle)},${targetY - arrowLen * Math.sin(angle) + arrowWidth * Math.cos(angle)}`

  return {
    path,
    arrow,
    label,
    labelX: (sourceX + targetX) / 2,
    labelY: (sourceY + targetY) / 2 - 10,
    color,
    sourceNode: sourceNode.id,
    targetNode: targetNode.id
  }
}

/**
 * 计算流程连接
 * @param {Array} nodes - 布局后的节点数组
 * @returns {Array} 连接数组
 */
export const calculateViewConnections = (nodes) => {
  const connections = []

  nodes.forEach((node) => {
    if (node.nextNodes) {
      Object.entries(node.nextNodes).forEach(([key, nextNodeId]) => {
        const nextNode = nodes.find(n => n.id === nextNodeId)
        if (nextNode) {
          const connection = createConnection(node, nextNode, key)
          connections.push(connection)
        }
      })
    }
  })

  return connections
}