/**
 * 全局常量配置
 */

// 画像6大维度
export const PROFILE_DIMENSIONS = {
  knowledgeBase: {
    key: 'knowledgeBase',
    label: '知识基础',
    icon: 'Reading',
    color: '#409EFF',
    description: '当前掌握的知识水平和学科基础'
  },
  cognitiveStyle: {
    key: 'cognitiveStyle',
    label: '认知风格',
    icon: 'View',
    color: '#67C23A',
    description: '学习方式偏好和认知处理特点'
  },
  learningGoals: {
    key: 'learningGoals',
    label: '学习目标',
    icon: 'Aim',
    color: '#E6A23C',
    description: '短期和长期的学习目标与期望'
  },
  errorPoints: {
    key: 'errorPoints',
    label: '易错点',
    icon: 'Warning',
    color: '#F56C6C',
    description: '常见错误类型和薄弱知识点'
  },
  learningRhythm: {
    key: 'learningRhythm',
    label: '学习节奏',
    icon: 'Timer',
    color: '#909399',
    description: '学习时间安排和最佳学习状态'
  },
  resourcePreference: {
    key: 'resourcePreference',
    label: '资源偏好',
    icon: 'Collection',
    color: '#B37FEB',
    description: '对不同类型学习资源的偏好'
  }
}

// 智能体角色列表
export const AI_AGENTS = [
  {
    id: 'requirement',
    name: '需求解析智能体',
    icon: 'Search',
    color: '#409EFF',
    description: '分析学习需求，拆解学习目标，生成个性化学习方案'
  },
  {
    id: 'document',
    name: '文档生成智能体',
    icon: 'Document',
    color: '#67C23A',
    description: '根据学习需求生成结构化的学习文档和笔记'
  },
  {
    id: 'mindmap',
    name: '思维导图智能体',
    icon: 'Share',
    color: '#E6A23C',
    description: '将知识点以思维导图形式呈现，帮助建立知识体系'
  },
  {
    id: 'quiz',
    name: '题库智能体',
    icon: 'EditPen',
    color: '#F56C6C',
    description: '根据知识点生成针对性练习题和测试卷'
  },
  {
    id: 'practice',
    name: '实操案例智能体',
    icon: 'SetUp',
    color: '#909399',
    description: '生成实操案例和项目实践任务'
  },
  {
    id: 'multimodal',
    name: '多模态智能体',
    icon: 'VideoCamera',
    color: '#B37FEB',
    description: '生成视频脚本、音频讲解等多模态学习资源'
  }
]

// 资源类型（key 必须和后端 LearningResource.resourceType 一致）
export const RESOURCE_TYPES = {
  document: { key: 'document', label: '文档', icon: 'Document', color: '#67C23A' },
  mind: { key: 'mind', label: '思维导图', icon: 'Share', color: '#E6A23C' },
  question: { key: 'question', label: '题库', icon: 'EditPen', color: '#F56C6C' },
  case: { key: 'case', label: '实操案例', icon: 'SetUp', color: '#909399' }
}

// 学习路径状态
export const PATH_STATUS = {
  pending: { key: 'pending', label: '待开始', color: '#909399' },
  inProgress: { key: 'inProgress', label: '学习中', color: '#409EFF' },
  completed: { key: 'completed', label: '已完成', color: '#67C23A' },
  paused: { key: 'paused', label: '已暂停', color: '#E6A23C' }
}

// 学习步骤状态
export const STEP_STATUS = {
  pending: { key: 'pending', label: '待学习', color: '#909399' },
  inProgress: { key: 'inProgress', label: '学习中', color: '#409EFF' },
  completed: { key: 'completed', label: '已完成', color: '#67C23A' }
}

// 资源生成任务状态（与后端 ResourceGenerateTask.status 对齐）
export const GENERATION_STATUS = {
  pending: { key: 'pending', label: '排队中', color: '#909399' },
  running: { key: 'running', label: '生成中', color: '#409EFF' },
  processing: { key: 'processing', label: '生成中', color: '#409EFF' },
  success: { key: 'success', label: '已完成', color: '#67C23A' },
  completed: { key: 'completed', label: '已完成', color: '#67C23A' },
  failed: { key: 'failed', label: '失败', color: '#F56C6C' }
}
