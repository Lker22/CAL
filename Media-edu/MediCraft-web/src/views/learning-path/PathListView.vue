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

const pathList = computed(() => {
  const raw = learningPathStore.paths
  if (Array.isArray(raw)) return raw
  if (raw && Array.isArray(raw.records)) return raw.records
  return []
})

// 统计数据
const stats = computed(() => {
  const list = pathList.value
  return {
    total: list.length,
    inProgress: list.filter(p => p.status === 'inProgress').length,
    completed: list.filter(p => p.status === 'completed').length,
    pending: list.filter(p => p.status === 'pending').length,
    totalSteps: list.reduce((sum, p) => sum + (p.totalSteps || p.totalStep || 0), 0),
    completedSteps: list.reduce((sum, p) => sum + (p.completedSteps || 0), 0)
  }
})

const getStatusConfig = (status) => {
  return PATH_STATUS[status] || PATH_STATUS.pending
}

const viewPath = (path) => {
  router.push(`/path/step/${path.id}`)
}

const handleDelete = async (path) => {
  const name = path.title || path.pathName || '该路径'
  try {
    await ElMessageBox.confirm(`确定要删除学习路径"${name}"吗？`, '删除确认', { type: 'warning' })
    await learningPathStore.deletePath(path.id)
    ElMessage.success('删除成功')
  } catch {}
}

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
    <!-- 顶部标题区 -->
    <div class="page-hero">
      <div class="hero-content">
        <h2>我的学习路径</h2>
        <p>管理和跟踪你的个性化学习路线，AI 为你规划每一步</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/path/generate')">
        + 生成新路径
      </el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stats-bar" v-if="pathList.length > 0">
      <div class="stat-card">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">全部路径</div>
      </div>
      <div class="stat-card accent-blue">
        <div class="stat-number">{{ stats.inProgress }}</div>
        <div class="stat-label">学习中</div>
      </div>
      <div class="stat-card accent-green">
        <div class="stat-number">{{ stats.completed }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card accent-orange">
        <div class="stat-number">{{ stats.totalSteps }}</div>
        <div class="stat-label">总步骤</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ stats.completedSteps }}</div>
        <div class="stat-label">已打卡</div>
      </div>
    </div>

    <!-- 路径卡片列表 -->
    <div v-loading="loading" class="path-list">
      <div
        v-for="path in pathList"
        :key="path.id"
        class="path-card"
        :class="path.status"
      >
        <div class="path-card-left">
          <div class="path-status-dot" :style="{ background: getStatusConfig(path.status).color }"></div>
        </div>
        <div class="path-card-main">
          <div class="path-header">
            <h4 class="path-title">{{ path.title || path.pathName }}</h4>
            <el-tag
              :color="getStatusConfig(path.status).color"
              effect="dark"
              size="small"
            >
              {{ getStatusConfig(path.status).label }}
            </el-tag>
          </div>

          <div class="path-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ path.duration || (path.totalStep + '个步骤') }}
            </span>
            <span class="meta-item">
              <el-icon><Finished /></el-icon>
              {{ path.completedSteps || 0 }}/{{ path.totalSteps || path.totalStep || 0 }} 步骤
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(path.createdAt || path.createTime, 'MM-DD') }}
            </span>
          </div>

          <div class="path-progress">
            <el-progress
              :percentage="path.progress || 0"
              :color="getStatusConfig(path.status).color"
              :stroke-width="8"
            />
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
              plain
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
            <el-button
              size="small"
              @click="$router.push(`/path/adjust`)"
            >
              调整路径
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(path)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && pathList.length === 0" class="empty-wrapper">
      <div class="empty-illustration">
        <div class="empty-icon">
          <el-icon :size="48"><Guide /></el-icon>
        </div>
        <h3>还没有学习路径</h3>
        <p>AI 将根据你的学习目标，为你规划个性化学习路线</p>
        <el-button type="primary" size="large" @click="$router.push('/path/generate')">
          生成第一条路径
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { Calendar, Finished, Clock, Guide } from '@element-plus/icons-vue'
export default { components: { Calendar, Finished, Clock, Guide } }
</script>

<style scoped>
.path-list-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 0;
}

/* 顶部标题区 */
.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  color: #fff;
}

.hero-content h2 {
  font-size: 24px;
  margin: 0 0 6px;
  color: #fff;
}

.hero-content p {
  font-size: 14px;
  margin: 0;
  opacity: 0.85;
}

.page-hero .el-button {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.4);
  color: #fff;
  backdrop-filter: blur(4px);
}

.page-hero .el-button:hover {
  background: rgba(255,255,255,0.35);
}

/* 统计概览 */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #1d1e2c;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.accent-blue .stat-number { color: #409eff; }
.accent-green .stat-number { color: #67c23a; }
.accent-orange .stat-number { color: #e6a23c; }

/* 路径卡片列表 */
.path-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
}

.path-card {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.path-card:hover {
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.path-card-left {
  display: flex;
  align-items: flex-start;
  padding-top: 6px;
}

.path-status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.path-card-main {
  flex: 1;
  min-width: 0;
}

.path-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.path-progress {
  margin-bottom: 14px;
}

.path-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 空状态 */
.empty-wrapper {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.empty-illustration {
  text-align: center;
}

.empty-icon {
  width: 96px;
  height: 96px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea20, #764ba220);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
}

.empty-illustration h3 {
  font-size: 18px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.empty-illustration p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 24px;
}
</style>
