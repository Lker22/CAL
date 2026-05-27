<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import { AI_AGENTS } from '@/utils/constants'
import { ElMessage } from 'element-plus'

const router = useRouter()
const resourceStore = useResourceStore()

const loading = ref(false)

// 加载智能体列表
const loadAgents = async () => {
  loading.value = true
  try {
    await resourceStore.getAgents()
  } catch {
    // 使用本地配置的智能体数据
    resourceStore.agents = AI_AGENTS
  } finally {
    loading.value = false
  }
}

// 选择智能体并跳转生成页面
const selectAgent = (agent) => {
  resourceStore.selectAgent(agent)
  router.push({ path: '/resource/generate', query: { agentId: agent.id } })
}

// 智能体列表（优先使用store数据，否则使用常量配置）
const agentList = AI_AGENTS

onMounted(() => {
  loadAgents()
})
</script>

<template>
  <div class="agent-list-page">
    <div class="page-header">
      <h2>AI智能体中心</h2>
      <p>选择智能体，为你生成个性化学习资源</p>
    </div>

    <div class="agents-grid">
      <div
        v-for="agent in agentList"
        :key="agent.id"
        class="agent-card"
        @click="selectAgent(agent)"
      >
        <div class="agent-icon" :style="{ backgroundColor: agent.color + '15' }">
          <el-icon :size="32" :color="agent.color">
            <component :is="agent.icon" />
          </el-icon>
        </div>
        <h4 class="agent-name">{{ agent.name }}</h4>
        <p class="agent-desc">{{ agent.description }}</p>
        <div class="agent-action">
          <el-button type="primary" text>
            开始使用
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowRight } from '@element-plus/icons-vue'
export default {
  components: { ArrowRight }
}
</script>

<style scoped>
.agent-list-page {
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

.agents-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.agent-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.agent-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.agent-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.agent-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0 0 16px;
  min-height: 42px;
}

.agent-action {
  text-align: right;
}

@media (max-width: 1200px) {
  .agents-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
