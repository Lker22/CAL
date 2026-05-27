<script setup>
import { computed } from 'vue'
import { GENERATION_STATUS } from '@/utils/constants'

/**
 * 进度追踪组件
 * 用于显示资源生成等异步任务的进度
 */
const props = defineProps({
  progress: {
    type: Number,
    default: 0
  },
  status: {
    type: String,
    default: 'processing'
  },
  statusText: {
    type: String,
    default: ''
  },
  showSteps: {
    type: Boolean,
    default: false
  },
  steps: {
    type: Array,
    default: () => []
  }
})

const statusConfig = computed(() => GENERATION_STATUS[props.status] || GENERATION_STATUS.processing)

const displayText = computed(() => {
  if (props.statusText) return props.statusText
  return statusConfig.value.label
})

const progressColor = computed(() => {
  if (props.status === 'failed') return '#F56C6C'
  if (props.status === 'completed') return '#67C23A'
  return '#409EFF'
})
</script>

<template>
  <div class="progress-tracker">
    <!-- 进度条 -->
    <div class="progress-bar-section">
      <div class="progress-header">
        <span class="progress-label">{{ displayText }}</span>
        <span class="progress-value">{{ progress }}%</span>
      </div>
      <el-progress
        :percentage="progress"
        :color="progressColor"
        :stroke-width="10"
        :show-text="false"
      />
    </div>

    <!-- 步骤列表 -->
    <div v-if="showSteps && steps.length" class="steps-section">
      <el-timeline>
        <el-timeline-item
          v-for="(step, index) in steps"
          :key="index"
          :type="step.status === 'completed' ? 'success' : step.status === 'processing' ? 'primary' : 'info'"
          :hollow="step.status === 'pending'"
          :timestamp="step.time || ''"
          placement="top"
        >
          <div class="step-content">
            <span class="step-name">{{ step.name }}</span>
            <span
              v-if="step.description"
              class="step-desc"
            >{{ step.description }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<style scoped>
.progress-tracker {
  padding: 16px;
}

.progress-bar-section {
  margin-bottom: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.progress-value {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

.steps-section {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-name {
  font-size: 14px;
  color: #303133;
}

.step-desc {
  font-size: 12px;
  color: #909399;
}
</style>
