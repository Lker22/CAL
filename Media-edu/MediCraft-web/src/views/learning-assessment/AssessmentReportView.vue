<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAssessmentStore } from '@/stores/assessment'
import { ElMessage } from 'element-plus'

const assessmentStore = useAssessmentStore()

const loading = ref(false)
const generating = ref(false)
const selectedPeriod = ref('week')

const periodOptions = [
  { label: '近7天', value: 'week' },
  { label: '近30天', value: 'month' },
  { label: '近90天', value: 'quarter' }
]

// 评估报告数据（从 /assessment/result 获取）
const reportData = computed(() => {
  let data = assessmentStore.assessmentResult || assessmentStore.assessmentReport
  if (!data) return null
  if (Array.isArray(data)) {
    data = data.length > 0 ? data[0] : null
  }
  if (!data) return null

  // knowledgeMastery 可能是 JSON 字符串，需要解析为对象
  let mastery = data.knowledgeMastery
  if (typeof mastery === 'string' && mastery) {
    try { mastery = JSON.parse(mastery) } catch (e) { mastery = null }
  }

  return {
    id: data.id,
    evaluateContent: data.evaluateContent || '',
    improveSuggest: data.improveSuggest || '',
    knowledgeMastery: mastery,
    createTime: data.createTime || ''
  }
})

// 知识点掌握列表（从 knowledgeMastery 展开）
const knowledgeList = computed(() => {
  const mastery = reportData.value?.knowledgeMastery
  if (!mastery || typeof mastery !== 'object' || Object.keys(mastery).length === 0) return []
  return Object.entries(mastery).map(([name, score]) => ({ name, score })).sort((a, b) => a.score - b.score)
})

// 综合评分（知识点平均分，无数据时显示"--"而非0）
const overallScore = computed(() => {
  if (knowledgeList.value.length === 0) return null
  return Math.round(knowledgeList.value.reduce((sum, k) => sum + k.score, 0) / knowledgeList.value.length)
})

// 生成报告
const generateReport = async () => {
  generating.value = true
  try {
    await assessmentStore.generateReport({ period: selectedPeriod.value })
    // 生成后从/result获取（knowledgeMastery已解析为对象）
    await assessmentStore.getAssessmentResult()
    if (assessmentStore.assessmentReport) {
      ElMessage.success('评估报告生成成功')
    } else {
      ElMessage.error('评估报告生成失败')
    }
  } catch (error) {
    ElMessage.error('生成报告失败: ' + error.message)
  } finally {
    generating.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    // 从/result获取最新报告（knowledgeMastery已正确解析）
    await assessmentStore.getAssessmentResult()
    console.log('[AssessmentReport] assessmentResult:', assessmentStore.assessmentResult)
    console.log('[AssessmentReport] knowledgeMastery:', assessmentStore.assessmentResult?.knowledgeMastery)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="assessment-report-page">
    <div class="page-header">
      <h2>学习评估报告</h2>
      <p>AI 根据你的学习数据生成个性化评估</p>
    </div>

    <div class="report-controls">
      <el-radio-group v-model="selectedPeriod" size="default">
        <el-radio-button
          v-for="opt in periodOptions"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
      <el-button
        type="primary"
        :loading="generating"
        @click="generateReport"
      >
        生成新报告
      </el-button>
    </div>

    <div v-loading="loading" class="report-container">
      <template v-if="reportData">
        <!-- 综合评分 -->
        <div class="score-card">
          <div class="score-circle">
            <span class="score-number">{{ overallScore ?? '--' }}</span>
            <span class="score-label">综合评分</span>
          </div>
          <div class="score-meta">
            <span>报告时间：{{ reportData.createTime?.substring(0, 10) || '未知' }}</span>
            <span>评估周期：{{ periodOptions.find(p => p.value === selectedPeriod)?.label }}</span>
          </div>
        </div>

        <!-- AI 评估内容 -->
        <div class="content-card">
          <h4>AI 评估报告</h4>
          <div class="ai-content" v-html="reportData.evaluateContent || '暂无评估内容'"></div>
        </div>

        <!-- 知识点掌握详情 -->
        <div class="knowledge-card" v-if="knowledgeList.length > 0">
          <h4>知识点掌握详情</h4>
          <div class="knowledge-list">
            <div
              v-for="(item, index) in knowledgeList"
              :key="index"
              class="knowledge-item"
            >
              <span class="knowledge-name">{{ item.name }}</span>
              <el-progress
                :percentage="item.score"
                :color="(p) => p >= 80 ? '#67C23A' : p >= 60 ? '#E6A23C' : '#F56C6C'"
                :show-text="false"
              />
              <span class="knowledge-score" :class="{ weak: item.score < 60 }">{{ item.score }}%</span>
            </div>
          </div>
        </div>

        <!-- 提升建议 -->
        <div class="suggest-card" v-if="reportData.improveSuggest">
          <h4>提升建议</h4>
          <div class="suggest-content" v-html="reportData.improveSuggest"></div>
        </div>
      </template>

      <template v-else>
        <div class="empty-card">
          <p>暂无评估报告，点击上方「生成新报告」按钮开始</p>
          <p class="empty-tip">AI 将根据你近期的学习行为、打卡记录等数据生成个性化评估</p>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.assessment-report-page {
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

.report-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.report-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.score-card,
.content-card,
.knowledge-card,
.suggest-card,
.empty-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.score-card {
  display: flex;
  align-items: center;
  gap: 32px;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: #fff;
  flex-shrink: 0;
}

.score-number {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.score-label {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}

.score-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.content-card h4,
.knowledge-card h4,
.suggest-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 16px;
}

.ai-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.knowledge-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.knowledge-name {
  width: 80px;
  font-size: 14px;
  color: #606266;
  flex-shrink: 0;
}

.knowledge-item :deep(.el-progress) {
  flex: 1;
}

.knowledge-score {
  width: 45px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1e2c;
  text-align: right;
  flex-shrink: 0;
}

.knowledge-score.weak {
  color: #F56C6C;
}

.suggest-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.empty-card {
  text-align: center;
  padding: 60px 24px;
}

.empty-card p {
  color: #909399;
  font-size: 16px;
  margin: 0 0 8px;
}

.empty-tip {
  font-size: 13px !important;
  color: #c0c4cc !important;
}
</style>
