<script setup>
import { ref, onMounted } from 'vue'
import { useAssessmentStore } from '@/stores/assessment'

const assessmentStore = useAssessmentStore()

const loading = ref(false)

// 示例评估结果数据
const result = ref({
  knowledgeGraph: {
    mastered: 65,
    learning: 25,
    pending: 10
  },
  trendData: [
    { date: '01-09', score: 75 },
    { date: '01-10', score: 78 },
    { date: '01-11', score: 80 },
    { date: '01-12', score: 77 },
    { date: '01-13', score: 82 },
    { date: '01-14', score: 85 },
    { date: '01-15', score: 88 }
  ],
  weakPoints: [
    { name: '概率统计', mastery: 45 },
    { name: '线性代数', mastery: 55 },
    { name: '算法复杂度', mastery: 60 }
  ],
  recentActivity: [
    { type: 'completed', title: 'Python 基础语法', date: '2024-01-15' },
    { type: 'quiz', title: '数据结构练习题', score: 92, date: '2024-01-14' },
    { type: 'video', title: '神经网络教程', duration: 45, date: '2024-01-14' }
  ]
})

// 雷达图数据
const radarData = ref([
  { name: '知识掌握', value: 82 },
  { name: '技能应用', value: 75 },
  { name: '学习进度', value: 88 },
  { name: '练习正确率', value: 90 },
  { name: '学习持续性', value: 85 }
])

// 雷达图最大值
const radarMax = 100

onMounted(async () => {
  loading.value = true
  try {
    await assessmentStore.getAssessmentResult()
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
        <div class="knowledge-chart">
          <el-progress
            type="dashboard"
            :percentage="result.knowledgeGraph.mastered"
            :color="{ '0%': '#409EFF', '100%': '#67C23A' }"
            :format="(p) => `${p}% 已掌握`"
          />
          <div class="knowledge-legend">
            <div class="legend-item">
              <span class="legend-dot mastered" />
              <span>已掌握 {{ result.knowledgeGraph.mastered }}%</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot learning" />
              <span>学习中 {{ result.knowledgeGraph.learning }}%</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot pending" />
              <span>待学习 {{ result.knowledgeGraph.pending }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 学习趋势图 -->
      <div class="trend-card">
        <h4>学习趋势</h4>
        <div class="trend-chart">
          <div class="trend-bars">
            <div
              v-for="(item, index) in result.trendData"
              :key="index"
              class="trend-bar"
              :style="{ height: `${(item.score / 100) * 150}px` }"
            >
              <span class="bar-value">{{ item.score }}</span>
            </div>
          </div>
          <div class="trend-dates">
            <span
              v-for="(item, index) in result.trendData"
              :key="index"
              class="date-label"
            >
              {{ item.date }}
            </span>
          </div>
        </div>
      </div>

      <!-- 薄弱点分析 -->
      <div class="weakpoints-card">
        <h4>薄弱知识点</h4>
        <div class="weakpoints-list">
          <div
            v-for="(point, index) in result.weakPoints"
            :key="index"
            class="weakpoint-item"
          >
            <span class="weakpoint-name">{{ point.name }}</span>
            <el-progress
              :percentage="point.mastery"
              :color="point.mastery < 60 ? '#F56C6C' : '#E6A23C'"
              :show-text="false"
            />
            <span class="weakpoint-score">{{ point.mastery }}%</span>
          </div>
        </div>
      </div>

      <!-- 雷达图 -->
      <div class="radar-card">
        <h4>能力雷达图</h4>
        <div class="radar-chart">
          <div class="radar-placeholder">
            <div class="radar-point" style="top: 20%; left: 50%;">
              <span>{{ radarData[0]?.value }}</span>
            </div>
            <div class="radar-point" style="top: 40%; left: 80%;">
              <span>{{ radarData[1]?.value }}</span>
            </div>
            <div class="radar-point" style="top: 70%; left: 70%;">
              <span>{{ radarData[2]?.value }}</span>
            </div>
            <div class="radar-point" style="top: 70%; left: 30%;">
              <span>{{ radarData[3]?.value }}</span>
            </div>
            <div class="radar-point" style="top: 40%; left: 20%;">
              <span>{{ radarData[4]?.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="activity-card">
        <h4>最近学习活动</h4>
        <div class="activity-list">
          <div
            v-for="(activity, index) in result.recentActivity"
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
              <component
                :is="
                  activity.type === 'completed'
                    ? 'CircleCheck'
                    : activity.type === 'quiz'
                    ? 'EditPen'
                    : 'VideoCamera'
                "
              />
            </el-icon>
            <div class="activity-info">
              <span class="activity-title">{{ activity.title }}</span>
              <span v-if="activity.score" class="activity-score">得分：{{ activity.score }}%</span>
              <span v-if="activity.duration" class="activity-score">时长：{{ activity.duration }}分钟</span>
            </div>
            <span class="activity-date">{{ activity.date }}</span>
          </div>
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
.trend-card,
.weakpoints-card,
.radar-card,
.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.knowledge-card h4,
.trend-card h4,
.weakpoints-card h4,
.radar-card h4,
.activity-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 20px;
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

.legend-dot.mastered {
  background: #67c23a;
}

.legend-dot.learning {
  background: #409eff;
}

.legend-dot.pending {
  background: #909399;
}

.trend-chart {
  text-align: center;
}

.trend-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 150px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4e7ed;
}

.trend-bar {
  width: 32px;
  background: linear-gradient(to top, #409eff, #67c23a);
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
}

.bar-value {
  font-size: 12px;
  color: #fff;
  font-weight: 600;
}

.trend-dates {
  display: flex;
  justify-content: space-around;
  margin-top: 8px;
}

.date-label {
  width: 40px;
  font-size: 12px;
  color: #909399;
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
  color: #1d1e2c;
  text-align: right;
}

.radar-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.radar-placeholder {
  position: relative;
  width: 200px;
  height: 200px;
}

.radar-point {
  position: absolute;
  width: 32px;
  height: 32px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translate(-50%, -50%);
}

.radar-point span {
  font-size: 11px;
  color: #fff;
  font-weight: 600;
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

.activity-score {
  font-size: 12px;
  color: #909399;
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
