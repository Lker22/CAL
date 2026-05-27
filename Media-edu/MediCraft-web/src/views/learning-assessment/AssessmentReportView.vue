<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAssessmentStore } from '@/stores/assessment'
import { formatDate } from '@/utils/format'

const router = useRouter()
const assessmentStore = useAssessmentStore()

const loading = ref(false)
const selectedPeriod = ref('week')

// 示例评估报告数据
const report = ref({
  id: 1,
  generateDate: '2024-01-15',
  period: '最近 7 天',
  overallScore: 85,
  studyTime: 1260, // 分钟
  completedSteps: 8,
  totalSteps: 12,
  quizAverage: 88,
  dimensions: [
    { name: '知识掌握', score: 82, maxScore: 100 },
    { name: '学习进度', score: 75, maxScore: 100 },
    { name: '练习正确率', score: 88, maxScore: 100 },
    { name: '学习持续性', score: 90, maxScore: 100 },
    { name: '资源利用', score: 85, maxScore: 100 }
  ],
  strengths: [
    '学习持续性好，能够保持每天学习',
    '练习题正确率较高，基础知识掌握扎实'
  ],
  weaknesses: [
    '学习进度略低于预期，建议适当增加学习时间',
    '部分难点知识点需要加强复习'
  ],
  suggestions: [
    '建议每天增加 30 分钟学习时间',
    '针对薄弱知识点进行专项练习',
    '多利用思维导图整理知识体系'
  ]
})

const periodOptions = [
  { value: 'week', label: '最近 7 天' },
  { value: 'month', label: '最近 30 天' },
  { value: 'quarter', label: '最近 90 天' }
]

// 生成新报告
const generateReport = async () => {
  loading.value = true
  try {
    await assessmentStore.generateReport({ period: selectedPeriod.value })
  } finally {
    loading.value = false
  }
}

// 查看详细结果
const viewResult = () => {
  router.push('/assessment/result')
}

// 查看数据统计
const viewStats = () => {
  router.push('/learning/stats')
}

onMounted(async () => {
  loading.value = true
  try {
    await assessmentStore.getAssessmentReport({ period: selectedPeriod.value })
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="assessment-report-page">
    <div class="page-header">
      <h2>学习评估报告</h2>
      <p>全面了解你的学习情况和进步轨迹</p>
    </div>

    <!-- 周期选择 -->
    <div class="period-selector">
      <el-radio-group v-model="selectedPeriod" size="large">
        <el-radio-button
          v-for="opt in periodOptions"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
      <el-button type="primary" :loading="loading" @click="generateReport">
        生成新报告
      </el-button>
    </div>

    <div v-loading="loading" class="report-container">
      <!-- 总体评分 -->
      <div class="overview-card">
        <div class="score-section">
          <div class="score-circle">
            <span class="score-value">{{ report.overallScore }}</span>
            <span class="score-label">综合评分</span>
          </div>
        </div>
        <div class="info-section">
          <div class="info-item">
            <el-icon :size="24" color="#409EFF"><Clock /></el-icon>
            <div>
              <span class="info-value">{{ Math.floor(report.studyTime / 60) }}小时{{ report.studyTime % 60 }}分钟</span>
              <span class="info-label">学习时长</span>
            </div>
          </div>
          <div class="info-item">
            <el-icon :size="24" color="#67C23A"><CircleCheck /></el-icon>
            <div>
              <span class="info-value">{{ report.completedSteps }}/{{ report.totalSteps }}</span>
              <span class="info-label">完成步骤</span>
            </div>
          </div>
          <div class="info-item">
            <el-icon :size="24" color="#E6A23C"><Trophy /></el-icon>
            <div>
              <span class="info-value">{{ report.quizAverage }}%</span>
              <span class="info-label">练习正确率</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 维度评分 -->
      <div class="dimensions-card">
        <h4>维度分析</h4>
        <div class="dimension-list">
          <div
            v-for="(dim, index) in report.dimensions"
            :key="index"
            class="dimension-item"
          >
            <span class="dimension-name">{{ dim.name }}</span>
            <el-progress
              :percentage="dim.score"
              :color="dim.score >= 80 ? '#67C23A' : dim.score >= 60 ? '#409EFF' : '#F56C6C'"
              :show-text="false"
            />
            <span class="dimension-score">{{ dim.score }}分</span>
          </div>
        </div>
      </div>

      <!-- 优势与建议 -->
      <div class="suggestion-card">
        <div class="suggestion-section">
          <h4>
            <el-icon color="#67C23A"><CircleCheck /></el-icon>
            学习优势
          </h4>
          <ul>
            <li v-for="(item, i) in report.strengths" :key="i">{{ item }}</li>
          </ul>
        </div>
        <div class="suggestion-section">
          <h4>
            <el-icon color="#F56C6C"><Warning /></el-icon>
            需要改进
          </h4>
          <ul>
            <li v-for="(item, i) in report.weaknesses" :key="i">{{ item }}</li>
          </ul>
        </div>
        <div class="suggestion-section">
          <h4>
            <el-icon color="#409EFF"><Reading /></el-icon>
            学习建议
          </h4>
          <ul>
            <li v-for="(item, i) in report.suggestions" :key="i">{{ item }}</li>
          </ul>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="report-actions">
        <el-button type="primary" size="large" @click="viewResult">
          查看详细结果
        </el-button>
        <el-button size="large" @click="viewStats">
          查看数据统计
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { Clock, CircleCheck, Trophy, Warning, Reading } from '@element-plus/icons-vue'
export default {
  components: { Clock, CircleCheck, Trophy, Warning, Reading }
}
</script>

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

.period-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.report-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-card {
  display: flex;
  background: linear-gradient(135deg, #1d1e2c 0%, #2d2e42 100%);
  border-radius: 12px;
  padding: 32px;
  color: #fff;
}

.score-section {
  display: flex;
  align-items: center;
  padding-right: 40px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: #67c23a;
  line-height: 1;
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.info-section {
  flex: 1;
  display: flex;
  justify-content: space-around;
  padding-left: 40px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-item .info-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.info-item .info-label {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}

.dimensions-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.dimensions-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 20px;
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dimension-name {
  width: 80px;
  font-size: 14px;
  color: #606266;
}

.dimension-item :deep(.el-progress) {
  flex: 1;
}

.dimension-score {
  width: 50px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1e2c;
  text-align: right;
}

.suggestion-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.suggestion-section {
  background: #f9fafb;
  border-radius: 10px;
  padding: 20px;
}

.suggestion-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.suggestion-section ul {
  margin: 0;
  padding-left: 20px;
}

.suggestion-section li {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.report-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 20px 0;
}
</style>
