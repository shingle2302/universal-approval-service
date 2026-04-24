<template>
  <div class="template">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo" />
        <a-menu
          theme="dark"
          mode="horizontal"
          :default-selected-keys="['2']"
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
          <a-breadcrumb-item>模板管理</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="site-layout-content">
          <a-row style="margin-bottom: 16px">
            <a-col :span="12">
              <h2>审批模板管理</h2>
            </a-col>
            <a-col :span="12" style="text-align: right">
              <a-button type="primary">创建模板</a-button>
            </a-col>
          </a-row>
          <a-table :columns="columns" :data-source="templates" row-key="id">
            <template #action="{ record }">
              <a-space size="small">
                <a-button type="link">编辑</a-button>
                <a-button type="link">部署</a-button>
                <a-button type="link" danger>删除</a-button>
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
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode' },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName' },
  { title: '版本', dataIndex: 'version', key: 'version' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', slots: { customRender: 'action' } }
]

const templates = ref([
  {
    id: 1,
    templateCode: 'PURCHASE_001',
    templateName: '通用采购审批流',
    version: 1,
    status: '已部署',
    createTime: '2026-04-24 10:00:00'
  },
  {
    id: 2,
    templateCode: 'LEAVE_001',
    templateName: '请假审批流',
    version: 1,
    status: '未部署',
    createTime: '2026-04-24 11:00:00'
  }
])
</script>

<style scoped>
.template {
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