# Universal Approval Service

一个基于 **Spring Boot + Flowable + MyBatis-Plus + Vue 3** 的通用审批系统，支持：

- 审批模板管理（创建 / 更新 / 部署 / 删除）
- 审批流程发起、查询、变量维护
- 任务查询、审批、委派、驳回回退
- JSON 模板动态生成 BPMN
- 前端可视化管理界面

---

## 1. 技术栈

### 后端
- Java 17
- Spring Boot 3.4
- Flowable 7.1
- MyBatis-Plus 3.5
- H2 Database（默认）
- springdoc-openapi（Swagger UI）

### 前端
- Vue 3（Composition API）
- Vue Router
- Axios
- Vite
- Vitest + Vue Test Utils

---

## 2. 项目结构

```text
.
├── src/main/java/com/universal/approval
│   ├── controller      # REST API
│   ├── service         # 流程与模板核心业务
│   ├── mapper          # MyBatis-Plus Mapper
│   ├── entity          # 数据实体
│   ├── model           # JSON模板与DTO
│   ├── event           # 审批事件定义/发布
│   └── command/listener# Flowable扩展命令与监听器
├── src/main/resources
│   ├── application.yml
│   ├── schema.sql
│   └── data.sql
├── src/test            # 后端测试
├── frontend            # Vue 前端
│   ├── src/views       # 页面
│   ├── src/components  # 组件
│   └── src/__tests__   # 前端单元测试
└── docs                # 设计文档与实施计划
```

---

## 3. 快速启动

## 3.1 启动后端

```bash
./mvnw spring-boot:run
```

默认服务端口：`8080`

常用入口：
- OpenAPI 文档：`http://localhost:8080/swagger-ui/index.html`
- H2 控制台（如开启）：`http://localhost:8080/h2-console`

## 3.2 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认访问：`http://localhost:5173`

Vite 已配置 `/api` 代理到后端，开发时可直接调用后端接口。

---

## 4. 后端核心 API（简版）

所有接口统一前缀：`/approval`

## 4.1 模板管理
- `POST /approval/template`：创建模板
- `GET /approval/template`：模板列表
- `GET /approval/template/{code}`：查询模板
- `PUT /approval/template/{code}`：更新模板
- `DELETE /approval/template/{code}`：删除模板
- `POST /approval/template/{code}/deploy`：部署模板

## 4.2 流程管理
- `POST /approval/process`：发起流程
- `GET /approval/process`：查询流程（可按 businessKey）
- `GET /approval/process/{id}`：流程详情
- `DELETE /approval/process/{id}`：终止流程
- `GET /approval/process/{id}/variables`：查询流程变量
- `PUT /approval/process/{id}/variables`：更新流程变量

## 4.3 任务管理
- `GET /approval/task`：查询任务（可按 assignee）
- `GET /approval/task/{id}`：任务详情
- `POST /approval/task/{id}`：完成任务
- `POST /approval/task/{id}/delegate`：委派任务
- `POST /approval/task/{id}/rollback`：回退任务

---

## 5. JSON 模板与 BPMN

系统支持通过 JSON 模板描述流程节点（开始、审批、网关、结束等），由 `BpmnGeneratorService` 动态转换为 BPMN 模型并部署到 Flowable。

建议模板字段包含：
- `templateCode` / `templateName` / `version`
- `globalConfig.startNode` / `globalConfig.endNode`
- `nodes[]`（含节点类型、流转关系、条件表达式）

可参考现有测试用例中的示例模板：
- `BpmnGeneratorServiceTest`
- `LeaveProcessTest`

---

## 6. 测试

### 后端测试
```bash
./mvnw test
```

### 前端测试
```bash
cd frontend
npm test
```

---

## 7. 开发建议

- 先在「模板管理」创建并部署模板，再发起流程。
- 流程变量命名保持一致，便于网关表达式判断。
- 生产环境建议将 H2 替换为 MySQL/PostgreSQL，并配置独立消息/日志/监控方案。

---

## 8. 后续可扩展方向

- 多租户隔离（模板、流程、任务按租户维度分区）
- 更细粒度权限模型（RBAC + 数据权限）
- 统一消息中心（站内信、邮件、IM）
- 审批时限 SLA 与自动催办
- 可视化流程设计器的拖拽能力增强

