<template>
  <div class="designer">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo" />
        <a-menu
          theme="dark"
          mode="horizontal"
          :default-selected-keys="['4']"
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
          <a-breadcrumb-item>流程设计</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="site-layout-content">
          <h2>流程设计器</h2>
          <p>可视化设计审批流程，支持 BPMN 2.0 标准</p>
          <div class="designer-container">
            <a-card title="流程设计">
              <p>Flowable Modeler 集成区域</p>
              <p>提示：此处将集成 Flowable Modeler 进行流程设计</p>
              <a-button type="primary" style="margin-top: 16px">
                新建流程
              </a-button>
              <a-button style="margin-top: 16px; margin-left: 8px">
                导入流程
              </a-button>
            </a-card>
          </div>
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        通用审批服务 ©2026 Created by Universal Approval Team
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup>
// Designer view component
</script>

<style scoped>
.designer {
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

.designer-container {
  margin-top: 24px;
}
</style>