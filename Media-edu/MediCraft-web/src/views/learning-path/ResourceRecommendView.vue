<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { RESOURCE_TYPES } from '@/utils/constants'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)

// 示例推荐资源
const recommendedResources = ref([
  {
    id: 1,
    title: 'Python高级编程技巧',
    type: 'document',
    matchScore: 95,
    reason: '根据你的学习进度推荐',
    pathName: 'Python全栈开发学习路径'
  },
  {
    id: 2,
    title: 'Flask框架实战思维导图',
    type: 'mindmap',
    matchScore: 92,
    reason: '当前学习步骤的相关资源',
    pathName: 'Python全栈开发学习路径'
  },
  {
    id: 3,
    title: '数据库设计练习题',
    type: 'quiz',
    matchScore: 88,
    reason: '针对即将到来的学习内容预热',
    pathName: 'Python全栈开发学习路径'
  },
  {
    id: 4,
    title: '全栈项目实操案例',
    type: 'practice',
    matchScore: 85,
    reason: '与你的学习目标高度匹配',
    pathName: 'Python全栈开发学习路径'
  }
])

// 获取资源类型配置
const getTypeConfig = (type) => {
  return RESOURCE_TYPES[type] || RESOURCE_TYPES.document
}

// 使用资源
const useResource = (resource) => {
  router.push(`/resource/detail/${resource.id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    await learningPathStore.getRecommendedResources(1)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="resource-recommend-page">
    <div class="page-header">
      <h2>智能资源推荐</h2>
      <p>AI根据你的学习路径和画像，为你推荐最合适的学习资源</p>
    </div>

    <div v-loading="loading" class="recommend-list">
      <div
        v-for="resource in recommendedResources"
        :key="resource.id"
        class="recommend-card"
      >
        <div class="resource-icon" :style="{ backgroundColor: getTypeConfig(resource.type).color + '15' }">
          <el-icon :size="28" :color="getTypeConfig(resource.type).color">
            <component :is="getTypeConfig(resource.type).icon" />
          </el-icon>
        </div>

        <div class="resource-info">
          <h4>{{ resource.title }}</h4>
          <div class="resource-meta">
            <el-tag size="small" :color="getTypeConfig(resource.type).color" effect="plain">
              {{ getTypeConfig(resource.type).label }}
            </el-tag>
            <span class="path-name">{{ resource.pathName }}</span>
          </div>
          <p class="recommend-reason">{{ resource.reason }}</p>
        </div>

        <div class="match-score">
          <div class="score-circle" :class="{ high: resource.matchScore >= 90 }">
            <span class="score-value">{{ resource.matchScore }}</span>
            <span class="score-label">匹配度</span>
          </div>
          <el-button type="primary" size="small" @click="useResource(resource)">
            立即使用
          </el-button>
        </div>
      </div>
    </div>

    <EmptyState v-if="!loading && recommendedResources.length === 0" description="暂无推荐资源" />
  </div>
</template>

<style scoped>
.resource-recommend-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.recommend-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.recommend-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.resource-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-info h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.path-name {
  font-size: 12px;
  color: #909399;
}

.recommend-reason {
  font-size: 13px;
  color: #606266;
  margin: 0;
}

.match-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.score-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid #e6a23c;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-circle.high {
  border-color: #67c23a;
}

.score-value {
  font-size: 18px;
  font-weight: 700;
  color: #1d1e2c;
  line-height: 1;
}

.score-label {
  font-size: 10px;
  color: #909399;
}
</style>
