<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAssessmentStore } from '@/stores/assessment'
import { assessmentApi } from '@/api/assessment'
import { CircleCheck, EditPen, VideoCamera } from '@element-plus/icons-vue'

const assessmentStore = useAssessmentStore()
const loading = ref(false)
const weakPointsData = ref(null)
const trendData = ref([])

// 雷达图数据（从 /assessment/stats 获取）
const radarData = computed(() => {
  const stats = assessmentStore.learningStats
  if (!stats) return null
  return [
    { name: '知识掌握', value: stats.masteryScore || 0 },
    { name: '技能应用', value: stats.applicationScore || 0 },
    { name: '学习进度', value: stats.progressScore || 0 },
    { name: '练习正确率', value: stats.accuracyScore || 0 },
    { name: '学习持续性', value: stats.consistencyScore || 0 }
  ]
})

// 知识掌握分布（从 /assessment/result 的 knowledgeMastery 计算）
const parseMastery = (raw) => {
  if (!raw) return null
  if (typeof raw === 'string') { try { return JSON.parse(raw) } catch { return null } }
  return raw
}

const knowledgeGraph = computed(() => {
  const data = assessmentStore.assessmentResult
  const mastery = parseMastery(data?.knowledgeMastery)
  if (!mastery || typeof mastery !== 'object' || Object.keys(mastery).length === 0) {
    // 没有评估报告数据，从 stats 推算
    const stats = assessmentStore.learningStats
    if (stats && (stats.totalTime > 0 || stats.totalQuiz > 0)) {
      const avgMastery = Math.round(
        ((stats.masteryScore || 0) + (stats.accuracyScore || 0) + (stats.progressScore || 0)) / 3
      )
      return { mastered: avgMastery, learning: Math.max(0, 100 - avgMastery), pending: 0 }
    }
    return { mastered: 0, learning: 0, pending: 100 }
  }
  // knowledgeMastery 是 { "MySQL": 75, "Java": 60 } 形式
  const values = Object.values(mastery)
  const avg = Math.round(values.reduce((a, b) => a + b, 0) / values.length)
  // 按分数段分类：>=70已掌握，40-70学习中，<40待学习
  // 如果所有分数都很低（都<40），按相对高低分配：前1/3已掌握，中1/3学习中，后1/3待学习
  let mastered, learning, pending
  const hasHigh = values.some(v => v >= 70)
  const hasMid = values.some(v => v >= 40 && v < 70)
  if (hasHigh || hasMid) {
    mastered = Math.round(values.filter(v => v >= 70).length / values.length * 100)
    learning = Math.round(values.filter(v => v >= 40 && v < 70).length / values.length * 100)
    pending = 100 - mastered - learning
  } else {
    // 全部低分，按平均分显示
    mastered = 0
    learning = Math.min(100, avg * 2) // 平均分25→50%学习中
    pending = 100 - learning
  }
  return { mastered, learning, pending: Math.max(0, pending), avg }
})

// 知识点详细列表（从 knowledgeMastery 展开）
const knowledgeList = computed(() => {
  const data = assessmentStore.assessmentResult
  const mastery = parseMastery(data?.knowledgeMastery)
  if (!mastery || typeof mastery !== 'object' || Object.keys(mastery).length === 0) {
    return []
  }
  return Object.entries(mastery).map(([name, score]) => ({
    name,
    mastery: score
  })).sort((a, b) => a.mastery - b.mastery)
})

// 薄弱点列表（掌握度低于60的知识点）
const weakPoints = computed(() => {
  return knowledgeList.value.filter(p => p.mastery < 60)
})

