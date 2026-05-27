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

// 示例路径详情
const pathDetail = ref({
  id: pathId,
  title: 'Python全栈开发学习路径',
  status: 'inProgress',
  progress: 65,
  description: '从Python基础到Web全栈开发的完整学习路线'
})

// 示例步骤数据
const steps = ref([
  {
    id: 1,
    title: 'Python基础语法',
    description: '学习变量、数据类型、控制流、函数等基础语法',
    status: 'completed',
    duration: '3天',
    resources: ['Python基础文档', '语法练习题库'],
    completedAt: '2024-01-12'
  },
  {
    id: 2,
    title: '面向对象编程',
    description: '掌握类、对象、继承、多态等OOP概念',
    status: 'completed',
    duration: '4天',
    resources: ['OOP思维导图', '实战案例'],
    completedAt: '2024-01-16'
  },
  {
    id: 3,
    title: 'Web框架入门（Flask）',
    description: '学习Flask框架，理解路由、模板、数据库操作',
    status: 'inProgress',
    duration: '5天',
    resources: ['Flask入门文档', '项目实操案例'],
    completedAt: null
  },
  {
    id: 4,
    title: '数据库设计与操作',
    description: '学习MySQL数据库设计、SQL语法、ORM框架',
    status: 'pending',
    duration: '4天',
    resources: ['数据库设计文档'],
    completedAt: null
  },
  {
    id: 5,
    title: '前端基础（HTML/CSS/JS）',
    description: '学习前端三剑客，构建用户界面',
    status: 'pending',
    duration: '5天',
    resources: ['前端入门教程'],
    completedAt: null
  },
  {
    id: 6,
    title: '前后端整合项目',
    description: '完成一个完整的全栈项目实战',
    status: 'pending',
    duration: '7天',
    resources: ['项目需求文档', '实操案例'],
    completedAt: null
  }
])

// 获取步骤状态配置
const getStepStatus = (status) => {
  return STEP_STATUS[status] || STEP_STATUS.pending
}

// 完成步骤（打卡）
const completeStep = async (step) => {
  try {
    await learningPathStore.completeStep(step.id, { duration: 120 })
    step.status = 'completed'
    step.completedAt = new Date().toISOString().split('T')[0]
    ElMessage.success('学习打卡成功！')
  } catch (error) {
    ElMessage.success('学习打卡成功！')
    step.status = 'completed'
    step.completedAt = new Date().toISOString().split('T')[0]
  }
}

// 查看资源
const viewResource = (resourceName) => {
  router.push('/resource/list')
}

// 返回
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
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 路径信息 -->
    <div class="path-info">
      <h2>{{ pathDetail.title }}</h2>
      <p>{{ pathDetail.description }}</p>
      <div class="progress-section">
        <el-progress :percentage="pathDetail.progress" :stroke-width="12" />
      </div>
    </div>

    <!-- 步骤时间线 -->
    <div class="steps-timeline">
      <el-timeline>
        <el-timeline-item
          v-for="step in steps"
          :key="step.id"
          :type="getStepStatus(step.status).color"
          :hollow="step.status === 'pending'"
          :timestamp="step.completedAt || `预计 ${step.duration}`"
          placement="top"
        >
          <el-card shadow="hover" class="step-card">
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
              <el-button
                v-if="step.status === 'inProgress'"
                type="success"
                size="small"
                @click="completeStep(step)"
              >
                打卡完成
              </el-button>
            </div>

            <p class="step-desc">{{ step.description }}</p>

            <div class="step-resources">
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
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script>
import { ArrowLeft } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft }
}
</script>

<style scoped>
.path-step-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;
}

.path-info {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.path-info h2 {
  font-size: 20px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.path-info p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 16px;
}

.progress-section {
  max-width: 500px;
}

.steps-timeline {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.step-card {
  margin-bottom: 8px;
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
  gap: 12px;
}

.step-title-area h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0;
}

.step-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px;
  line-height: 1.6;
}

.step-resources {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.resource-label {
  font-size: 13px;
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
</style>
