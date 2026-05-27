<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTutorStore } from '@/stores/tutor'
import { formatRelativeTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const tutorStore = useTutorStore()

const loading = ref(false)

// 示例历史记录
const mockHistory = ref([
  {
    id: 1,
    question: '如何理解神经网络的反向传播算法？',
    answerPreview: '反向传播是训练神经网络的核心算法，通过计算损失函数对每个参数的梯度...',
    createdAt: '2024-01-15 14:30',
    type: 'text'
  },
  {
    id: 2,
    question: 'Python中列表和元组有什么区别？',
    answerPreview: '列表是可变的，元组是不可变的。列表使用方括号[]，元组使用圆括号()...',
    createdAt: '2024-01-14 10:15',
    type: 'text'
  },
  {
    id: 3,
    question: '请分析这张数据库设计图',
    answerPreview: '从图中可以看到，这是一个电商系统的数据库设计，包含用户表、订单表、商品表...',
    createdAt: '2024-01-13 16:45',
    type: 'image'
  },
  {
    id: 4,
    question: '什么是HTTP协议的工作原理？',
    answerPreview: 'HTTP协议采用请求-响应模型，基于TCP/IP协议，默认端口80...',
    createdAt: '2024-01-12 09:20',
    type: 'text'
  }
])

// 查看详情
const viewDetail = (record) => {
  router.push('/tutor/answer')
}

// 删除记录
const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条答疑记录吗？', '删除确认', {
      type: 'warning'
    })
    mockHistory.value = mockHistory.value.filter((r) => r.id !== record.id)
    ElMessage.success('删除成功')
  } catch {
    // 取消
  }
}

// 继续提问
const askMore = () => {
  router.push('/tutor/question')
}

onMounted(async () => {
  loading.value = true
  try {
    await tutorStore.getHistory()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="tutor-history-page">
    <div class="page-header">
      <h2>答疑历史记录</h2>
      <p>查看和管理你的历史提问与AI解答</p>
      <el-button type="primary" @click="askMore">继续提问</el-button>
    </div>

    <div v-loading="loading" class="history-list">
      <div
        v-for="record in mockHistory"
        :key="record.id"
        class="history-card"
        @click="viewDetail(record)"
      >
        <div class="history-header">
          <div class="question-area">
            <el-tag v-if="record.type === 'image'" size="small" type="warning">图片提问</el-tag>
            <h4>{{ record.question }}</h4>
          </div>
          <span class="time">{{ formatRelativeTime(record.createdAt) }}</span>
        </div>

        <p class="answer-preview">{{ record.answerPreview }}</p>

        <div class="history-actions" @click.stop>
          <el-button type="primary" text size="small" @click="viewDetail(record)">
            查看详情
          </el-button>
          <el-button type="danger" text size="small" @click="handleDelete(record)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <EmptyState v-if="!loading && mockHistory.length === 0" description="暂无答疑记录">
      <el-button type="primary" @click="askMore">开始提问</el-button>
    </EmptyState>
  </div>
</template>

<style scoped>
.tutor-history-page {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0;
}

.page-header p {
  flex: 1;
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.history-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #409eff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.question-area {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.question-area h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.answer-preview {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