// 最近学习活动（从 /assessment/stats 的 behaviorByType 推算）
const recentActivity = computed(() => {
  const stats = assessmentStore.learningStats
  if (!stats) return []
  const activities = []
  const behaviorByType = stats.behaviorByType || {}
  for (const [type, count] of Object.entries(behaviorByType)) {
    activities.push({
      type: type === '完成' ? 'completed' : type === '做题' ? 'quiz' : 'study',
      title: `${type} ${count} 次`,
      date: stats.endDate || ''
    })
  }
  return activities
})

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      assessmentStore.getAssessmentResult(),
      assessmentStore.getLearningStats()
    ])
    // 额外获取薄弱点
    try {
      const wp = await assessmentApi.getWeakPoints()
      weakPointsData.value = wp.data
    } catch (e) { /* 无数据不报错 */ }
  } catch (error) {
    console.warn('获取评估结果失败:', error.message)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="assessment-result-page">
    <div class="page-header">
      <h2>评估结果详情</h2>
      <p>深入学习评估数据，发现提升空间</p>
    </div>

    <div v-loading="loading" class="result-container">
      <!-- 知识掌握分布 -->
      <div class="knowledge-card">
        <h4>知识掌握分布</h4>
        <div v-if="knowledgeGraph.mastered > 0 || knowledgeGraph.learning > 0" class="knowledge-chart">
          <el-progress
            type="dashboard"
            :percentage="knowledgeGraph.mastered"
            :color="() => '#67C23A'"
            :format="(p) => `${p}% 已掌握`"
          />
          <div class="knowledge-legend">
            <div class="legend-item">
              <span class="legend-dot mastered" />
              <span>已掌握 {{ knowledgeGraph.mastered }}%</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot learning" />
              <span>学习中 {{ knowledgeGraph.learning }}%</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot pending" />
              <span>待学习 {{ knowledgeGraph.pending }}%</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">
          <p>暂无知识掌握数据，完成学习路径生成评估报告后可查看</p>
        </div>
      </div>

      <!-- 知识点详细列表 -->
      <div class="knowledge-detail-card">
        <h4>知识点掌握详情</h4>
        <div v-if="knowledgeList.length > 0" class="knowledge-list">
          <div
            v-for="(item, index) in knowledgeList"
            :key="index"
            class="knowledge-item"
          >
            <span class="knowledge-name">{{ item.name }}</span>
            <el-progress
              :percentage="item.mastery"
              :color="(p) => p >= 80 ? '#67C23A' : p >= 60 ? '#E6A23C' : '#F56C6C'"
              :show-text="false"
            />
            <span class="knowledge-score">{{ item.mastery }}%</span>
          </div>
        </div>
        <div v-else class="empty-tip">
          <p>暂无知识点数据，生成评估报告后可查看详细掌握度</p>
        </div>
      </div>

      <!-- 薄弱点分析 -->
      <div class="weakpoints-card">
        <h4>薄弱知识点</h4>
        <div v-if="weakPoints.length > 0" class="weakpoints-list">
          <div
            v-for="(point, index) in weakPoints"
            :key="index"
            class="weakpoint-item"
          >
            <span class="weakpoint-name">{{ point.name }}</span>
            <el-progress
              :percentage="point.mastery"
              :color="(p) => p < 40 ? '#F56C6C' : '#E6A23C'"
              :show-text="false"
            />
            <span class="weakpoint-score">{{ point.mastery }}%</span>
          </div>
        </div>
        <div v-else class="empty-tip">
          <p>暂无薄弱知识点（掌握度低于60%的知识点会显示在这里）</p>
        </div>
      </div>

      <!-- 雷达图 -->
      <div class="radar-card">
        <h4>能力雷达图</h4>
        <div v-if="radarData && radarData.some(d => d.value > 0)" class="radar-chart">
          <div class="radar-axis">
            <div
              v-for="(item, index) in radarData"
              :key="index"
              class="radar-item"
            >
              <span class="radar-label">{{ item.name }}</span>
              <el-progress
                :percentage="item.value"
                :color="() => '#409EFF'"
                :stroke-width="8"
                :show-text="true"
              />
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">
          <p>暂无能力数据，完成学习和练习后可生成能力评估</p>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="activity-card">
        <h4>最近学习活动</h4>
        <div v-if="recentActivity.length > 0" class="activity-list">
          <div
            v-for="(activity, index) in recentActivity"
            :key="index"
            class="activity-item"
          >
            <el-icon
              :size="20"
              :color="
                activity.type === 'completed'
                  ? '#67C23A'
                  : activity.type === 'quiz'
                  ? '#409EFF'
                  : '#E6A23C'
              "
            >
              <CircleCheck v-if="activity.type === 'completed'" />
              <EditPen v-else-if="activity.type === 'quiz'" />
              <VideoCamera v-else />
            </el-icon>
            <div class="activity-info">
              <span class="activity-title">{{ activity.title }}</span>
            </div>
            <span class="activity-date">{{ activity.date }}</span>
          </div>
        </div>
        <div v-else class="empty-tip">
          <p>暂无学习活动记录，完成学习路径打卡或练习后可查看</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assessment-result-page {
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

.result-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.knowledge-card,
.knowledge-detail-card,
.trend-card,
.weakpoints-card,
.radar-card,
.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.knowledge-card h4,
.knowledge-detail-card h4,
.trend-card h4,
.weakpoints-card h4,
.radar-card h4,
.activity-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 20px;
}

.empty-tip {
  text-align: center;
  padding: 20px 0;
  color: #909399;
  font-size: 14px;
}

.knowledge-chart {
  display: flex;
  align-items: center;
  gap: 32px;
}

.knowledge-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.mastered { background: #67c23a; }
.legend-dot.learning { background: #409eff; }
.legend-dot.pending { background: #909399; }

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

.weakpoints-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.weakpoint-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.weakpoint-name {
  width: 80px;
  font-size: 14px;
  color: #606266;
}

.weakpoint-item :deep(.el-progress) {
  flex: 1;
}

.weakpoint-score {
  width: 45px;
  font-size: 14px;
  font-weight: 600;
  color: #F56C6C;
  text-align: right;
}

.radar-axis {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.radar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.radar-label {
  width: 80px;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.radar-item :deep(.el-progress) {
  flex: 1;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-title {
  font-size: 14px;
  color: #303133;
}

.activity-date {
  font-size: 12px;
  color: #c0c4cc;
}

@media (max-width: 1024px) {
  .result-container {
    grid-template-columns: 1fr;
  }
}
</style>
