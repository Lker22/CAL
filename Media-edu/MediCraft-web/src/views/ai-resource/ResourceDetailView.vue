<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import { RESOURCE_TYPES } from '@/utils/constants'
import { formatDate, formatFileSize } from '@/utils/format'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const resourceStore = useResourceStore()

const loading = ref(false)
const activeTab = ref('preview')

// 示例资源详情
const resource = ref({
  id: route.params.id,
  title: '深度学习基础入门文档',
  type: 'document',
  agent: '文档生成智能体',
  createdAt: '2024-01-15 10:30',
  size: 2048576,
  status: 'completed',
  content: `# 深度学习基础入门

## 1. 什么是深度学习？

深度学习是机器学习的一个分支，它使用多层神经网络来学习数据的分层表示。

### 1.1 核心概念

- **神经网络**：由大量互连的节点（神经元）组成
- **前向传播**：数据从输入层到输出层的流动
- **反向传播**：误差从输出层向输入层的反向传递
- **梯度下降**：优化算法，用于最小化损失函数

## 2. 深度学习的应用

| 领域 | 应用示例 |
|------|----------|
| 计算机视觉 | 图像分类、目标检测 |
| 自然语言处理 | 机器翻译、文本生成 |
| 语音识别 | 语音助手、实时翻译 |
| 推荐系统 | 个性化推荐 |

## 3. 常用框架

\`\`\`python
# PyTorch 示例
import torch
import torch.nn as nn

class SimpleNet(nn.Module):
    def __init__(self):
        super().__init__()
        self.fc1 = nn.Linear(784, 128)
        self.fc2 = nn.Linear(128, 10)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        return self.fc2(x)
\`\`\`

> 提示：学习深度学习需要掌握线性代数、微积分和概率论的基础知识。
`
})

// 获取资源类型配置
const typeConfig = computed(() => {
  return RESOURCE_TYPES[resource.value.type] || RESOURCE_TYPES.document
})

// 加载资源详情
const loadResource = async () => {
  loading.value = true
  try {
    // 实际项目中调用API
    // await resourceStore.getResourceDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

// 下载资源
const handleDownload = () => {
  ElMessage.success('开始下载...')
}

// 返回列表
const goBack = () => {
  router.push('/resource/list')
}

onMounted(() => {
  loadResource()
})
</script>

<template>
  <div class="resource-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div v-loading="loading" class="resource-container">
      <!-- 资源信息 -->
      <div class="resource-header">
        <div class="resource-icon" :style="{ backgroundColor: typeConfig.color + '15' }">
          <el-icon :size="32" :color="typeConfig.color">
            <component :is="typeConfig.icon" />
          </el-icon>
        </div>
        <div class="resource-info">
          <h2>{{ resource.title }}</h2>
          <div class="meta-info">
            <el-tag size="small" :color="typeConfig.color" effect="dark">
              {{ typeConfig.label }}
            </el-tag>
            <span>{{ resource.agent }}</span>
            <span>{{ formatDate(resource.createdAt) }}</span>
            <span>{{ formatFileSize(resource.size) }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="handleDownload">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="resource-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="内容预览" name="preview">
            <div class="preview-area">
              <!-- 文档类型：显示Markdown -->
              <MarkdownRenderer v-if="resource.type === 'document'" :content="resource.content" />

              <!-- 思维导图类型：显示图片 -->
              <div v-else-if="resource.type === 'mindmap'" class="mindmap-preview">
                <el-image
                  src="https://via.placeholder.com/800x600?text=Mind+Map"
                  fit="contain"
                  :preview-src-list="['https://via.placeholder.com/800x600?text=Mind+Map']"
                />
              </div>

              <!-- 题库类型：显示题目 -->
              <div v-else-if="resource.type === 'quiz'" class="quiz-preview">
                <div class="quiz-item">
                  <h4>1. 下列哪个不是深度学习框架？</h4>
                  <ul>
                    <li>A. TensorFlow</li>
                    <li>B. PyTorch</li>
                    <li>C. Scikit-learn</li>
                    <li>D. Keras</li>
                  </ul>
                  <p class="answer">答案：C</p>
                </div>
              </div>

              <!-- 其他类型 -->
              <div v-else class="other-preview">
                <el-empty description="该资源类型暂不支持预览" />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="详细信息" name="info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="资源名称">{{ resource.title }}</el-descriptions-item>
              <el-descriptions-item label="资源类型">{{ typeConfig.label }}</el-descriptions-item>
              <el-descriptions-item label="生成智能体">{{ resource.agent }}</el-descriptions-item>
              <el-descriptions-item label="生成时间">{{ formatDate(resource.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(resource.size) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag type="success">已完成</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowLeft, Download } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, Download }
}
</script>

<style scoped>
.resource-detail-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;
}

.resource-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.resource-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.resource-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-info h2 {
  font-size: 20px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}

.header-actions {
  flex-shrink: 0;
}

.resource-content {
  padding: 24px;
}

.preview-area {
  min-height: 400px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
}

.mindmap-preview {
  text-align: center;
}

.quiz-item {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.quiz-item h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.quiz-item ul {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
}

.quiz-item li {
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.quiz-item li:hover {
  background: #f5f7fa;
}

.answer {
  font-size: 14px;
  color: #67c23a;
  font-weight: 600;
  margin: 0;
}
</style>
