<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAssessmentStore } from '@/stores/assessment'
import { Clock, Document, EditPen, Trophy } from '@element-plus/icons-vue'

const assessmentStore = useAssessmentStore()

const loading = ref(false)
const selectedTab = ref('overview')

// 默认空数据
const emptyStats = {
  totalTime: 0,
  totalResources: 0,
  totalQuiz: 0,
  avgScore: 0,
  weekData: [],
  resourceStats: [],
  subjectStats: []
}

// 优先使用后端数据
const stats = computed(() => {
  const data = assessmentStore.learningStats
  if (!data) return emptyStats
  return {
    totalTime: data.totalTime || 0,
    totalResources: data.totalResources || 0,
    totalQuiz: data.totalQuiz || 0,
    avgScore: data.avgScore || 0,
    weekData: data.weekData || [],
    resourceStats: data.resourceStats || [],
    subjectStats: data.subjectStats || []
  }
})

// 格式化时长：分钟
const formatTime = (minutes) => {
  if (!minutes) return '0分钟'
  return `${minutes}分钟`
}

onMounted(async () => {
  loading.value = true
  try {
    await assessmentStore.getLearningStats()
  } catch (error) {
    console.warn('获取统计数据失败:', error.message)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="learning-stats-page">
    <div class="page-header">
      <h2>学习数据统计</h2>
      <p>全面了解你的学习行为和习惯</p>
    </div>

    <div v-loading="loading" class="stats-container">
      <!-- 总览卡片 -->
      <div class="overview-grid">
        <div class="stat-card">
          <el-icon :size="32" color="#409EFF"><Clock /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ stats.totalTime }}分钟</span>
            <span class="stat-label">总学习时长</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#67C23A"><Document /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ stats.totalResources }}</span>
            <span class="stat-label">学习资源</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#E6A23C"><EditPen /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ stats.totalQuiz }}</span>
            <span class="stat-label">练习题目</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#F56C6C"><Trophy /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ stats.avgScore }}%</span>
            <span class="stat-label">平均正确率</span>
          </div>
        </div>
      </div>

      <!-- 周学习趋势 -->
      <div class="stats-card">
        <h4>本周学习趋势</h4>
        <div class="week-chart">
          <div class="week-bars">
            <div
              v-for="(day, index) in stats.weekData"
              :key="index"
              class="day-column"
            >
              <div class="bar-container">
                <div
                  class="bar time-bar"
                  :style="{ height: `${(day.time / 250) * 100}%` }"
                />
              </div>
              <span class="day-label">{{ day.day }}</span>
              <span class="day-value">{{ formatTime(day.time) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 资源类型分布 -->
      <div class="stats-card">
        <h4>资源类型分布</h4>
        <div class="resource-distribution">
          <div
            v-for="(item, index) in stats.resourceStats"
            :key="index"
            class="resource-item"
          >
            <div class="resource-header">
              <span class="resource-type">{{ item.type }}</span>
              <span class="resource-count">{{ item.count }}个</span>
            </div>
            <el-progress
              :percentage="item.percentage"
              :color="() => ['#409EFF','#67C23A','#E6A23C','#909399','#B37FEB'][index] || '#909399'"
              :show-text="false"
            />
            <span class="resource-percentage">{{ item.percentage }}%</span>
          </div>
        </div>
      </div>

      <!-- 学科进度 -->
      <div class="stats-card full-width">
        <h4>各学科学习进度</h4>
        <div class="subject-list">
          <div
            v-for="(subject, index) in stats.subjectStats"
            :key="index"
            class="subject-item"
          >
            <span class="subject-name">{{ subject.name }}</span>
            <div class="subject-info">
              <el-progress
                :percentage="subject.progress"
                :color="(p) => p >= 80 ? '#67C23A' : p >= 50 ? '#409EFF' : '#E6A23C'"
                :show-text="false"
              />
              <span class="subject-time">{{ formatTime(subject.time) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.learning-stats-page {
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

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d1e2c;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.stats-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #f0f0f0;
}

.stats-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 20px;
}

.stats-card.full-width {
  grid-column: 1 / -1;
}

.week-chart {
  padding: 20px 0;
}

.week-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  padding: 0 20px;
}

.day-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-container {
  width: 40px;
  height: 150px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 4px;
}

.bar {
  width: 100%;
  border-radius: 4px;
  transition: height 0.3s;
}

.time-bar {
  background: linear-gradient(to top, #409eff, #67c23a);
}

.day-label {
  font-size: 13px;
  color: #606266;
}

.day-value {
  font-size: 12px;
  color: #909399;
}

.resource-distribution {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-header {
  width: 100px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.resource-item :deep(.el-progress) {
  flex: 1;
}

.resource-percentage {
  width: 45px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1e2c;
  text-align: right;
}

.subject-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.subject-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.subject-name {
  width: 120px;
  font-size: 14px;
  color: #606266;
}

.subject-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.subject-info :deep(.el-progress) {
  flex: 1;
}

.subject-time {
  width: 60px;
  font-size: 13px;
  color: #909399;
  text-align: right;
}

@media (max-width: 1024px) {
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
