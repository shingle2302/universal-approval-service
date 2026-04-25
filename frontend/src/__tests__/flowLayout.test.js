import { describe, it, expect } from 'vitest'
import { layoutFlowNodes, calculateViewConnections } from '../utils/flowLayout.js'

// 测试数据：请假审批流程
const leaveProcessNodes = [
  {
    nodeId: 'Start',
    nodeName: '开始',
    nodeType: 'startEvent',
    nextNodes: { next: 'DirectManager' }
  },
  {
    nodeId: 'DirectManager',
    nodeName: '直接主管审批',
    nodeType: 'userTask',
    assigneeValue: '${direct_manager}',
    nextNodes: { APPROVE: 'DaysCheck', REJECT: 'End' },
    conditions: {
      APPROVE: { expression: '${approve_result == \'APPROVE\'}', next: 'DaysCheck' },
      REJECT: { expression: '${approve_result == \'REJECT\'}', next: 'End' }
    }
  },
  {
    nodeId: 'DaysCheck',
    nodeName: '请假天数检查',
    nodeType: 'exclusiveGateway',
    nextNodes: { short: 'HR', long: 'SeniorManager' },
    conditions: {
      short: { expression: '${leave_days <= 3}', next: 'HR' },
      long: { expression: '${leave_days > 3}', next: 'SeniorManager' }
    }
  },
  {
    nodeId: 'SeniorManager',
    nodeName: '上级主管审批(>3天)',
    nodeType: 'userTask',
    assigneeValue: '${senior_manager}',
    nextNodes: { APPROVE: 'HR', REJECT: 'End' },
    conditions: {
      APPROVE: { expression: '${approve_result == \'APPROVE\'}', next: 'HR' },
      REJECT: { expression: '${approve_result == \'REJECT\'}', next: 'End' }
    }
  },
  {
    nodeId: 'HR',
    nodeName: 'HR审批',
    nodeType: 'userTask',
    assigneeValue: '${hr}',
    nextNodes: { next: 'End' }
  },
  {
    nodeId: 'End',
    nodeName: '结束',
    nodeType: 'endEvent',
    nextNodes: {}
  }
]

describe('诊断测试 - 检查 DirectManager -> End 路径', () => {
  it('应该显示 DirectManager -> End 的拒绝路径', () => {
    const layoutNodes = layoutFlowNodes(leaveProcessNodes)
    const connections = calculateViewConnections(layoutNodes)

    console.log('=== 节点布局 ===')
    layoutNodes.forEach(n => {
      console.log(`${n.id}: (${n.x}, ${n.y})`)
    })

    console.log('\n=== 所有连接 ===')
    connections.forEach(c => {
      console.log(`${c.sourceNode} -> ${c.targetNode}: label="${c.label}", color=${c.color}`)
    })

    // 验证 DirectManager -> End (REJECT) 连接存在
    const directManagerToEnd = connections.find(c => c.sourceNode === 'DirectManager' && c.targetNode === 'End')
    expect(directManagerToEnd).toBeDefined(`DirectManager -> End 连接应该存在`)
    expect(directManagerToEnd.label).toBe('')
    expect(directManagerToEnd.color).toBe('#1890ff')
  })

  it('DaysCheck 应该只有两条分支线', () => {
    const layoutNodes = layoutFlowNodes(leaveProcessNodes)
    const connections = calculateViewConnections(layoutNodes)

    // DaysCheck 发出的连接
    const daysCheckConnections = connections.filter(c => c.sourceNode === 'DaysCheck')
    console.log('\n=== DaysCheck 的连接 ===')
    daysCheckConnections.forEach(c => {
      console.log(`DaysCheck -> ${c.targetNode}: label="${c.label}"`)
    })

    expect(daysCheckConnections).toHaveLength(2)
  })

  it('总共应该有 8 条连接', () => {
    const layoutNodes = layoutFlowNodes(leaveProcessNodes)
    const connections = calculateViewConnections(layoutNodes)

    console.log('\n=== 总连接数 ===')
    console.log(`连接数: ${connections.length}`)

    expect(connections).toHaveLength(8)
  })
})