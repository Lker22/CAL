<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { RESOURCE_TYPES } from '@/utils/constants'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)
const selectedPathId = ref('')

// 从store获取路径列表
const pathList = computed(() => {
  const raw = learningPathStore.paths
  if (Array.isArray(raw)) return raw
  if (raw && Array.isArray(raw.records)) return raw.records
  return []
})

// 从store获取推荐资源
const recommendedResources = computed(() => learningPathStore.recommendedResources || [])

// 资源类型配置(本地定义，防止constants未导出时崩溃)
const typeConfig = {
  document: { key: 'document', label: '文档', icon: 'Document', color: '#67C23A' },
  mindmap: { key: 'mindmap', label: '思维导图', icon: 'Share', color: '#E6A23C' },
  quiz: { key: 'quiz', label: '题库', icon: 'EditPen', color: '#F56C6C' },
  practice: { key: 'practice', label: '实操案例', icon: 'SetUp', color: '#909399' },
  videoScript: { key: 'videoScript', label: '视频脚本', icon: 'VideoCamera', color: '#B37FEB' }
}

const getTypeConfig = (type) => {
  return RESOURCE_TYPES?.[type] || typeConfig[type] || typeConfig.document
}

// 选中路径后加载推荐
watch(selectedPathId, async (newId) => {
  if (newId) {
    await loadRecommendations(newId)
  }
})

async function loadRecommendations(pathId) {
  loading.value = true
  try {
    await learningPathStore.getRecommendedResources(pathId)
  } finally {
    loading.value = false
  }
}

const useResource = (resource) => {
  router.push(`/resource/detail/${resource.id}`)
}

onMounted(async () => {
  // 确保路径列表已加载
  if (pathList.value.length === 0) {
    await learningPathStore.getPaths()
  }

  // 优先使用URL参数中的pathId
  const queryPathId = router.currentRoute.value.query.pathId
  if (queryPathId) {
    selectedPathId.value = String(queryPathId)
  } else if (pathList.value.length > 0) {
    // 默认选中第一个路径
    selectedPathId.value = String(pathList.value[0].id)
  }
})
</script>

<template>
  <div class="resource-recommend-page">
    <div class="page-hero">
      <h2>智能资源推荐</h2>
      <p>AI 根据你的学习路径和画像，为你推荐最合适的学习资源</p>
    </div>

    <!-- 路径选择器 -->
    <div class="path-selector">
      <span class="selector-label">选择学习路径：</span>
      <el-select
        v-model="selectedPathId"
        placeholder="请选择学习路径"
        style="width: 360px"
        :loading="pathList.length === 0"
      >
        <el-option
          v-for="path in pathList"
          :key="path.id"
          :label="path.title || path.pathName"
          :value="String(path.id)"
        />
      </el-select>
    </div>

    <!-- 推荐资源列表 -->
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
            <span class="path-name">来自：{{ resource.pathName }}</span>
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

    <!-- 空状态 -->
    <div v-if="!loading && selectedPathId && recommendedResources.length === 0" class="empty-wrapper">
      <div class="empty-illustration">
        <div class="empty-icon">
          <el-icon :size="48"><Document /></el-icon>
        </div>
        <h3>暂无推荐资源</h3>
        <p>该路径暂未关联学习资源，请先通过智能体生成一些学习资源</p>
        <el-button type="primary" @click="$router.push('/resource/generate')">
          生成学习资源
        </el-button>
      </div>
    </div>

    <div v-if="!loading && !selectedPathId && pathList.length === 0" class="empty-wrapper">
      <div class="empty-illustration">
        <div class="empty-icon">
          <el-icon :size="48"><Guide /></el-icon>
        </div>
        <h3>还没有学习路径</h3>
        <p>请先创建一个学习路径，AI 才能为你推荐资源</p>
        <el-button type="primary" @click="$router.push('/path/generate')">
          生成学习路径
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { Document, Guide } from '@element-plus/icons-vue'
export default { components: { Document, Guide } }
</script>

<style scoped>
.resource-recommend-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 0;
}

/* 标题区 */
.page-hero {
  text-align: center;
  margin-bottom: 24px;
  padding: 28px 0 16px;
}

.page-hero h2 {
  font-size: 26px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.page-hero p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 路径选择器 */
.path-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.selector-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

/* 推荐列表 */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
}

.recommend-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.recommend-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
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
  min-width: 0;
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
  transition: all 0.3s;
}

.score-circle.high {
  border-color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
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
