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
const timer = ref(null)

// 生成步骤
const steps = ref([
  { name: '需求分析', status: 'completed', time: '00:05', description: '解析学习需求与知识图谱' },
  { name: '内容规划', status: 'completed', time: '00:12', description: '规划资源结构与内容大纲' },
  { name: 'AI生成中', status: 'processing', time: '', description: '智能体协同生成资源内容' },
  { name: '质量校验', status: 'pending', time: '', description: '内容质量检查与格式优化' },
  { name: '完成发布', status: 'pending', time: '', description: '资源入库并发布' }
])

// 状态文本
const statusText = computed(() => {
  const config = GENERATION_STATUS[status.value]
  return config ? config.label : '生成中'
})

// 轮询生成进度
const pollProgress = () => {
  let currentProgress = 0

  timer.value = setInterval(() => {
    currentProgress += Math.random() * 8

    if (currentProgress >= 100) {
      currentProgress = 100
      progress.value = 100
      status.value = 'completed'

      // 更新所有步骤为已完成
      steps.value.forEach((step) => {
        step.status = 'completed'
        if (!step.time) step.time = '00:30'
      })

      clearInterval(timer.value)
      ElMessage.success('资源生成完成！')
      return
    }

    progress.value = Math.round(currentProgress)

    // 更新步骤状态
    if (currentProgress > 20 && steps.value[1].status !== 'completed') {
      steps.value[1].status = 'completed'
    }
    if (currentProgress > 40) {
      steps.value[2].status = 'processing'
    }
    if (currentProgress > 70) {
      steps.value[2].status = 'completed'
      steps.value[3].status = 'processing'
    }
    if (currentProgress > 90) {
      steps.value[3].status = 'completed'
      steps.value[4].status = 'processing'
    }
  }, 1000)
}

// 查看资源
const viewResource = () => {
  router.push('/resource/list')
}

// 继续生成
const generateMore = () => {
  router.push('/resource/generate')
}

onMounted(() => {
  pollProgress()
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<template>
  <div class="generation-progress-page">
    <div class="page-header">
      <h2>资源生成进度</h2>
      <p>AI智能体正在为你生成个性化学习资源</p>
    </div>

    <div class="progress-card">
      <!-- 动画图标 -->
      <div class="animation-area">
        <div class="generating-orb" :class="{ completed: status === 'completed' }">
          <el-icon :size="48" color="#fff">
            <component :is="status === 'completed' ? 'Check' : 'Cpu'" />
          </el-icon>
        </div>
        <div v-if="status !== 'completed'" class="orbit">
          <div class="orbit-dot" />
          <div class="orbit-dot delay-1" />
          <div class="orbit-dot delay-2" />
        </div>
      </div>

      <!-- 进度追踪 -->
      <ProgressTracker
        :progress="progress"
        :status="status"
        :status-text="statusText"
        :show-steps="true"
        :steps="steps"
      />

      <!-- 操作按钮 -->
      <div v-if="status === 'completed'" class="action-area">
        <el-button type="primary" size="large" @click="viewResource">
          查看资源
        </el-button>
        <el-button size="large" @click="generateMore">
          继续生成
        </el-button>
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="tips-card">
      <h4>温馨提示</h4>
      <ul>
        <li>资源生成过程可能需要1-3分钟，请耐心等待</li>
        <li>生成过程中请勿关闭页面，否则进度将丢失</li>
        <li>生成完成后，你可以在"我的资源"中查看和管理</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { Check } from '@element-plus/icons-vue'
export default {
  components: { Check }
}
</script>

<style scoped>
.generation-progress-page {
  max-width: 700px;
  margin: 0 auto;
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
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
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
