# MediCraft — 基于大模型的个性化资源生成与学习多智能体系统

## 项目开发说明书

---

## 目录

- [一、项目概述](#一项目概述)
  - [1.1 项目背景](#11-项目背景)
  - [1.2 项目目标](#12-项目目标)
  - [1.3 技术选型总览](#13-技术选型总览)
- [二、系统总体架构设计](#二系统总体架构设计)
  - [2.1 系统架构图](#21-系统架构图)
  - [2.2 前端架构](#22-前端架构)
  - [2.3 后端架构](#23-后端架构)
  - [2.4 数据流设计](#24-数据流设计)
- [三、多智能体系统设计](#三多智能体系统设计)
  - [3.1 智能体总体架构](#31-智能体总体架构)
  - [3.2 策略工厂模式](#32-策略工厂模式)
  - [3.3 六大资源生成智能体](#33-六大资源生成智能体)
  - [3.4 评估智能体](#34-评估智能体)
  - [3.5 智能辅导智能体](#35-智能辅导智能体)
  - [3.6 异步任务管线](#36-异步任务管线)
- [四、核心功能模块设计](#四核心功能模块设计)
  - [4.1 对话式学习画像构建](#41-对话式学习画像构建)
  - [4.2 多智能体资源生成](#42-多智能体资源生成)
  - [4.3 个性化学习路径规划](#43-个性化学习路径规划)
  - [4.4 智能辅导系统](#44-智能辅导系统)
  - [4.5 学习效果评估](#45-学习效果评估)
- [五、数据库设计](#五数据库设计)
  - [5.1 ER 关系图](#51-er-关系图)
  - [5.2 核心表结构](#52-核心表结构)
- [六、API 接口设计](#六api-接口设计)
  - [6.1 接口总览](#61-接口总览)
  - [6.2 接口鉴权机制](#62-接口鉴权机制)
- [七、前端界面与交互设计](#七前端界面与交互设计)
  - [7.1 页面路由设计](#71-页面路由设计)
  - [7.2 核心交互流程](#72-核心交互流程)
  - [7.3 UI/UX 设计亮点](#73-uiux-设计亮点)
- [八、关键技术与创新点](#八关键技术与创新点)
- [九、部署与运行说明](#九部署与运行说明)

---

## 一、项目概述

### 1.1 项目背景

在数字化与智能化深度融合的时代，高等教育的个性化变革成为核心发展方向。不同学生在知识基础、学习能力、兴趣方向上存在显著差异，标准化教学难以满足个性化学习需求。当前大模型技术高速发展，以通用大模型、多模态生成大模型为代表的技术体系，具备强大的自然语言理解、多模态内容生成及实时推理能力，为高等教育领域的创新升级带来全新契机。

MediCraft 正是在此背景下应运而生的**基于大模型的个性化资源生成与学习多智能体系统**，以计算机/人工智能专业课程为切入点，借助多智能体协作与大模型技术，实现个性化学习资源的自动化生成与智能化学习引导，真正做到"因材施教"的数字化落地。

### 1.2 项目目标

本系统围绕以下五大核心目标进行设计与开发：

| 目标编号 | 目标描述 |
|---------|---------|
| G1 | **对话式画像构建** — 通过自然语言对话自动抽取≥6个维度的学生学习画像，支持随学随新 |
| G2 | **多智能体资源生成** — 6个专业智能体协作生成≥5种类型的个性化学习资源 |
| G3 | **个性化路径规划** — AI驱动的动态学习路径规划与精准资源推送 |
| G4 | **智能辅导答疑** — 即时多模态答疑，结合文字解答与知识点关联 |
| G5 | **学习效果评估** — 多维度精准评估，动态调整学习策略 |

### 1.3 技术选型总览

```
┌─────────────────────────────────────────────────────────────────┐
│                        技术栈全景                                │
├──────────────┬──────────────────────────────────────────────────┤
│ 前端          │ Vue 3.5 + Vite 6 + Element Plus 2.9            │
│              │ Pinia 3 (状态管理) + Vue Router 4                │
│              │ Axios 1.9 (HTTP) + Marked 16 (Markdown渲染)     │
│              │ ECharts 6 (数据可视化) + Lucide Vue (图标库)     │
├──────────────┼──────────────────────────────────────────────────┤
│ 后端          │ Java 17 + Spring Boot 3.3.5                     │
│              │ Spring AI 1.0.0-M6 (大模型集成框架)              │
│              │ Spring Security + JWT (认证鉴权)                 │
│              │ MyBatis-Plus 3.5.11 (ORM)                       │
├──────────────┼──────────────────────────────────────────────────┤
│ AI 大模型     │ 阿里云 DashScope (通义千问系列)                  │
│              │ 通过 OpenAI 兼容协议接入，可切换 qwen/deepseek    │
│              │ 当前主力模型: qwen3.6-35b-a3b                    │
├──────────────┼──────────────────────────────────────────────────┤
│ 数据存储      │ MySQL 8.0 (主数据库)                            │
│              │ Redis 7.x (会话缓存、JWT令牌、画像对话上下文)    │
├──────────────┼──────────────────────────────────────────────────┤
│ 构建与部署    │ Maven 3.x (后端) + Node 20 + pnpm (前端)       │
│              │ Vite 开发服务器 (前端热更新)                      │
└──────────────┴──────────────────────────────────────────────────┘
```

---

## 二、系统总体架构设计

### 2.1 系统架构图

```
                        ┌─────────────────────────────┐
                        │         用户浏览器            │
                        │   (Vue 3 SPA 单页应用)        │
                        └──────────────┬──────────────┘
                                       │ HTTP/HTTPS
                                       ▼
                        ┌─────────────────────────────┐
                        │      Nginx / Vite Dev        │
                        │     (静态资源 & 反向代理)      │
                        └──────────────┬──────────────┘
                                       │ :8080
                                       ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Spring Boot 3.3.5 应用                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  安全层: JWT Token 拦截器 → BaseContext (ThreadLocal)        │ │
│  └──────────────────────────┬──────────────────────────────────┘ │
│                              ▼                                    │
│  ┌───────────────────── Controller 层 ─────────────────────────┐ │
│  │  UserCtrl  ProfileCtrl  ResourceCtrl  PathCtrl  AssessCtrl  │ │
│  │  TutorCtrl  AgentCtrl                                       │ │
│  └──────────────────────────┬──────────────────────────────────┘ │
│                              ▼                                    │
│  ┌───────────────────── Service 层 ────────────────────────────┐ │
│  │  UserService   AIChatService   ResourceService               │ │
│  │  LearningPathService   AssessmentService  SmartTutorService  │ │
│  └──────┬──────────────────────┬───────────────────────────────┘ │
│         ▼                       ▼                                 │
│  ┌─────────────┐   ┌──────────────────────────────────────┐     │
│  │  MySQL 8.0   │   │       多智能体引擎                     │     │
│  │  (14张业务表) │   │  ┌──────────────────────────────┐   │     │
│  └──────────────┘   │  │  AgentStrategyFactory         │   │     │
│  ┌──────────────┐   │  │  (策略工厂，路由到具体智能体)   │   │     │
│  │  Redis 7.x    │   │  └──────────┬───────────────────┘   │     │
│  │  (会话/缓存)  │   │             ▼                        │     │
│  └──────────────┘   │  ┌────────────────────────────────┐  │     │
│                      │  │  6大资源生成策略 + 评估智能体    │  │     │
│                      │  │  Document | Mind | Question     │  │     │
│                      │  │  Case | Demand | Multimodal     │  │     │
│                      │  │  AssessmentAIAgent | Tutor       │  │     │
│                      │  └──────────┬─────────────────────┘  │     │
│                      │             ▼                        │     │
│                      │  ┌──────────────────────────────┐   │     │
│                      │  │  Spring AI ChatClient          │   │     │
│                      │  │  (OpenAI 兼容协议)             │   │     │
│                      │  └──────────┬───────────────────┘   │     │
│                      └─────────────┼───────────────────────┘     │
│                                    ▼                              │
│                      ┌──────────────────────────────┐            │
│                      │  阿里云 DashScope API          │            │
│                      │  (qwen3.6-35b-a3b 等模型)     │            │
│                      └──────────────────────────────┘            │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 前端架构

前端采用 **Vue 3 Composition API** 构建的单页应用（SPA），遵循关注点分离原则：

```
MediCraft-web/
├── src/
│   ├── api/                     # API 接口层
│   │   ├── request.js           # Axios 实例 + 请求/响应拦截器
│   │   ├── user.js              # 用户认证接口
│   │   ├── profile.js           # 学习画像接口
│   │   ├── resource.js          # 资源生成接口
│   │   ├── resourceAgent.js     # 智能体列表接口
│   │   ├── learningPath.js      # 学习路径接口
│   │   ├── assessment.js        # 学习评估接口
│   │   └── tutor.js             # 智能辅导接口
│   ├── assets/                  # 静态资源 (CSS/图片)
│   ├── components/              # 通用组件
│   │   ├── AppHeader.vue        # 全局顶栏导航
│   │   ├── ChatMessage.vue      # AI 对话消息气泡
│   │   └── ProgressPoller.vue   # 异步任务进度轮询器
│   ├── router/
│   │   └── index.js             # 路由配置 (12条路由)
│   ├── stores/                  # Pinia 状态管理
│   │   ├── user.js              # 用户认证状态
│   │   ├── profile.js           # 学习画像状态
│   │   └── learningPath.js      # 学习路径状态
│   ├── utils/
│   │   └── auth.js              # Token 存取工具
│   └── views/                   # 页面视图
│       ├── HomeView.vue             # 首页
│       ├── LoginView.vue            # 登录/注册
│       ├── ProfileView.vue          # 学习画像
│       ├── ProfileChatView.vue      # 画像构建对话
│       ├── ai-resource/             # AI 资源模块
│       │   ├── ResourceListView.vue     # 资源列表
│       │   ├── ResourceGenerateView.vue # 资源生成
│       │   └── ResourceDetailView.vue   # 资源详情
│       ├── learning-path/           # 学习路径模块
│       │   ├── PathListView.vue         # 路径列表
│       │   ├── PathDetailView.vue       # 路径详情/日历
│       │   ├── PathStepView.vue         # 路径步骤
│       │   └── StepResourceView.vue     # 步骤关联资源
│       ├── ai-tutor/                # 智能辅导模块
│       │   └── AiTutorView.vue         # AI 对话辅导
│       ├── assessment/              # 学习评估模块
│       │   ├── AssessmentReportView.vue # 评估报告
│       │   └── LearningOutcomeView.vue  # 学习成果
│       └── user/
│           ├── UserProfileView.vue  # 个人信息
│           └── ChangePasswordView.vue# 修改密码
├── vite.config.js               # Vite 配置 (API 代理)
└── package.json
```

**关键设计决策：**

- **Axios 拦截器统一处理**：请求拦截器自动注入 JWT Token，响应拦截器处理 401 自动跳转登录
- **Pinia 持久化**：User Store 使用 `localStorage` 持久化登录状态
- **Markdown 实时渲染**：所有 AI 生成内容通过 `marked` 库实时渲染为富文本
- **异步轮询机制**：资源生成采用 `ProgressPoller` 组件，前端轮询任务进度直至完成

### 2.3 后端架构

后端采用**领域驱动的单体架构**，按业务域划分包结构：

```
com.education
├── MediCraftApplication.java           # Spring Boot 启动类
├── config/
│   ├── MybatisPlusConfig.java          # MyBatis-Plus 分页插件
│   └── SchedulingConfig.java           # 定时任务配置
├── resource/                           # 【资源生成域】
│   ├── agent/                          # 智能体策略实现
│   │   ├── AgentStrategyFactory.java   # 策略工厂
│   │   ├── DocumentAgentStrategy       # 文档生成智能体
│   │   ├── MindAgentStrategy           # 思维导图智能体
│   │   ├── QuestionAgentStrategy       # 题目生成智能体
│   │   ├── CaseAgentStrategy           # 实操案例智能体
│   │   ├── DemandAgentStrategy         # 需求分析智能体
│   │   ├── MultimodalAgentStrategy     # 多模态智能体
│   │   └── VideoAgent                  # 视频脚本智能体 (SSE)
│   ├── config/
│   │   ├── AiConfig.java               # AI 模型配置 (ChatClient)
│   │   └── AsyncConfig.java            # 异步线程池配置
│   ├── controller/
│   │   ├── ResourceController.java     # 资源 CRUD + 生成
│   │   └── ResourceAgentController.java# 智能体列表
│   ├── service/
│   │   ├── AgentGenerateStrategy.java  # 策略接口
│   │   ├── AIChatService.java          # 画像对话接口
│   │   ├── BaseAgent.java              # SSE 流式基类
│   │   └── impl/
│   │       ├── ResourceService.java        # 资源业务
│   │       ├── ResourceAsyncExecutor.java  # 异步执行器
│   │       └── AIChatServiceImpl.java      # 画像对话实现
│   └── utils/
│       └── SpringAiUtil.java           # AI 调用工具类
├── path/                               # 【学习路径域】
│   ├── controller/LearningPathController
│   └── service/impl/LearningPathServiceImpl
├── outcome/                            # 【学习评估域】
│   ├── agent/AssessmentAIAgent         # 评估智能体
│   ├── controller/AssessmentController
│   ├── service/impl/                   # 评估服务实现
│   └── utils/AssessmentScheduleTask    # 定时评估任务
├── tutor/                              # 【智能辅导域】
│   ├── controller/TutorController
│   └── service/impl/SmartTutorServiceImpl
└── user/                               # 【用户画像域】
    ├── controller/
    │   ├── UserController              # 认证接口
    │   └── ProfileController           # 画像接口
    └── service/impl/
        ├── UserServiceImpl             # 用户服务
        └── ProfileServiceImpl          # 画像服务
```

### 2.4 数据流设计

```
用户操作 → Vue 组件 → API 调用 (Axios + JWT) → Controller
    → Service 业务逻辑 → AI 调用 (Spring AI ChatClient)
    → DashScope API → 返回结构化内容 → 持久化到 MySQL
    → 返回前端 → Markdown 渲染展示

异步资源生成流程：
    前端提交生成请求 → Controller 返回 taskId (立即返回)
    → @Async 线程池执行生成任务 → 更新 ResourceGenerateTask 进度
    → 前端轮询进度接口 → 进度100% → 跳转资源详情

画像对话流程：
    前端发起对话 → 加载 Redis 中的历史上下文
    → 拼接学生画像 + 对话历史 + 用户输入 → 调用 AI
    → 返回 AI 回复 → 双写 Redis + MySQL
    → 用户说"完成" → AI 提取画像 JSON → 更新 StudentProfile
```

---

## 三、多智能体系统设计

### 3.1 智能体总体架构

MediCraft 的多智能体系统是整个项目的技术核心。系统采用 **策略模式 + 工厂模式** 实现智能体的可插拔、可扩展架构，所有智能体共享统一的 AI 调用基础设施，但各自拥有独立的 Prompt 模板和业务逻辑。

```
┌──────────────────────────────────────────────────────────────┐
│                     多智能体协同架构                           │
│                                                              │
│   ┌─────────────────────────────────────────────────────┐   │
│   │             AgentStrategyFactory (策略工厂)           │   │
│   │   Map<String, AgentGenerateStrategy> strategyMap     │   │
│   │   自动发现所有 @Component 策略实现并注册               │   │
│   └──────────────────────┬──────────────────────────────┘   │
│                          │ getStrategy(agentRole)            │
│          ┌───────────────┼───────────────┐                  │
│          ▼               ▼               ▼                  │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐              │
│   │ Document  │   │   Mind   │   │ Question │              │
│   │  Agent    │   │  Agent   │   │  Agent   │              │
│   │ (文档)    │   │ (导图)   │   │ (题目)   │              │
│   └──────────┘   └──────────┘   └──────────┘              │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐              │
│   │   Case   │   │  Demand  │   │Multimodal│              │
│   │  Agent   │   │  Agent   │   │  Agent   │              │
│   │ (实操)   │   │ (需求)   │   │ (多模态) │              │
│   └──────────┘   └──────────┘   └──────────┘              │
│                                                              │
│   ┌──────────────────┐   ┌──────────────────┐              │
│   │ AssessmentAgent   │   │  SmartTutor      │              │
│   │ (评估智能体)      │   │ (辅导智能体)     │              │
│   │ 独立于策略工厂    │   │ 独立于策略工厂   │              │
│   └──────────────────┘   └──────────────────┘              │
│                                                              │
│   共享基础设施:                                               │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  Spring AI ChatClient → DashScope (qwen3.6-35b)     │   │
│   │  SpringAiUtil.callAi(promptTemplate, params)         │   │
│   │  异步线程池 (core=2, max=5, queue=100)               │   │
│   └─────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 策略工厂模式

智能体系统的核心设计模式是 **Strategy + Factory**：

**策略接口 `AgentGenerateStrategy`：**

```java
public interface AgentGenerateStrategy {
    /**
     * 生成学习资源
     * @param aiAgent   智能体配置（含 Prompt 模板）
     * @param userId    用户ID
     * @param topic     生成主题
     * @param params    附加参数 (难度、数量、类型等)
     * @return 生成的学习资源实体
     */
    LearningResource generate(AiAgent aiAgent, Long userId,
                              String topic, Map<String, Object> params);

    /** 返回该策略支持的智能体角色标识 */
    String getSupportRole();
}
```

**策略工厂 `AgentStrategyFactory`：**

```java
@Component
public class AgentStrategyFactory {
    private final Map<String, AgentGenerateStrategy> strategyMap;

    @Autowired
    public AgentStrategyFactory(List<AgentGenerateStrategy> strategies) {
        // Spring 自动注入所有实现了 AgentGenerateStrategy 的 Bean
        // 按 getSupportRole() 建立映射
        strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                AgentGenerateStrategy::getSupportRole,
                Function.identity()
            ));
    }

    public AgentGenerateStrategy getStrategy(String agentRole) {
        return strategyMap.get(agentRole);
    }
}
```

**设计优势：**
- **开闭原则**：新增智能体只需实现接口并标注 `@Component`，无需修改工厂代码
- **数据库可配置**：智能体的名称、图标、Prompt 模板存储在 `ai_agent` 表中，支持运行时调整
- **统一调用入口**：`ResourceAsyncExecutor` 只需通过工厂获取策略即可，与具体智能体解耦

### 3.3 六大资源生成智能体

每个智能体遵循统一的生成流程：**构建 Prompt → 调用 AI → 解析响应 → 构造资源实体**。

#### 3.3.1 文档讲解智能体 (DocumentAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `document` |
| 生成类型 | 结构化学习文档 (Markdown) |
| 输入参数 | 课程主题、学习需求、难度等级 |
| 输出结构 | 知识点解析、原理说明、代码示例、总结 |

**Prompt 设计要点：**
- 要求 AI 以 Markdown 格式输出，包含多级标题结构
- 根据难度参数调整内容深度（基础/进阶/高级）
- 要求包含代码示例和实践要点
- 根据学生画像中的知识基础定制内容详略

#### 3.3.2 思维导图智能体 (MindAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `mind` |
| 生成类型 | 层级结构思维导图 (Markdown) |
| 输入参数 | 主题、关联知识点、难度 |
| 输出结构 | 多层级节点、节点描述、关联关系 |

#### 3.3.3 题目生成智能体 (QuestionAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `question` |
| 生成类型 | 练习题目 (JSON 数组) |
| 输入参数 | 主题、题目数量、题型、难度 |
| 输出结构 | 题目ID、题干、选项、正确答案、解析 |

**题目结构示例：**
```json
{
  "id": 1,
  "title": "以下关于卷积神经网络的描述，正确的是？",
  "options": ["A. CNN只能处理图像数据", "B. 卷积层通过卷积核提取局部特征", ...],
  "answer": "B",
  "analysis": "卷积层的核心机制是通过可学习的卷积核在输入数据上滑动..."
}
```

#### 3.3.4 实操案例智能体 (CaseAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `case` |
| 生成类型 | 代码实操案例 (Markdown) |
| 输入参数 | 主题、编程语言、难度 |
| 输出结构 | 实践目标、环境配置、分步操作、可运行代码、预期输出、常见问题 |

#### 3.3.5 需求分析智能体 (DemandAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `demand` |
| 生成类型 | 学习需求分析文档 (Markdown) |
| 输入参数 | 学生信息、课程内容、学习目标 |
| 输出结构 | 学习目标、知识点清单、推荐路径、前置知识、预估时长、推荐资源类型 |

#### 3.3.6 多模态智能体 (MultimodalAgentStrategy)

| 属性 | 说明 |
|------|------|
| 角色标识 | `multimodal` |
| 生成类型 | 教学视频脚本 (Markdown) |
| 输入参数 | 主题、视频风格、难度 |
| 输出结构 | 视频标题、分镜脚本(≥5个场景)、知识图解、总结提问 |

### 3.4 评估智能体

`AssessmentAIAgent` 是独立于策略工厂的专用智能体，负责生成学习效果评估报告：

```
输入数据:
  ├── 学生画像 (知识基础、学习目标、薄弱点)
  ├── 学习行为数据 (学习时长、资源使用、路径进度)
  ├── 答题记录 (正确率、耗时、知识点分布)
  └── 路径完成情况 (完成步骤、打卡记录)

Prompt 构造:
  "根据以下学生数据，生成学习评估报告：
   包含【学习概览】【知识点掌握度(返回JSON)】【薄弱点分析】【提升建议】四个部分..."

输出解析:
  ├── 学习概览 → evaluateContent 字段
  ├── 知识点掌握度 → 解析 JSON → knowledgeMastery 字段
  │   示例: {"机器学习基础": 85, "深度学习": 60, "NLP": 45}
  └── 提升建议 → improveSuggest 字段
```

### 3.5 智能辅导智能体

`SmartTutorServiceImpl` 提供即时答疑能力：

```
学生提问 → 加载 Prompt 模板 (可配置)
         → 拼接问题 + 画像上下文
         → 调用 ChatClient (同步)
         → 返回 Markdown 格式解答
         → 持久化到 smart_tutor 表
```

### 3.6 异步任务管线

资源生成采用异步任务管线，避免 AI 长时间调用阻塞用户界面：

```
前端请求                    后端处理                       前端轮询
   │                          │                              │
   │  POST /resource/generate │                              │
   │─────────────────────────>│                              │
   │                          │ 1. 校验智能体                 │
   │                          │ 2. 创建 ResourceGenerateTask  │
   │                          │    (status=pending)           │
   │                          │ 3. @Async 调用 Executor       │
   │  返回 {taskId, progress} │                              │
   │<─────────────────────────│                              │
   │                          │                              │
   │                          │ [后台线程]                    │
   │                          │ 4. 更新 status=running        │
   │                          │ 5. Factory.getStrategy()      │
   │                          │ 6. strategy.generate()        │
   │                          │ 7. 保存 LearningResource      │
   │                          │ 8. 更新 status=success        │
   │                          │                              │
   │  GET /resource/generate/progress/{taskId}                │
   │─────────────────────────────────────────────────────────>│
   │                          │   返回 {progress: 100,        │
   │                          │         resourceId: 123}      │
   │<────────────────────────────────────────────────────────│
   │                          │                              │
   │  → 跳转资源详情页                                        │
```

**线程池配置 (AsyncConfig)：**
- 核心线程数：2
- 最大线程数：5
- 队列容量：100
- 线程名前缀：`taskExecutor-`

---

## 四、核心功能模块设计

### 4.1 对话式学习画像构建

#### 4.1.1 画像维度设计

系统构建包含 **7 个维度** 的动态学生画像：

| 维度 | 字段 | 说明 | 示例值 |
|------|------|------|--------|
| 知识基础 | `knowledgeBase` | 学生当前知识水平描述 | "计算机科学大三，学过数据结构、操作系统" |
| 认知风格 | `cognitiveStyle` | 学生偏好的学习方式 | "视觉型学习者，偏好图表和示例" |
| 学习目标 | `learningGoal` | 短期/长期学习目标 | "掌握深度学习基础，完成期末项目" |
| 易错点偏好 | `errorPronePoints` | JSON 数组，常见错误类型 | `["混淆L1/L2正则化","梯度消失理解不清"]` |
| 学习节奏 | `learningPace` | 学习速度偏好 | "中等节奏，每天2小时" |
| 资源偏好 | `resourcePreference` | 偏好的资源类型 | "视频教程 + 动手实操" |
| 学习习惯 | `learningHabits` | 学习行为模式 | "喜欢先看视频再做练习，周末集中学习" |

#### 4.1.2 对话流程设计

```
┌──────────┐     ┌───────────────┐     ┌────────────────┐
│ 用户发起  │────>│ 开始画像会话    │────>│ AI 引导式提问   │
│ 画像构建  │     │ POST /profile  │     │ 多轮自然语言    │
│          │     │ /build/start   │     │ 对话收集信息    │
└──────────┘     └───────────────┘     └───────┬────────┘
                                               │
                    ┌──────────────────────────┐│
                    │  对话循环 (N 轮)          ││
                    │                          ││
                    │  1. 用户输入个人信息      <┘
                    │  2. 加载 Redis 中的历史上下文
                    │  3. 拼接已有画像 + 对话历史
                    │  4. AI 生成引导性回复
                    │  5. 双写 Redis + MySQL
                    │  6. 判断是否收集完成
                    │         │                │
                    │    收集完成？              │
                    │    /     \               │
                    │   否      是             │
                    │   │       │              │
                    │   └──┐    │              │
                    │      │    ▼              │
                    │      │  AI 提取画像 JSON  │
                    │      │  解析为 StudentProfile
                    │      │  更新数据库        │
                    │      │                   │
                    └──────┴───────────────────┘
```

**AI Prompt 设计核心逻辑：**
- 首轮对话：欢迎 + 询问专业和年级
- 中间轮次：逐个引导缺失维度的信息收集
- 每轮检查已有画像数据，避免重复询问
- 检测到"信息收集完成"关键词或用户说"完成"时触发画像提取

### 4.2 多智能体资源生成

#### 4.2.1 资源类型体系

| 资源类型 | contentFormat | 智能体 | 渲染方式 |
|---------|--------------|--------|---------|
| 课程讲解文档 | `document` | DocumentAgentStrategy | Markdown 渲染 |
| 知识点思维导图 | `mind` | MindAgentStrategy | Markdown 层级渲染 |
| 练习题目 | `question` | QuestionAgentStrategy | JSON 交互式题目卡片 |
| 实操案例 | `case` | CaseAgentStrategy | Markdown + 代码高亮 |
| 需求分析文档 | `demand` | DemandAgentStrategy | Markdown 渲染 |
| 多模态脚本 | `multimodal` | MultimodalAgentStrategy | Markdown 分镜展示 |

#### 4.2.2 生成流程

```
用户选择智能体 + 输入主题和参数
    │
    ▼
前端 POST /resource/generate
    {agentId, topic, params: {difficulty, questionCount, ...}}
    │
    ▼
ResourceService:
    1. 验证 AiAgent 存在且启用
    2. 创建 ResourceGenerateTask (pending, progress=0)
    3. 异步调用 ResourceAsyncExecutor
    │
    ▼
ResourceAsyncExecutor (@Async):
    1. 更新任务状态 → running, progress=10
    2. 查询 AiAgent → 获取 agentRole
    3. AgentStrategyFactory.getStrategy(agentRole)
    4. strategy.generate(aiAgent, userId, topic, params)
       ├── 构建 Prompt (使用数据库模板或默认模板)
       ├── SpringAiUtil.callAi(chatClient, template, params)
       └── 构造 LearningResource 实体
    5. 保存 LearningResource 到数据库
    6. 更新任务 → success, progress=100, resourceId=xxx
    │
    ▼
前端轮询 /resource/generate/progress/{taskId}
    → progress < 100: 继续轮询，显示进度条
    → progress = 100: 跳转资源详情页
```

### 4.3 个性化学习路径规划

#### 4.3.1 路径生成算法

```
输入:
  ├── subject (学习科目)
  ├── goal (学习目标)
  ├── duration (学习周期，天)
  └── intensity (学习强度: easy/medium/hard)

AI 处理:
  1. 分析科目知识体系结构
  2. 根据目标确定必学知识点
  3. 按照先修关系编排学习顺序
  4. 结合周期和强度分配每步时长
  5. 输出 4-8 个步骤的 JSON 数组

输出 (每个步骤):
  {
    "stepName": "Python基础语法",
    "stepContent": "学习变量、数据类型、控制流、函数定义...",
    "sort": 1
  }

容错机制:
  AI 返回无效 → 使用默认4步学习路径兜底
```

#### 4.3.2 动态路径调整

系统支持 5 种路径调整模式：

| 调整类型 | 说明 | AI 参与 |
|---------|------|--------|
| **extend** | 延长学习周期 | AI 重新分配未完成步骤的时长和内容 |
| **compress** | 压缩学习周期 | AI 合并精简未完成步骤 |
| **reorder** | 调整步骤顺序 | 直接更新排序字段，无需 AI |
| **add** | 新增学习步骤 | AI 根据指定知识点生成 2-3 个新步骤 |
| **remove** | 移除学习步骤 | 软删除未完成步骤，无需 AI |

#### 4.3.3 智能资源推荐

路径详情页可一键获取与当前步骤匹配的推荐资源：

```
POST /learning-path/{pathId}/recommend
    │
    ▼
1. 获取路径所有步骤及其关联资源
2. 找出无资源的步骤
3. 为每个空步骤匹配用户已有的同类资源
4. 若无匹配，提示用户生成新资源
5. 返回推荐列表: [{stepId, stepName, resource}]
```

### 4.4 智能辅导系统

```
┌──────────┐     ┌──────────────┐     ┌─────────────────┐
│ 学生提问  │────>│ TutorController│───>│ SmartTutorService│
│ (文字/图片)│     │ POST /tutor/ask│     │                  │
└──────────┘     └──────────────┘     │ 1. 加载 Prompt 模板│
                                      │ 2. 拼接画像上下文  │
                                      │ 3. 调用 ChatClient │
                                      │ 4. 保存问答记录    │
                                      └────────┬──────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │  Markdown 格式    │
                                      │  解答 + 知识点关联│
                                      └─────────────────┘
```

### 4.5 学习效果评估

#### 4.5.1 评估数据来源

```
评估数据采集:
  ├── 学习行为 (learning_behavior)
  │   ├── 行为类型: view/complete/quiz/review
  │   ├── 学习时长 (秒)
  │   └── 测试分数
  ├── 答题记录 (question_answer_record)
  │   ├── 用户答案 vs 正确答案
  │   ├── 是否正确
  │   └── 作答耗时
  ├── 路径完成 (learning_path_step)
  │   ├── 完成状态
  │   └── 完成时间
  └── 视频观看 (video_watch_progress)
      ├── 观看进度
      └── 观看次数
```

#### 4.5.2 评估报告结构

AI 生成的评估报告包含四大模块：

| 模块 | 字段 | 说明 |
|------|------|------|
| 学习概览 | `evaluateContent` | 总体学习情况概述、学习时长统计、活跃度分析 |
| 知识点掌握度 | `knowledgeMastery` | JSON 格式，各知识点百分制得分 |
| 薄弱点分析 | (嵌入 evaluateContent) | 具体薄弱知识点及原因分析 |
| 提升建议 | `improveSuggest` | 针对性改进方案、推荐学习资源 |

#### 4.5.3 定时自动评估

```
AssessmentScheduleTask:
  ├── 每日凌晨 2:00
  │   └── 查询7天内更新过画像的活跃用户
  │       └── 批量生成学习评估报告
  └── 每小时执行
      └── 同步最近生成的评估结果到学生画像
          (标记 updateScene="定时同步评估")
```

---

## 五、数据库设计

### 5.1 ER 关系图

```
┌──────────┐    1:N    ┌───────────────────┐    1:N    ┌───────────────────┐
│ sys_user │──────────>│ learning_resource  │<─────────│   ai_agent        │
│          │           │                   │           │                   │
│ id (PK)  │           │ id (PK)           │           │ id (PK)           │
│ username │           │ user_id (FK)      │           │ agent_name        │
│ password │           │ agent_id (FK)     │           │ agent_role        │
│ nick_name│           │ resource_type     │           │ prompt_template   │
│ major    │           │ resource_content  │           │ model             │
│ grade    │           │ resource_title    │           │ icon              │
└────┬─────┘           └───────────────────┘           └───────────────────┘
     │
     │ 1:1
     ▼
┌───────────────────┐
│ student_profile    │
│                   │
│ id (PK)           │
│ user_id (FK, UQ)  │
│ knowledge_base    │
│ cognitive_style   │
│ learning_goal     │
│ error_prone_points│  (JSON)
│ learning_pace     │
│ resource_preference│
│ learning_habits   │
└───────────────────┘

┌──────────┐    1:N    ┌─────────────────────┐    1:N    ┌──────────────────────┐
│ sys_user │──────────>│  learning_path       │──────────>│ learning_path_step   │
│          │           │                     │           │                      │
│          │           │ id (PK)             │           │ id (PK)              │
│          │           │ user_id (FK)        │           │ path_id (FK)         │
│          │           │ path_name           │           │ step_name            │
│          │           │ total_step          │           │ resource_ids (逗号分隔)│
│          │           │ current_step        │           │ sort                 │
│          │           │ status (doing/finish)│          │ finish_status        │
│          │           │ duration            │           └──────────┬───────────┘
│          │           └─────────────────────┘                     │
│          │                                                       │ N:M
│          │    1:N    ┌───────────────────────┐                   │
│          │──────────>│  learning_behavior     │     ┌─────────────▼────────────┐
│          │           │                       │     │ learning_path_step_resource│
│          │           │ id (PK)               │     │                          │
│          │           │ user_id (FK)          │     │ id (PK)                  │
│          │           │ resource_id (FK)      │     │ step_id (FK)             │
│          │           │ step_id (FK)          │     │ resource_id (FK)         │
│          │           │ behavior_type         │     │ sort                     │
│          │           │ duration              │     └──────────────────────────┘
│          │           │ score                 │
│          │           └───────────────────────┘
│          │
│          │    1:N    ┌───────────────────────┐
│          │──────────>│ question_answer_record │
│          │           │                       │
│          │           │ id (PK)               │
│          │           │ user_id (FK)          │
│          │           │ resource_id (FK)      │
│          │           │ question_id           │
│          │           │ user_answer           │
│          │           │ correct_answer        │
│          │           │ is_correct            │
│          │           │ spend_time            │
│          │           └───────────────────────┘
│          │
│          │    1:N    ┌───────────────────────┐
│          │──────────>│ learning_evaluate      │
│          │           │                       │
│          │           │ id (PK)               │
│          │           │ user_id (FK)          │
│          │           │ evaluate_content      │
│          │           │ improve_suggest       │
│          │           │ knowledge_mastery (JSON)│
│          │           └───────────────────────┘
│          │
│          │    1:N    ┌───────────────────────┐
│          │──────────>│ smart_tutor            │
│          │           │                       │
│          │           │ id (PK)               │
│          │           │ user_id (FK)          │
│          │           │ question              │
│          │           │ text_answer           │
│          │           │ session_id            │
│          │           └───────────────────────┘
└──────────┘
```

### 5.2 核心表结构

#### sys_user (用户表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO | 用户ID |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(100) | BCrypt 加密密码 |
| nick_name | VARCHAR(50) | 昵称 |
| major | VARCHAR(100) | 专业 |
| grade | VARCHAR(20) | 年级 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar | VARCHAR(500) | 头像URL |
| status | INT | 状态 (1启用/0禁用) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_deleted | INT DEFAULT 0 | 逻辑删除标记 |

#### student_profile (学生画像表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO | 画像ID |
| user_id | BIGINT UNIQUE FK | 用户ID |
| knowledge_base | TEXT | 知识基础 |
| cognitive_style | VARCHAR(100) | 认知风格 |
| learning_goal | TEXT | 学习目标 |
| error_prone_points | JSON | 易错点 (JSON数组) |
| learning_pace | VARCHAR(100) | 学习节奏 |
| resource_preference | VARCHAR(200) | 资源偏好 |
| learning_habits | TEXT | 学习习惯 |
| update_scene | VARCHAR(50) | 更新场景标记 |

#### ai_agent (智能体配置表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO | 智能体ID |
| agent_name | VARCHAR(100) | 智能体名称 |
| agent_role | VARCHAR(50) UNIQUE | 角色标识 (document/mind/question等) |
| agent_description | TEXT | 描述 |
| icon | VARCHAR(500) | 图标 |
| prompt_template | TEXT | Prompt 模板 |
| model | VARCHAR(100) | 使用的模型 |
| sort | INT | 排序 |
| status | INT | 状态 |

#### learning_resource (学习资源表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO | 资源ID |
| user_id | BIGINT FK | 所属用户 |
| agent_id | BIGINT FK | 生成智能体 |
| resource_type | VARCHAR(20) | 类型 (document/mind/question/case/video) |
| content_format | VARCHAR(20) | 内容格式 |
| resource_title | VARCHAR(200) | 标题 |
| resource_content | LONGTEXT | 内容 (Markdown/JSON) |
| metadata | JSON | 元数据 |
| file_url | VARCHAR(500) | 文件URL |
| task_id | VARCHAR(50) | 关联任务ID |
| version | INT DEFAULT 1 | 版本号 |
| course_name | VARCHAR(100) | 课程名称 |
| knowledge_point | VARCHAR(200) | 知识点 |
| difficulty | VARCHAR(20) | 难度 |

#### resource_generate_task (资源生成任务表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO | 任务ID |
| task_id | VARCHAR(50) UNIQUE | UUID 任务标识 |
| user_id | BIGINT FK | 用户ID |
| agent_id | BIGINT FK | 智能体ID |
| topic | VARCHAR(200) | 生成主题 |
| params | JSON | 生成参数 |
| status | VARCHAR(20) | pending/running/success/failed |
| progress | INT DEFAULT 0 | 进度 (0-100) |
| error_msg | TEXT | 错误信息 |
| resource_id | BIGINT FK | 生成的资源ID |

---

## 六、API 接口设计

### 6.1 接口总览

#### 用户认证模块 (/user)

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/user/register` | 用户注册 | 否 |
| POST | `/user/login` | 用户登录 (返回 JWT) | 否 |
| POST | `/user/logout` | 退出登录 (清除 Redis Token) | 是 |
| GET | `/user/info` | 获取当前用户信息 | 是 |
| PUT | `/user/info` | 更新用户信息 | 是 |
| PUT | `/user/password` | 修改密码 | 是 |

#### 学习画像模块 (/profile)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/profile/build/start` | 开始画像构建会话 |
| POST | `/profile/build/chat` | 画像对话 (多轮) |
| GET | `/profile/chat/history` | 获取对话历史 |
| GET | `/profile` | 获取当前画像 |
| PUT | `/profile` | 更新画像 |
| POST | `/profile/reset` | 重置画像 |

#### 资源生成模块 (/resource)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/resource/generate` | 异步生成资源 (返回 taskId) |
| GET | `/resource/generate/progress/{taskId}` | 查询生成进度 |
| GET | `/resource/list` | 资源列表 (分页+筛选) |
| GET | `/resource/detail/{id}` | 资源详情 |
| DELETE | `/resource/{id}` | 删除资源 |
| GET | `/resource/agents` | 获取所有可用智能体 |
| GET | `/resource/agents/{agentId}` | 获取智能体详情 |

#### 学习路径模块 (/learning-path)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/learning-path/generate` | AI 一键生成学习路径 |
| GET | `/learning-path/list` | 路径列表 |
| GET | `/learning-path/detail/{pathId}` | 路径详情+步骤 |
| POST | `/learning-path/step/{stepId}/complete` | 步骤打卡完成 |
| GET | `/learning-path/{pathId}/recommend` | 智能资源推荐 |
| PUT | `/learning-path/{pathId}/adjust` | 动态调整路径 |
| DELETE | `/learning-path/{pathId}` | 删除路径 |
| POST | `/learning-path/behavior` | 记录学习行为 |
| POST | `/learning-path/step/{stepId}/generate-resource` | 为步骤生成资源 |
| POST | `/learning-path/step/{stepId}/link-resource/{resourceId}` | 关联已有资源 |
| POST | `/learning-path/step/{stepId}/submit-quiz` | 提交题目答案 |

#### 智能辅导模块 (/tutor)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/tutor/ask` | 向 AI 辅导员提问 |
| GET | `/tutor/history` | 问答历史 (分页) |
| GET | `/tutor/detail/{recordId}` | 问答详情 |
| DELETE | `/tutor/{recordId}` | 删除记录 |

#### 学习评估模块 (/assessment)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/assessment/report` | 获取评估报告 |
| POST | `/assessment/report/generate` | AI 生成评估报告 |
| GET | `/assessment/result` | 评估结果详情 |
| GET | `/assessment/stats` | 学习统计数据 |
| GET | `/assessment/weak-points` | 薄弱点分析 |
| GET | `/assessment/trend` | 学习趋势 (7/30/90天) |

### 6.2 接口鉴权机制

```
请求流程:
  前端 → Authorization: Bearer <JWT Token>
      → JwtTokenUserInterceptor 拦截
      → 解析 JWT → 提取 userId
      → 存入 BaseContext (ThreadLocal)
      → Controller 通过 BaseContext.getCurrentId() 获取当前用户
      → 请求结束 → 清除 ThreadLocal

排除路径: /user/login, /user/register

Token 管理:
  ├── 登录时生成 JWT (有效期 24 小时)
  ├── Token 存入 Redis (支持主动失效)
  ├── 修改密码时清除旧 Token
  └── 退出登录时删除 Redis Token
```

---

## 七、前端界面与交互设计

### 7.1 页面路由设计

| 路由 | 页面 | 功能 |
|------|------|------|
| `/` | HomeView | 首页功能导航 |
| `/login` | LoginView | 登录/注册 (Tab 切换) |
| `/profile` | ProfileView | 学习画像查看/编辑 |
| `/profile/chat` | ProfileChatView | AI 对话式画像构建 |
| `/ai-resources` | ResourceListView | 已生成资源列表 |
| `/ai-resources/generate` | ResourceGenerateView | 选择智能体+生成资源 |
| `/ai-resources/:id` | ResourceDetailView | 资源详情 (Markdown渲染) |
| `/learning-paths` | PathListView | 学习路径列表 |
| `/learning-paths/:pathId` | PathDetailView | 路径详情+日历视图 |
| `/learning-paths/:pathId/steps` | PathStepView | 路径步骤详情 |
| `/learning-paths/:pathId/steps/:stepId/resources` | StepResourceView | 步骤关联资源 |
| `/tutor` | AiTutorView | AI 智能辅导对话 |
| `/assessment` | LearningOutcomeView | 学习成果总览 |
| `/assessment/report` | AssessmentReportView | 评估报告详情 |

### 7.2 核心交互流程

#### 流程一：新用户首次使用

```
注册 → 登录 → 首页引导
  → "构建学习画像" → AI 多轮对话收集信息
  → 画像构建完成 → 显示画像维度卡片
  → "开始学习" → AI 生成学习路径
  → 进入路径详情 → 按步骤学习
```

#### 流程二：日常学习流程

```
登录 → 首页
  ├── 查看学习路径进度 → 点击步骤 → 查看/生成资源
  ├── AI 生成新资源 → 选择智能体 → 输入主题 → 等待生成
  ├── 智能辅导 → 提问 → 获取 AI 解答
  └── 查看学习评估 → 了解薄弱点 → 获取提升建议
```

### 7.3 UI/UX 设计亮点

1. **渐变卡片设计**：首页采用紫蓝渐变背景，功能模块以毛玻璃效果卡片展示
2. **AI 对话体验**：ChatMessage 组件支持 Markdown 渲染，区分用户/AI 消息样式
3. **异步任务可视化**：资源生成时显示进度条，实时更新百分比
4. **资源卡片化展示**：不同类型资源使用差异化图标和颜色标签
5. **学习路径日历视图**：PathDetailView 通过 ECharts 展示学习进度趋势
6. **响应式布局**：所有页面适配不同屏幕尺寸

---

## 八、关键技术与创新点

### 8.1 多智能体策略工厂架构

**创新点**：采用 Strategy + Factory 设计模式，实现智能体的可插拔式架构。新增智能体只需：
1. 实现 `AgentGenerateStrategy` 接口
2. 添加 `@Component` 注解
3. 在 `ai_agent` 表中插入配置记录

无需修改任何已有代码，系统自动发现并注册新智能体。

### 8.2 对话式画像自主构建

**创新点**：摒弃传统表单填写，通过 AI 引导式多轮对话自然收集学生信息。系统在每轮对话中：
- 分析已有画像数据，智能跳过已收集维度
- 根据对话内容动态调整提问策略
- 对话结束时自动提取结构化 JSON 更新画像

### 8.3 异步任务管线 + 进度追踪

**创新点**：AI 资源生成采用异步任务队列，前端通过轮询实时追踪生成进度。任务状态机：
```
pending → running (progress: 10→100) → success/failed
```
避免 AI 长时间调用导致 HTTP 超时，提供流畅的用户体验。

### 8.4 Prompt 模板数据库化

**创新点**：智能体的 Prompt 模板存储在数据库 `ai_agent.prompt_template` 字段中，支持：
- 运行时动态调整 Prompt 而无需重启服务
- 为不同智能体配置不同的模型 (qwen-plus/deepseek-v3 等)
- 使用数据库模板优先，内置默认模板兜底的双层机制

### 8.5 多维度学习评估引擎

**创新点**：综合学习行为、答题记录、路径完成度等多维数据，通过 AI 生成个性化评估报告。支持：
- 定时自动评估 (每日凌晨批量)
- 实时手动触发评估
- 评估结果自动同步到学生画像
- 知识点掌握度 JSON 量化 (百分制)

### 8.6 动态学习路径调整

**创新点**：支持 5 种路径调整模式 (延长/压缩/重排/新增/移除)，AI 根据调整类型智能重规划未完成步骤，实现学习路径的持续优化。

---

## 九、部署与运行说明

### 9.1 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 20+ |
| pnpm | 9+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |

### 9.2 后端启动

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE MediCraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 导入表结构 (项目提供 SQL 文件)

# 3. 配置环境变量
export OPENAI_API_KEY=your_dashscope_api_key

# 4. 修改 application.yaml 中的数据库/Redis 连接信息

# 5. 编译启动
cd MediCraft/MediCraft
mvn clean package -DskipTests
java -jar educate-server/target/educate-server-*.jar
# 启动端口: 8080
```

### 9.3 前端启动

```bash
cd Media-edu/MediCraft-web

# 1. 安装依赖
pnpm install

# 2. 启动开发服务器
pnpm dev
# 默认端口: 5173，自动代理 /api → localhost:8080

# 3. 生产构建
pnpm build
# 输出到 dist/ 目录
```

### 9.4 配置说明

**后端 application.yaml 关键配置：**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/MediCraft
    username: root
    password: 1234

  data:
    redis:
      host: localhost
      port: 6379

ai:
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  api-key: ${OPENAI_API_KEY}
  model: qwen3.6-35b-a3b
```

**前端 vite.config.js 关键配置：**

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

---

## 附录：项目目录结构总览

```
CAL/
├── MediCraft/                          # 后端项目
│   └── MediCraft/
│       ├── pom.xml                     # 父 POM
│       ├── educate-pojo/               # 实体/DTO/VO 模块
│       │   └── src/main/java/com/education/
│       │       ├── entity/             # 14 个数据库实体
│       │       ├── dto/                # 数据传输对象
│       │       └── vo/                 # 视图对象
│       ├── educate-common/             # 公共模块
│       │   └── src/main/java/com/education/
│       │       ├── config/             # SecurityConfig
│       │       ├── context/            # BaseContext (ThreadLocal)
│       │       ├── interceptor/        # JWT 拦截器
│       │       ├── properties/         # JwtProperties
│       │       └── utils/              # JwtUtil
│       └── educate-server/             # 主服务模块
│           └── src/main/
│               ├── java/com/education/
│               │   ├── resource/       # 资源生成域 (6个智能体)
│               │   ├── path/           # 学习路径域
│               │   ├── outcome/        # 学习评估域
│               │   ├── tutor/          # 智能辅导域
│               │   └── user/           # 用户画像域
│               └── resources/
│                   └── application.yaml
├── Media-edu/                          # 前端项目
│   └── MediCraft-web/
│       ├── src/
│       │   ├── api/                    # 8 个 API 模块
│       │   ├── stores/                 # 3 个 Pinia Store
│       │   ├── views/                  # 15+ 页面视图
│       │   └── components/             # 通用组件
│       ├── vite.config.js
│       └── package.json
└── 项目说明文档.md                      # 本文档
```

---

*本文档基于 MediCraft 项目源码分析生成，涵盖系统架构设计、多智能体协同机制、核心功能模块、数据库设计、API 接口、前端交互等完整内容。*
