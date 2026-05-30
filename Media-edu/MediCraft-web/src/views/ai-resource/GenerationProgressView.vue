<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import ProgressTracker from '@/components/common/ProgressTracker.vue'
import { GENERATION_STATUS } from '@/utils/constants'
import { ElMessage } from 'element-plus'

const router = useRouter()
const resourceStore = useResourceStore()

const progress = ref(0)
const status = ref('processing')
const errorMsg = ref('')
const timer = ref(null)

const steps = ref([
  { name: '需求分析', status: 'processing', time: '', description: '解析学习需求与知识图谱' },
  { name: '内容规划', status: 'pending', time: '', description: '规划资源结构与内容大纲' },
  { name: 'AI生成中', status: 'pending', time: '', description: '智能体协同生成资源内容' },
  { name: '质量校验', status: 'pending', time: '', description: '内容质量检查与格式优化' },
  { name: '完成发布', status: 'pending', time: '', description: '资源入库并发布' }
])

// 当前任务（优先从 store 取，支持页面刷新后从 localStorage 恢复）
const currentTask = computed(() => resourceStore.currentTask)

// 状态文本
const statusText = computed(() => {
  if (status.value === 'failed') return '生成失败'
  const config = GENERATION_STATUS[status.value]
  return config ? config.label : '生成中'
})

// 从后端轮询真实进度
const pollFromBackend = async () => {
  const task = currentTask.value
  if (!task || !task.taskId) return false

  try {
    await resourceStore.getGenerationProgress(task.taskId)
    const updatedTask = resourceStore.currentTask
    if (updatedTask) {
      progress.value = updatedTask.progress || 0
      // 后端 status 是 String: "pending" / "running" / "success" / "failed"
      const taskStatus = updatedTask.status
      if (taskStatus === 'success') {
        status.value = 'completed'
        steps.value.forEach((s) => { s.status = 'completed'; if (!s.time) s.time = '00:30' })
        ElMessage.success('资源生成完成！')
        return true
      } else if (taskStatus === 'failed') {
        status.value = 'failed'
        errorMsg.value = updatedTask.errorMsg || '未知错误'
        ElMessage.error('资源生成失败：' + errorMsg.value)
        return true
      }
      // pending 或 running → 继续轮询，根据进度更新步骤
      updateStepsByProgress(progress.value)
    }
  } catch {
    // 后端不可用
  }
  return false
}

const updateStepsByProgress = (p) => {
  if (p >= 20) { steps.value[0].status = 'completed'; steps.value[0].time = '00:10' }
  if (p >= 50) { steps.value[1].status = 'completed'; steps.value[1].time = '00:20' }
  if (p >= 80) { steps.value[2].status = 'completed'; steps.value[2].time = '00:25'; steps.value[3].status = 'processing' }
  if (p >= 100) { steps.value[3].status = 'completed'; steps.value[3].time = '00:30'; steps.value[4].status = 'completed'; steps.value[4].time = '00:35' }
}

// 开始轮询
const startPolling = () => {
  // 立即检查一次
  pollFromBackend().then((done) => {
    if (done) return
    // 每2秒轮询后端
    timer.value = setInterval(async () => {
      const backendDone = await pollFromBackend()
      if (backendDone) {
        clearInterval(timer.value)
      }
    }, 2000)
  })
}

// 查看资源（生成成功后）
const viewResource = () => {
  resourceStore.clearCurrentTask()
  router.push('/resource/list')
}

// 继续生成
const generateMore = () => {
  resourceStore.clearCurrentTask()
  router.push('/agent/list')
}

// 返回重试
const goBack = () => {
  router.push('/resource/generate')
}

// 没有任务时的处理
const hasTask = computed(() => !!currentTask.value && !!currentTask.value.taskId)

