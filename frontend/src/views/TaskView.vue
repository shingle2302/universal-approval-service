<template>
  <div class="task">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo" />
        <a-menu
          theme="dark"
          mode="horizontal"
          :default-selected-keys="['3']"
          style="line-height: 64px"
        >
          <a-menu-item key="1">
            <router-link to="/">首页</router-link>
          </a-menu-item>
          <a-menu-item key="2">
            <router-link to="/template">模板管理</router-link>
          </a-menu-item>
          <a-menu-item key="3">
            <router-link to="/task">任务管理</router-link>
          </a-menu-item>
          <a-menu-item key="4">
            <router-link to="/designer">流程设计</router-link>
          </a-menu-item>
        </a-menu>
      </a-layout-header>
      <a-layout-content style="padding: 0 50px">
        <a-breadcrumb style="margin: 16px 0">
          <a-breadcrumb-item><router-link to="/">首页</router-link></a-breadcrumb-item>
          <a-breadcrumb-item>任务管理</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="site-layout-content">
          <h2>待办审批任务</h2>
          <a-table :columns="columns" :data-source="tasks" row-key="id">
            <template #action="{ record }">
              <a-space size="small">
                <a-button type="primary">处理</a-button>
                <a-button type="link">委派</a-button>
                <a-button type="link" danger>退回</a-button>
              </a-space>
            </template>
          </a-table>
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        通用审批服务 ©2026 Created by Universal Approval Team
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id' },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '流程实例', dataIndex: 'processInstanceId', key: 'processInstanceId' },
  { title: '业务主键', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', slots: { customRender: 'action' } }
]

const tasks = ref([
  {
    id: 'task1',
    name: '主管审批',
    processInstanceId: 'proc1',
    businessKey: 'PO-2026-001',
    createTime: '2026-04-24 09:00:00'
  },
  {
    id: 'task2',
    name: '总监审批',
    processInstanceId: 'proc2',
    businessKey: 'PO-2026-002',
    createTime: '2026-04-24 10:00:00'
  }
])
</script>

<style scoped>
.task {
  min-height: 100vh;
}

.logo {
  width: 120px;
  height: 31px;
  background: rgba(255, 255, 255, 0.2);
  margin: 16px 24px 16px 0;
  float: left;
}

.site-layout-content {
  background: #fff;
  padding: 24px;
  min-height: 280px;
}
</style>