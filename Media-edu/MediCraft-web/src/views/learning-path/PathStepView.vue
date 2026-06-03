<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { STEP_STATUS } from '@/utils/constants'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)
const pathId = route.params.id

const pathDetail = computed(() => learningPathStore.currentPath || {})
const steps = computed(() => learningPathStore.currentSteps || [])
const progress = computed(() => pathDetail.value?.progress || 0)

// 保存原始学习周期（后端调整后会覆盖为"延长X天"格式，需要记住原始值）
const originalDuration = ref('')
const ORIGINAL_DURATION_KEY = `path_original_duration_${pathId}`

watch(pathDetail, (val) => {
  if (!val || !val.duration) return
  // "延长"/"压缩" 开头表示已调整，不覆盖
  if (val.duration.startsWith('延长') || val.duration.startsWith('压缩')) return
  originalDuration.value = val.duration
  localStorage.setItem(ORIGINAL_DURATION_KEY, val.duration)
}, { immediate: true })

const getStepStatus = (status) => {
  return STEP_STATUS[status] || STEP_STATUS.pending
}

// ========== 步骤导航 ==========
const activeStepIndex = ref(0)
const canGoPrev = computed(() => activeStepIndex.value > 0)
const canGoNext = computed(() => activeStepIndex.value < steps.value.length - 1)

function goPrev() {
  if (canGoPrev.value) activeStepIndex.value--
}
function goNext() {
  if (canGoNext.value) activeStepIndex.value++
}

// ========== 学习倒计时 ==========
const countdownText = ref('')
const countdownExpired = ref(false)
let countdownTimer = null

// 解析普通时长（如 "2周"、"1个月"、"10天"）
function parseDuration(duration) {
  if (!duration) return 0
  const match = duration.match(/(\d+)/)
  if (!match) return 0
  const num = parseInt(match[1])
  if (duration.includes('月')) return num * 30
  if (duration.includes('周')) return num * 7
  if (duration.includes('天')) return num
  return num
}

// 解析调整信息（如 "延长7天"、"压缩3天"），返回 { isAdjusted, days }
function parseAdjustment(duration) {
  if (!duration) return { isAdjusted: false, days: 0 }
  if (duration.startsWith('延长')) {
    const match = duration.match(/(\d+)/)
    return { isAdjusted: true, days: match ? parseInt(match[1]) : 0 }
  }
  if (duration.startsWith('压缩')) {
    const match = duration.match(/(\d+)/)
    return { isAdjusted: true, days: match ? -parseInt(match[1]) : 0 }
  }
  return { isAdjusted: false, days: 0 }
}

function startCountdown() {
  stopCountdown()
  if (!pathDetail.value) return

  const durationStr = pathDetail.value.duration
  if (!durationStr || !pathDetail.value.createTime) {
    countdownText.value = ''
    return
  }

  const startDate = new Date(pathDetail.value.createTime)
  const { isAdjusted, days: adjustDays } = parseAdjustment(durationStr)

  // 用原始周期计算基础截止时间，调整后在此基础上加减
  let baseDays
  if (isAdjusted) {
    const saved = originalDuration.value || localStorage.getItem(ORIGINAL_DURATION_KEY) || ''
    baseDays = parseDuration(saved) || parseDuration(durationStr)
  } else {
    baseDays = parseDuration(durationStr)
  }
  if (!baseDays) { countdownText.value = ''; return }

  const originalDeadline = new Date(startDate.getTime() + baseDays * 24 * 60 * 60 * 1000)

  let deadline
  if (!isAdjusted) {
    deadline = originalDeadline
  } else {
    // 剩余 = 原始剩余 + 延长/压缩天数
    const now = new Date()
    const remainingMs = originalDeadline - now
    const adjustMs = adjustDays * 24 * 60 * 60 * 1000
    deadline = new Date(now.getTime() + Math.max(0, remainingMs + adjustMs))
  }

  const update = () => {
    const now = new Date()
    const diff = deadline - now
    if (diff <= 0) {
      countdownText.value = ''
      countdownExpired.value = true
      return
    }
    countdownExpired.value = false
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)
    countdownText.value = `${days}天 ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }

  update()
  countdownTimer = setInterval(update, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const completeStep = async (step) => {
  try {
    await learningPathStore.completeStep(step.id, { duration: 120 })
    ElMessage.success('学习打卡成功！')
    await learningPathStore.getPathDetail(pathId)
    startCountdown()
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

const navigateToAdjust = () => {
  router.push('/path/adjust')
}

const navigateToRecommend = () => {
  router.push('/recommend/resource')
}

const onStepClick = (step, index) => {
  activeStepIndex.value = index
  router.push({
    path: `/path/step/${step.id}/resource`,
    query: {
      pathId: pathId,
      stepType: step.stepType || 'document',
      title: step.title
    }
  })
}

onMounted(async () => {
  loading.value = true
  try {
    await learningPathStore.getPathDetail(pathId)
    startCountdown()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  stopCountdown()
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
          <div class="ov-divider"></div>
          <div class="ov-stat">
            <span class="ov-num orange">{{ pathDetail.duration || '未设置' }}</span>
            <span class="ov-label">学习周期</span>
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

    <!-- 步骤导航 + 倒计时 -->
    <div class="step-navigation">
      <el-button
        size="large"
        :disabled="!canGoPrev"
        @click="goPrev"
      >
        <el-icon><ArrowLeft /></el-icon>
        上一步
      </el-button>

      <div class="countdown-area">
        <div v-if="countdownText" class="countdown-timer">
          <span class="countdown-label">剩余学习时间</span>
          <span class="countdown-value">{{ countdownText }}</span>
        </div>
        <div v-else-if="countdownExpired" class="countdown-expired">
          学习周期已结束
        </div>
      </div>

      <el-button
        size="large"
        :disabled="!canGoNext"
        @click="goNext"
      >
        下一步
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- 步骤时间线 -->
    <div class="steps-section" v-loading="loading">
      <div class="section-header">
        <h3>学习步骤</h3>
        <el-button size="small" @click="navigateToAdjust">
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
            <div
              class="step-card"
              :class="[step.status, { 'step-active': activeStepIndex === index }]"
              @click="onStepClick(step, index)"
            >
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-header">
                  <div class="step-title-area">
                    <h4>{{ step.title }}</h4>
                    <el-tag
                      v-if="step.stepType === 'quiz'"
                      type="danger"
                      size="small"
                      effect="plain"
                    >
                      知识测验
                    </el-tag>
                    <el-tag
                      v-if="step.resourceIds"
                      type="success"
                      size="small"
                      effect="plain"
                    >
                      已生成资源
                    </el-tag>
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
                      @click.stop="completeStep(step)"
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
      <el-button @click="navigateToAdjust">调整路径</el-button>
      <el-button @click="navigateToRecommend">查看推荐资源</el-button>
    </div>
  </div>
</template>

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
.ov-num.orange { color: #ffd591; }

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

/* 步骤导航 + 倒计时 */
.step-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16px;
  padding: 20px 28px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
}

.countdown-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.countdown-timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.countdown-label {
  font-size: 12px;
  color: #909399;
}

.countdown-value {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}

.countdown-expired {
  font-size: 15px;
  color: #909399;
  font-weight: 500;
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
  cursor: pointer;
}

.step-card:hover {
  background: #f0f7ff;
  border-color: #c6e2ff;
}

.step-card.step-active {
  background: #f0f7ff;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff;
}

.step-card.completed {
  background: #f0f9eb;
  border-color: #e1f3d8;
}

.step-card.completed.step-active {
  box-shadow: 0 0 0 1px #67c23a;
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
