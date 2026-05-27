# MediCraft-web - 新时代大学生 AI 学习辅助系统（前端）

## 项目简介

这是一个基于 Vue 3 + Element Plus 开发的前端项目，为后端 AI 学习辅助系统提供用户界面。

## 技术栈

- **框架**: Vue 3.5+
- **构建工具**: Vite 8+
- **UI 组件库**: Element Plus 2.9+
- **状态管理**: Pinia 3+
- **路由**: Vue Router 5+
- **HTTP 请求**: Axios
- **Markdown 渲染**: marked + DOMPurify

## 项目结构

```
src/
├── api/                    # API 接口封装
│   ├── request.js         # Axios 实例配置
│   ├── user.js            # 用户相关接口
│   ├── profile.js         # 学习画像接口
│   ├── resource.js        # 学习资源接口
│   ├── learningPath.js    # 学习路径接口
│   ├── tutor.js           # 智能辅导接口
│   ├── assessment.js      # 学习评估接口
│   ├── upload.js          # 文件上传接口
│   └── index.js           # 统一导出
├── components/             # 公共组件
│   ├── common/            # 通用组件
│   │   ├── LoadingOverlay.vue
│   │   ├── EmptyState.vue
│   │   └── ProgressTracker.vue
│   ├── upload/            # 上传组件
│   │   └── FileUpload.vue
│   ├── markdown/          # Markdown 组件
│   │   ├── MarkdownRenderer.vue
│   │   └── StreamText.vue
│   └── layout/            # 布局组件
│       └── Layout.vue
├── router/                 # 路由配置
│   └── index.js
├── stores/                 # Pinia 状态管理
│   ├── user.js            # 用户状态
│   ├── profile.js         # 画像状态
│   ├── resource.js        # 资源状态
│   ├── learningPath.js    # 学习路径状态
│   ├── tutor.js           # 辅导状态
│   └── assessment.js      # 评估状态
├── utils/                  # 工具函数
│   ├── format.js          # 格式化函数
│   ├── stream.js          # 流式处理
│   ├── validator.js       # 表单验证
│   ├── upload.js          # 上传工具
│   ├── constants.js       # 常量配置
│   └── index.js
├── views/                  # 页面组件
│   ├── user/              # 用户模块
│   │   ├── LoginView.vue
│   │   ├── RegisterView.vue
│   │   ├── ProfileBuildView.vue
│   │   ├── ProfileDisplayView.vue
│   │   ├── ProfileQueryView.vue
│   │   └── UserInfoView.vue
│   ├── ai-resource/       # AI 资源模块
│   │   ├── AgentListView.vue
│   │   ├── ResourceGenerateView.vue
│   │   ├── GenerationProgressView.vue
│   │   ├── ResourceListView.vue
│   │   └── ResourceDetailView.vue
│   ├── learning-path/     # 学习路径模块
│   │   ├── PathGenerateView.vue
│   │   ├── PathListView.vue
│   │   ├── PathStepView.vue
│   │   ├── ResourceRecommendView.vue
│   │   └── PathAdjustView.vue
│   ├── intelligent-tutor/ # 智能辅导模块
│   │   ├── TutorQuestionView.vue
│   │   ├── TutorAnswerView.vue
│   │   └── TutorHistoryView.vue
│   └── learning-assessment/ # 学习评估模块
│       ├── AssessmentReportView.vue
│       ├── AssessmentResultView.vue
│       └── LearningStatsView.vue
├── App.vue
├── main.js
└── assets/
```

## 功能模块

### 1. 用户与学习画像模块
- 用户登录/注册
- 对话式画像构建
- 画像展示与查询
- 用户信息管理

### 2. AI 多智能体资源生成模块
- 智能体选择
- 个性化资源生成
- 生成进度追踪
- 学习资源管理

### 3. 学习路径规划与智能推送模块
- 学习路径生成
- 学习步骤管理
- 智能资源推荐
- 路径动态调整

### 4. 智能辅导模块
- 多模态提问
- AI 解答展示
- 答疑历史记录

### 5. 学习效果评估模块
- 评估报告生成
- 评估结果展示
- 学习数据统计

## 安装与运行

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## API 配置

在 `.env` 文件中配置 API 地址：
```
VITE_API_BASE_URL=/api
```

在 `vite.config.js` 中配置代理：
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 后端接口对接

前端 API 接口封装在 `src/api/` 目录下，所有接口都已按照后端 Spring AI + 消息队列的架构进行适配：

- 异步任务接口返回任务 ID，前端轮询进度
- 流式输出接口使用 SSE（Server-Sent Events）
- 文件上传支持 Base64 和服务器上传两种模式

## 开发规范

- 使用 ES Module 语法
- 组件采用 `<script setup>` 语法糖
- 统一使用 Composition API
- 状态管理使用 Pinia
- 代码注释清晰

## 注意事项

1. 开发阶段使用 Mock 数据，实际运行需对接后端 API
2. 部分接口使用流式输出，需确保后端支持 SSE
3. 文件上传功能需配置后端接收接口
4. Markdown 渲染已配置 XSS 过滤，确保内容安全
