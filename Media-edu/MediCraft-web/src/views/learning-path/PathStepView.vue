<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { STEP_STATUS } from '@/utils/constants'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)
const pathId = route.params.id

const pathDetail = computed(() => learningPathStore.currentPath || {})
const steps = computed(() => learningPathStore.currentSteps || [])
const progress = computed(() => pathDetail.value?.progress || 0)

const getStepStatus = (status) => {
  return STEP_STATUS[status] || STEP_STATUS.pending
}

const completeStep = async (step) => {
  try {
    await learningPathStore.completeStep(step.id, { duration: 120 })
    ElMessage.success('学习打卡成功！')
    // 重新加载详情以刷新进度
    await learningPathStore.getPathDetail(pathId)
  } catch (error) {
    ElMessage.error('打卡失败，请重试')
  }
}

const viewResource = (resourceName) => {
  router.push('/resource/list')
}

const goBack = () => {
  router.push('/path/list')
}

onMounted(async () => {
  loading.value = true
  try {
    await learningPathStore.getPathDetail(pathId)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="path-step-page">
    <!-- 顶部导航 -->
    <div class="page-nav">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回路径列表
      </el-button>
    </div>

    <!-- 路径概览卡片 -->
    <div class="path-overview" v-if="pathDetail">
      <div class="overview-left">
        <h2>{{ pathDetail.title || pathDetail.pathName }}</h2>
        <div class="overview-stats">
          <div class="ov-stat">
            <span class="ov-num">{{ steps.length }}</span>
            <span class="ov-label">总步骤</span>
          </div>
          <div class="ov-divider"></div>
          <div class="ov-stat">
            <span class="ov-num green">{{ pathDetail.completedSteps || 0 }}</span>
            <span class="ov-label">已完成</span>
          </div>
          <div class="ov-divider"></div>
          <div class="ov-stat">
            <span class="ov-num blue">{{ progress }}%</span>
            <span class="ov-label">进度</span>
          </div>
        </div>
      </div>
      <div class="overview-right">
        <el-progress
          type="dashboard"
          :percentage="progress"
          :width="100"
          :stroke-width="8"
          color="#409eff"
        >
          <template #default="{ percentage }">
            <span class="progress-inner">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>
    </div>

    <!-- 步骤时间线 -->
    <div class="steps-section" v-loading="loading">
      <div class="section-header">
        <h3>学习步骤</h3>
        <el-button size="small" @click="$router.push('/path/adjust')">
          调整路径
        </el-button>
      </div>

      <div class="steps-timeline">
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in steps"
            :key="step.id"
            :type="getStepStatus(step.status).color"
            :hollow="step.status === 'pending'"
            placement="top"
          >
            <div class="step-card" :class="step.status">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-header">
                  <div class="step-title-area">
                    <h4>{{ step.title }}</h4>
                    <el-tag
                      :color="getStepStatus(step.status).color"
                      effect="dark"
                      size="small"
                    >
                      {{ getStepStatus(step.status).label }}
                    </el-tag>
                  </div>
                  <div class="step-right">
                    <span class="step-time" v-if="step.completedAt">
                      {{ step.completedAt }} 完成
                    </span>
                    <span class="step-time" v-else>
                      预计 {{ step.duration || '3-5天' }}
                    </span>
                    <el-button
                      v-if="step.status === 'inProgress'"
                      type="success"
                      size="small"
                      @click="completeStep(step)"
                    >
                      打卡完成
                    </el-button>
                  </div>
                </div>

                <p class="step-desc">{{ step.description }}</p>

                <div class="step-resources" v-if="step.resources && step.resources.length">
                  <span class="resource-label">相关资源：</span>
                  <el-tag
                    v-for="(res, index) in step.resources"
                    :key="index"
                    size="small"
                    type="info"
                    effect="plain"
                    class="resource-tag"
                    @click="viewResource(res)"
                  >
                    {{ res }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-actions">
      <el-button @click="$router.push('/path/adjust')">调整路径</el-button>
      <el-button @click="$router.push('/recommend/resource')">查看推荐资源</el-button>
    </div>
  </div>
</template>

<script>
import { ArrowLeft } from '@element-plus/icons-vue'
export default { components: { ArrowLeft } }
</script>

<style scoped>
.path-step-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 0;
}

.page-nav {
  margin-bottom: 16px;
}

/* 路径概览卡片 */
.path-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  color: #fff;
}

.overview-left h2 {
  font-size: 22px;
  margin: 0 0 16px;
  color: #fff;
}

.overview-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.ov-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ov-num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.ov-num.green { color: #a5f5a5; }
.ov-num.blue { color: #a5d8ff; }

.ov-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.ov-divider {
  width: 1px;
  height: 32px;
  background: rgba(255,255,255,0.3);
}

.overview-right :deep(.el-progress-circle) path:first-child {
  stroke: rgba(255,255,255,0.2);
}

.progress-inner {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

/* 步骤区域 */
.steps-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  border: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 17px;
  color: #1d1e2c;
  margin: 0;
}

/* 步骤卡片 */
.step-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  margin-bottom: 4px;
}

.step-card:hover {
  background: #f0f7ff;
  border-color: #c6e2ff;
}

.step-card.completed {
  background: #f0f9eb;
  border-color: #e1f3d8;
}

.step-number {
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 50%;
  background: #e4e7ed;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-card.inProgress .step-number {
  background: #409eff;
  color: #fff;
}

.step-card.completed .step-number {
  background: #67c23a;
  color: #fff;
}

.step-content {
  flex: 1;
  min-width: 0;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.step-title-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-title-area h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
}

.step-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.step-time {
  font-size: 12px;
  color: #909399;
}

.step-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 10px;
  line-height: 1.6;
}

.step-resources {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.resource-label {
  font-size: 12px;
  color: #909399;
}

.resource-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.resource-tag:hover {
  color: #409eff;
  border-color: #409eff;
}

/* 底部操作 */
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
