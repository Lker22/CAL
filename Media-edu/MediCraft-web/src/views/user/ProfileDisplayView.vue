<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProfileStore } from '@/stores/profile'
import { ElMessage } from 'element-plus'
import { PROFILE_DIMENSIONS } from '@/utils/constants'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'

const router = useRouter()
const profileStore = useProfileStore()

const loading = ref(false)

// 格式化值：处理 JSON 数组/对象 → 可读文本
const formatValue = (val) => {
  if (!val) return null
  if (typeof val !== 'string') return val
  try {
    const parsed = JSON.parse(val)
    if (Array.isArray(parsed)) return parsed.join('；')
    if (typeof parsed === 'object') return JSON.stringify(parsed, null, 2)
    return parsed
  } catch {
    return val
  }
}

// 画像维度数据
const dimensionData = computed(() => {
  const profile = profileStore.profile
  if (!profile) return []

  // 后端 StudentProfile 实体字段名 → 前端常量字段名映射
  const fieldMap = {
    knowledgeBase: 'knowledgeBase',
    cognitiveStyle: 'cognitiveStyle',
    learningGoals: 'learningGoal',
    errorPoints: 'errorPronePoints',
    learningRhythm: 'learningPace',
    resourcePreference: 'resourcePreference'
  }

  return Object.entries(PROFILE_DIMENSIONS).map(([key, config]) => {
    const backendKey = fieldMap[key] || key
    const raw = profile[key] || profile[backendKey] || null
    return {
      ...config,
      value: formatValue(raw),
      completed: !!raw
    }
  })
})

// 画像完成度
const completionRate = computed(() => {
  const completed = dimensionData.value.filter((d) => d.completed).length
  return Math.round((completed / dimensionData.value.length) * 100)
})

// 加载画像数据
const loadProfile = async () => {
  loading.value = true
  try {
    await profileStore.getProfile()
  } catch (error) {
    // 画像加载失败，保持 null，页面会显示"暂无数据"
    console.warn('画像加载失败:', error.message)
  } finally {
    loading.value = false
  }
}

// 跳转到画像构建
const goToBuild = () => {
  router.push('/profile/build')
}

// 跳转到画像查询
const goToQuery = () => {
  router.push('/profile/query')
}

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="profile-display-page">
    <LoadingOverlay :visible="loading" text="加载画像中..." />

    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>我的学习画像</h2>
        <p>基于AI对话分析生成的个性化学习画像</p>
      </div>
      <div class="header-right">
        <el-button type="primary" plain @click="goToBuild">更新画像</el-button>
        <el-button type="primary" @click="goToQuery">画像详情</el-button>
      </div>
    </div>

    <!-- 完成度卡片 -->
    <div class="completion-card">
      <div class="completion-info">
        <h3>画像完成度</h3>
        <p>完善画像有助于获得更精准的个性化推荐</p>
      </div>
      <div class="completion-progress">
        <el-progress
          type="circle"
          :percentage="completionRate"
          :width="80"
          :stroke-width="8"
          :color="() => completionRate === 100 ? '#67C23A' : '#409EFF'"
        />
      </div>
    </div>

    <!-- 画像维度卡片 -->
    <div class="dimensions-grid">
      <div
        v-for="dim in dimensionData"
        :key="dim.key"
        class="dimension-card"
      >
        <div class="dim-header">
          <div class="dim-icon" :style="{ backgroundColor: dim.color + '15' }">
            <el-icon :size="24" :color="dim.color">
              <component :is="dim.icon" />
            </el-icon>
          </div>
          <div class="dim-title-area">
            <h4 class="dim-title">{{ dim.label }}</h4>
            <el-tag
              :type="dim.completed ? 'success' : 'info'"
              size="small"
              effect="plain"
            >
              {{ dim.completed ? '已完成' : '待完善' }}
            </el-tag>
          </div>
        </div>
        <p class="dim-desc">{{ dim.description }}</p>
        <div class="dim-content">
          <p v-if="dim.value" class="dim-value">{{ dim.value }}</p>
          <div v-else class="dim-empty">
            <span>暂无数据，请进行画像构建</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div v-if="completionRate < 100" class="action-card">
      <div class="action-content">
        <h3>完善你的学习画像</h3>
        <p>通过对话让AI更了解你，获得更精准的学习推荐</p>
      </div>
      <el-button type="primary" size="large" @click="goToBuild">
        开始对话构建
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.profile-display-page {
  position: relative;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.header-left p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 8px;
}

.completion-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4ff 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}

.completion-info h3 {
  font-size: 18px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.completion-info p {
  font-size: 13px;
  color: #606266;
  margin: 0;
}

.dimensions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.dimension-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.dimension-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.dim-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.dim-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dim-title-area {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dim-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1e2c;
  margin: 0;
}

.dim-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px;
}

.dim-content {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  min-height: 60px;
}

.dim-value {
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.dim-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #c0c4cc;
  font-size: 13px;
}

.action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #1d1e2c 0%, #2d2e42 100%);
  border-radius: 12px;
  color: #fff;
}

.action-content h3 {
  font-size: 18px;
  margin: 0 0 4px;
}

.action-content p {
  font-size: 13px;
  color: #a0a4b8;
  margin: 0;
}

@media (max-width: 1200px) {
  .dimensions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
