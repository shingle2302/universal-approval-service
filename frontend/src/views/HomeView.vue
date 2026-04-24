<template>
  <div class="home">
    <a-layout class="layout">
      <a-layout-header>
        <div class="logo" />
        <a-menu
          theme="dark"
          mode="horizontal"
          :default-selected-keys="['1']"
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
          <a-breadcrumb-item>首页</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="site-layout-content">
          <h1>通用审批服务</h1>
          <p>欢迎使用通用审批基础服务管理系统</p>
          <a-row style="margin-top: 24px">
            <a-col :span="6">
              <a-card title="模板管理" hoverable>
                <p>创建和管理审批流程模板</p>
                <a-button type="primary" style="margin-top: 16px">
                  <router-link to="/template">进入</router-link>
                </a-button>
              </a-card>
            </a-col>
            <a-col :span="6" :offset="2">
              <a-card title="任务管理" hoverable>
                <p>处理待办审批任务</p>
                <a-button type="primary" style="margin-top: 16px">
                  <router-link to="/task">进入</router-link>
                </a-button>
              </a-card>
            </a-col>
            <a-col :span="6" :offset="2">
              <a-card title="流程设计" hoverable>
                <p>可视化设计审批流程</p>
                <a-button type="primary" style="margin-top: 16px">
                  <router-link to="/designer">进入</router-link>
                </a-button>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        通用审批服务 ©2026 Created by Universal Approval Team
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup>
// Home view component
</script>

<style scoped>
.home {
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