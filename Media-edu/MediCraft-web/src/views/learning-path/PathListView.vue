<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { PATH_STATUS } from '@/utils/constants'
import { formatDate } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)

// 示例路径数据
const mockPaths = ref([
  {
    id: 1,
    title: 'Python全栈开发学习路径',
    status: 'inProgress',
    progress: 65,
    totalSteps: 12,
    completedSteps: 8,
    createdAt: '2024-01-10 09:00',
    duration: '2个月'
  },
  {
    id: 2,
    title: '深度学习入门到实践',
    status: 'pending',
    progress: 0,
    totalSteps: 10,
    completedSteps: 0,
    createdAt: '2024-01-15 14:30',
    duration: '1个月'
  },
  {
    id: 3,
    title: '数据结构与算法',
    status: 'completed',
    progress: 100,
    totalSteps: 15,
    completedSteps: 15,
    createdAt: '2023-12-01 10:00',
    duration: '3个月'
  }
])

// 获取状态配置
const getStatusConfig = (status) => {
  return PATH_STATUS[status] || PATH_STATUS.pending
}

// 查看路径详情
const viewPath = (path) => {
  router.push(`/path/step/${path.id}`)
}

// 删除路径
const handleDelete = async (path) => {
  try {
    await ElMessageBox.confirm(`确定要删除学习路径"${path.title}"吗？`, '删除确认', {
      type: 'warning'
    })
    mockPaths.value = mockPaths.value.filter((p) => p.id !== path.id)
    ElMessage.success('删除成功')
  } catch {
    // 取消
  }
}

// 继续学习
const continueLearning = (path) => {
  router.push(`/path/step/${path.id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    await learningPathStore.getPaths()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="path-list-page">
    <div class="page-header">
      <h2>我的学习路径</h2>
      <p>管理和跟踪你的个性化学习路线</p>
      <el-button type="primary" @click="$router.push('/path/generate')">
        生成新路径
      </el-button>
    </div>

    <div v-loading="loading" class="path-list">
      <div
        v-for="path in mockPaths"
        :key="path.id"
        class="path-card"
      >
        <div class="path-header">
          <h4 class="path-title">{{ path.title }}</h4>
          <el-tag
            :color="getStatusConfig(path.status).color"
            effect="dark"
            size="small"
          >
            {{ getStatusConfig(path.status).label }}
          </el-tag>
        </div>

        <div class="path-meta">
          <span>周期：{{ path.duration }}</span>
          <span>步骤：{{ path.completedSteps }}/{{ path.totalSteps }}</span>
          <span>创建：{{ formatDate(path.createdAt, 'MM-DD') }}</span>
        </div>

        <div class="path-progress">
          <el-progress
            :percentage="path.progress"
            :color="getStatusConfig(path.status).color"
            :show-text="false"
          />
          <span class="progress-text">{{ path.progress }}%</span>
        </div>

        <div class="path-actions">
          <el-button
            v-if="path.status === 'inProgress'"
            type="primary"
            size="small"
            @click="continueLearning(path)"
          >
            继续学习
          </el-button>
          <el-button
            v-else-if="path.status === 'pending'"
            type="primary"
            size="small"
            @click="viewPath(path)"
          >
            开始学习
          </el-button>
          <el-button
            v-else
            size="small"
            @click="viewPath(path)"
          >
            查看详情
          </el-button>
          <el-button type="danger" text size="small" @click="handleDelete(path)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <EmptyState v-if="!loading && mockPaths.length === 0" description="暂无学习路径">
      <el-button type="primary" @click="$router.push('/path/generate')">
        生成第一条路径
      </el-button>
    </EmptyState>
  </div>
</template>

<style scoped>
.path-list-page {
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

.path-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.path-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.path-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.path-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.path-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1e2c;
  margin: 0;
}

.path-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.path-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.path-progress :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  min-width: 40px;
}

.path-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