onMounted(() => {
  if (hasTask.value) {
    startPolling()
  }
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<template>
  <div class="generation-progress-page">
    <!-- 没有进行中的任务 -->
    <div v-if="!hasTask" class="no-task-card">
      <el-empty description="暂无进行中的生成任务">
        <el-button type="primary" @click="router.push('/agent/list')">
          去生成资源
        </el-button>
        <el-button @click="router.push('/resource/list')">
          查看我的资源
        </el-button>
      </el-empty>
    </div>

    <!-- 有任务 -->
    <template v-else>
      <div class="page-header">
        <h2>资源生成进度</h2>
        <p v-if="status === 'processing'">AI智能体正在为你生成个性化学习资源</p>
        <p v-else-if="status === 'completed'" class="success-text">资源已生成完成！</p>
        <p v-else-if="status === 'failed'" class="error-text">资源生成遇到问题</p>
      </div>

      <div class="progress-card">
        <div class="animation-area">
          <div class="generating-orb" :class="{ completed: status === 'completed', failed: status === 'failed' }">
            <el-icon :size="48" color="#fff">
              <component :is="status === 'completed' ? 'Check' : status === 'failed' ? 'Close' : 'Cpu'" />
            </el-icon>
          </div>
          <div v-if="status === 'processing'" class="orbit">
            <div class="orbit-dot" />
            <div class="orbit-dot delay-1" />
            <div class="orbit-dot delay-2" />
          </div>
        </div>

        <!-- 失败提示 -->
        <div v-if="status === 'failed'" class="error-box">
          <el-icon color="#f56c6c"><WarningFilled /></el-icon>
          <span>{{ errorMsg || '资源生成失败，请重试' }}</span>
        </div>

        <ProgressTracker
          :progress="progress"
          :status="status"
          :status-text="statusText"
          :show-steps="true"
          :steps="steps"
        />

        <div class="action-area">
          <!-- 生成成功 -->
          <template v-if="status === 'completed'">
            <el-button type="primary" size="large" @click="viewResource">
              查看资源
            </el-button>
            <el-button size="large" @click="generateMore">
              继续生成
            </el-button>
          </template>

          <!-- 生成失败 -->
          <template v-else-if="status === 'failed'">
            <el-button type="primary" size="large" @click="goBack">
              重新生成
            </el-button>
            <el-button size="large" @click="router.push('/resource/list')">
              查看已有资源
            </el-button>
          </template>
        </div>
      </div>

      <div class="tips-card">
        <h4>温馨提示</h4>
        <ul>
          <li>资源生成过程可能需要1-3分钟，请耐心等待</li>
          <li>生成过程中可以切换页面，进度不会丢失</li>
          <li>生成完成后，你可以在"我的资源"中查看和管理</li>
        </ul>
      </div>
    </template>
  </div>
</template>

<script>
import { Check, Close, WarningFilled } from '@element-plus/icons-vue'
export default {
  components: { Check, Close, WarningFilled }
}
</script>

<style scoped>
.generation-progress-page {
  max-width: 700px;
  margin: 0 auto;
}

.no-task-card {
  background: #fff;
  border-radius: 12px;
  padding: 60px 24px;
  text-align: center;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
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

.page-header .success-text {
  color: #67c23a;
}

.page-header .error-text {
  color: #f56c6c;
}

.progress-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 24px;
  text-align: center;
  margin-bottom: 20px;
}

.animation-area {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
}

.generating-orb {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: background 0.5s;
}

.generating-orb.completed {
  background: linear-gradient(135deg, #67c23a, #409eff);
}

.generating-orb.failed {
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
}

.orbit {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  animation: rotate 3s linear infinite;
}

.orbit-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.orbit-dot.delay-1 {
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  background: #67c23a;
}

.orbit-dot.delay-2 {
  top: auto;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background: #e6a23c;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 14px;
  margin-bottom: 20px;
}

.action-area {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.tips-card {
  background: #f0f7ff;
  border-radius: 12px;
  padding: 20px;
}

.tips-card h4 {
  font-size: 14px;
  color: #409eff;
  margin: 0 0 8px;
}

.tips-card ul {
  margin: 0;
  padding-left: 20px;
}

.tips-card li {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}
</style>
