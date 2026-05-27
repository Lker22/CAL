<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useResourceStore } from '@/stores/resource'
import { ElMessage } from 'element-plus'
import { AI_AGENTS, RESOURCE_TYPES } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const resourceStore = useResourceStore()

const loading = ref(false)

// 当前选择的智能体
const currentAgent = computed(() => {
  const agentId = route.query.agentId || resourceStore.selectedAgent?.id
  return AI_AGENTS.find((a) => a.id === agentId) || AI_AGENTS[0]
})

// 生成表单
const generateForm = reactive({
  topic: '',
  resourceType: '',
  difficulty: 'medium',
  language: 'zh',
  additionalRequirements: ''
})

// 资源类型选项
const resourceTypeOptions = Object.values(RESOURCE_TYPES)

// 难度选项
const difficultyOptions = [
  { value: 'easy', label: '入门级' },
  { value: 'medium', label: '进阶级' },
  { value: 'hard', label: '挑战级' }
]

// 智能体支持的资源类型映射
const agentResourceTypes = {
  requirement: ['document', 'mindmap'],
  document: ['document'],
  mindmap: ['mindmap'],
  quiz: ['quiz'],
  practice: ['practice'],
  multimodal: ['videoScript', 'document']
}

// 当前智能体支持的资源类型
const availableTypes = computed(() => {
  const types = agentResourceTypes[currentAgent.value.id] || ['document']
  return resourceTypeOptions.filter((t) => types.includes(t.key))
})

// 生成资源
const handleGenerate = async () => {
  if (!generateForm.topic.trim()) {
    ElMessage.warning('请输入学习主题')
    return
  }
  if (!generateForm.resourceType) {
    ElMessage.warning('请选择资源类型')
    return
  }

  loading.value = true
  try {
    await resourceStore.generateResource({
      agentId: currentAgent.value.id,
      ...generateForm
    })
    ElMessage.success('资源生成任务已提交')
    router.push('/generation/progress')
  } catch (error) {
    // 模拟成功（开发阶段）
    ElMessage.success('资源生成任务已提交')
    router.push('/generation/progress')
  } finally {
    loading.value = false
  }
}

// 返回智能体列表
const goBack = () => {
  router.push('/agent/list')
}
</script>

<template>
  <div class="resource-generate-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回智能体列表
      </el-button>
    </div>

    <!-- 智能体信息 -->
    <div class="agent-info">
      <div class="agent-icon" :style="{ backgroundColor: currentAgent.color + '15' }">
        <el-icon :size="28" :color="currentAgent.color">
          <component :is="currentAgent.icon" />
        </el-icon>
      </div>
      <div class="agent-detail">
        <h3>{{ currentAgent.name }}</h3>
        <p>{{ currentAgent.description }}</p>
      </div>
    </div>

    <!-- 生成表单 -->
    <div class="generate-form-card">
      <h4>配置生成参数</h4>
      <el-form :model="generateForm" label-width="100px" class="generate-form">
        <el-form-item label="学习主题" required>
          <el-input
            v-model="generateForm.topic"
            placeholder="请输入你想学习的主题，如：深度学习基础、Python数据分析..."
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="资源类型" required>
          <div class="type-cards">
            <div
              v-for="type in availableTypes"
              :key="type.key"
              class="type-card"
              :class="{ active: generateForm.resourceType === type.key }"
              @click="generateForm.resourceType = type.key"
            >
              <el-icon :size="24" :color="type.color">
                <component :is="type.icon" />
              </el-icon>
              <span>{{ type.label }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="难度等级">
          <el-radio-group v-model="generateForm.difficulty">
            <el-radio-button
              v-for="opt in difficultyOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="补充要求">
          <el-input
            v-model="generateForm.additionalRequirements"
            placeholder="如有特殊要求，请在此补充说明..."
            type="textarea"
            :rows="3"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="generate-btn"
            @click="handleGenerate"
          >
            开始生成资源
          </el-button>
        </el-form-item>
      </el-form>
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
.resource-generate-page {
  max-width: 800px;
}

.page-header {
  margin-bottom: 16px;
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
}

.agent-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-detail h3 {
  font-size: 17px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.agent-detail p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.generate-form-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.generate-form-card h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 20px;
}

.type-cards {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.type-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
  color: #606266;
}

.type-card:hover {
  border-color: #409eff;
}

.type-card.active {
  border-color: #409eff;
  background: #f0f7ff;
  color: #409eff;
}

.generate-btn {
  width: 200px;
}
</style>
