# 知识图谱：Universal Approval Service

## 项目概述
Universal Approval Service 是一个采用 Java + Maven 结构的全栈应用，集成了 React 前端和 Flowable BPMN 引擎，提供 RESTful API 为企业审批流程提供自动化支持。

## 主要层次结构

```
digraph universal_approval_service {
    // 核心实体
    Project [label="universal-approval-service"];
    Backend [label="Backend (Java / Spring‑like)"];
    Frontend [label="Frontend (React)"];
    ProcessService [label="Process Service"];
    FlowableEngine [label="Flowable BPMN Engine"];
    
    // 控制器
    ProcessController [label="ProcessController (REST)"];
    TaskController [label="TaskController (REST)"];
    
    // 辅助服务/模型
    BpmnGeneratorService [label="BpmnGeneratorService"];
    GlobalConfig [label="GlobalConfig (config)"];
    JsonTemplate [label="JsonTemplate (workflow template)"];
    NodeConfig [label="NodeConfig (Flowable node cfg)"];
    
    // 关系
    Project -> Backend;
    Project -> Frontend;
    Backend -> ProcessService;
    ProcessService -> FlowableEngine;
    ProcessService -> ProcessController;
    ProcessService -> TaskController;
    ProcessService -> BpmnGeneratorService;
    ProcessService -> GlobalConfig;
    ProcessService -> JsonTemplate;
    ProcessService -> NodeConfig;
    Frontend -> RouterAndViews;
    JsonTemplate -> Backend;
    NodeConfig -> Backend;
}
```

## 关键节点说明

| 节点 | 类型 | 主要职责 |
|------|------|----------|
| **Project** | 项目 | 包含整个仓库，既有后端 Java 代码，也有前端 React 资源。 |
| **Backend** | 层 | 位于 `src/main/java`，实现 Spring‑风格的控制器和服务。 |
| **Frontend** | 层 | 位于 `frontend` 目录，管理路由、视图（DesignerView、HomeView、TaskView、TemplateView 等）。 |
| **ProcessService** | 服务 | 业务核心，负责流程管理并与 Flowable 引擎协同。 |
| **FlowableEngine** | 技术 | 内嵌的 BPMN 执行引擎，负责流程实例的运行与状态管理。 |
| **ProcessController** | 控制器 | 向外提供流程相关的 REST 接口。 |
| **TaskController** | 控制器 | 向外提供任务相关的 REST 接口。 |
| **BpmnGeneratorService** | 服务 | 根据 JSON 模板生成 BPMN XML 文件。 |
| **GlobalConfig** | 配置 | 集中管理全局配置项。 |
| **JsonTemplate** | 模型 | 为工作流定义提供 JSON 结构化模板。 |
| **NodeConfig** | 模型 | 定义 Flowable 中节点的配置信息。 |

## 核心关系链

- **后端 → 流程服务**：业务逻辑集中在 `ProcessService` 中，该服务直接调用 Flowable 引擎完成流程调度。
- **流程服务 → 控制器**：`ProcessController` 与 `TaskController` 通过 REST 把服务暴露给前端或外部系统。
- **流程服务 → BPMN 生成服务**：业务流程定义通过 `BpmnGeneratorService` 生成可执行的 BPMN XML。
- **配置/模型 → 服务**：`GlobalConfig`、`JsonTemplate`、`NodeConfig` 为流程服务提供必要的配置和模板数据。
- **前端 ↔ 后端**：前端通过路由和视图与后端交互，实现用户界面与业务流程的结合。

## 架构洞察

1. **分层清晰**：项目采用典型的前后端分层架构，前端使用 React，后端使用 Java，两者在同一个仓库中协同管理。
2. **流程中心**：`ProcessService` 是系统的核心，它封装了所有与企业审批流程相关的业务逻辑，并通过 Flowable 实现流程的实际执行。
3. **可插拔的模板机制**：工作流定义使用 JSON 模板 (`JsonTemplate`) 与节点配置 (`NodeConfig`) 实现，使得流程定义能够动态生成且易于维护。
4. **RESTful 接口**：通过 `ProcessController` 与 `TaskController` 提供的 RESTful API，支持外部系统或前端应用对流程和任务进行创建、查询、更新和删除操作。
5. **扩展友好**：诸如“移动端支持”“AI 智能匹配”“供应商体验增强”等增强功能可在现有层结构之上叠加，而不影响核心流程服务的稳定性。

> 该知识图谱帮助理清了系统的整体结构、主要组成部件及其相互关系，为后续的功能扩展、性能优化或架构改进提供了清晰的参考框架。