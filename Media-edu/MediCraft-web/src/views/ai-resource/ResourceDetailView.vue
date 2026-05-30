<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import { resourceApi } from '@/api/resource'
import { RESOURCE_TYPES } from '@/utils/constants'
import { formatDate, formatFileSize } from '@/utils/format'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const resourceStore = useResourceStore()

const loading = ref(false)
const activeTab = ref('preview')

// 资源详情（从API加载）
const resource = ref({
  id: route.params.id,
  resourceTitle: '',
  resourceType: 'document',
  resourceContent: '',
  createTime: '',
  difficulty: '',
  knowledgePoint: '',
  status: 1
})

// 题库分页
const quizPage = ref(1)
const quizPageSize = 1

// 解析题库内容为题目数组
const quizList = computed(() => {
  if (resource.value.resourceType !== 'question') return []
  const content = resource.value.resourceContent
  if (!content) return []

  try {
    // 尝试直接解析 JSON
    let arr = JSON.parse(content)
    if (Array.isArray(arr)) return arr
    return [arr]
  } catch {
    // 尝试提取 markdown 代码块中的 JSON
    const match = content.match(/```(?:json)?\s*([\s\S]*?)```/)
    if (match) {
      try {
        let arr = JSON.parse(match[1].trim())
        if (Array.isArray(arr)) return arr
        return [arr]
      } catch { /* fall through */ }
    }
    // 不是 JSON 格式，作为纯文本题目处理（按编号分割）
    const lines = content.split('\n').filter(l => l.trim())
    const questions = []
    let current = null
    for (const line of lines) {
      const qMatch = line.match(/^\s*(\d+)[.、．]\s*(.+)/)
      if (qMatch) {
        if (current) questions.push(current)
        current = { id: questions.length + 1, title: qMatch[2], options: [], answer: '', analysis: '' }
      } else if (current) {
        const optMatch = line.match(/^\s*([A-Da-d])[.、．:\s]\s*(.+)/)
        if (optMatch) {
          current.options.push(line.trim())
        } else if (line.includes('答案') || line.includes('Answer')) {
          current.answer = line.trim()
        } else if (line.includes('解析') || line.includes('分析')) {
          current.analysis = line.trim()
        } else {
          current.title += '\n' + line
        }
      }
    }
    if (current) questions.push(current)
    return questions.length > 0 ? questions : [{ id: 1, title: content, options: [], answer: '', analysis: '' }]
  }
})

// 当前显示的题目
const currentQuiz = computed(() => {
  if (quizList.value.length === 0) return null
  return quizList.value[quizPage.value - 1] || quizList.value[0]
})

// 获取资源类型配置
const typeConfig = computed(() => {
  return RESOURCE_TYPES[resource.value.resourceType] || RESOURCE_TYPES.document
})

// 加载资源详情
const loadResource = async () => {
  loading.value = true
  try {
    await resourceStore.getResourceDetail(route.params.id)
    if (resourceStore.currentResource) {
      resource.value = resourceStore.currentResource
    }
  } catch (error) {
    ElMessage.error('加载资源详情失败')
  } finally {
    loading.value = false
  }
}

// 下载资源
const handleDownload = async () => {
  try {
    await resourceApi.downloadResource(route.params.id)
    ElMessage.success('开始下载...')
  } catch {
    ElMessage.error('下载失败')
  }
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
          <h2>{{ resource.resourceTitle }}</h2>
          <div class="meta-info">
            <el-tag size="small" :color="typeConfig.color" effect="dark">
              {{ typeConfig.label }}
            </el-tag>
            <span>{{ resource.difficulty || '标准' }}</span>
            <span>{{ formatDate(resource.createTime) }}</span>
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
              <!-- 文档/实操案例/思维导图类型：显示Markdown -->
              <MarkdownRenderer v-if="resource.resourceType === 'document' || resource.resourceType === 'case' || resource.resourceType === 'mind'" :content="resource.resourceContent" />

              <!-- 题库类型：解析JSON/文本，支持翻页 -->
              <div v-else-if="resource.resourceType === 'question'" class="quiz-preview">
                <div v-if="currentQuiz" class="quiz-item">
                  <h4>{{ quizPage }}. {{ currentQuiz.title || currentQuiz.question }}</h4>
                  <!-- 选项 -->
                  <ul v-if="currentQuiz.options && currentQuiz.options.length">
                    <li v-for="(opt, oi) in currentQuiz.options" :key="oi">{{ opt }}</li>
                  </ul>
                  <!-- 答案 -->
                  <p v-if="currentQuiz.answer" class="answer">答案：{{ currentQuiz.answer }}</p>
                  <!-- 解析 -->
                  <div v-if="currentQuiz.analysis" class="analysis">
                    <p>{{ currentQuiz.analysis }}</p>
                  </div>
                </div>
                <div v-else class="quiz-item">
                  <!-- 内容不是标准JSON格式，直接显示原文 -->
                  <MarkdownRenderer :content="resource.resourceContent" />
                </div>
                <!-- 翻页 -->
                <div v-if="quizList.length > 1" class="quiz-pagination">
                  <el-pagination
                    v-model:current-page="quizPage"
                    :page-size="1"
                    :total="quizList.length"
                    layout="prev, pager, next, total"
                    :pager-count="7"
                    small
                  />
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
              <el-descriptions-item label="资源名称">{{ resource.resourceTitle }}</el-descriptions-item>
              <el-descriptions-item label="资源类型">{{ typeConfig.label }}</el-descriptions-item>
              <el-descriptions-item label="难度">{{ resource.difficulty || '标准' }}</el-descriptions-item>
              <el-descriptions-item label="生成时间">{{ formatDate(resource.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="知识点">{{ resource.knowledgePoint || '-' }}</el-descriptions-item>
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

.analysis {
  margin-top: 12px;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.analysis p {
  font-size: 13px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.quiz-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}
</style>
