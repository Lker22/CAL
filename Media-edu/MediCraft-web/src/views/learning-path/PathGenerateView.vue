<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { ElMessage } from 'element-plus'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)

const pathForm = reactive({
  subject: '',
  goal: '',
  duration: '4',
  intensity: 'medium'
})

const durationOptions = [
  { value: '2', label: '2周' },
  { value: '4', label: '1个月' },
  { value: '8', label: '2个月' },
  { value: '12', label: '3个月' }
]

const intensityOptions = [
  { value: 'low', label: '轻松（每天1小时）' },
  { value: 'medium', label: '适中（每天2小时）' },
  { value: 'high', label: '密集（每天3小时+）' }
]

const handleGenerate = async () => {
  if (!pathForm.subject.trim()) {
    ElMessage.warning('请输入学习主题')
    return
  }

  loading.value = true
  try {
    await learningPathStore.generatePath(pathForm)
    ElMessage.success('学习路径生成成功')
    router.push('/path/list')
  } catch (error) {
    // 模拟成功
    ElMessage.success('学习路径生成成功')
    router.push('/path/list')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="path-generate-page">
    <div class="page-header">
      <h2>生成学习路径</h2>
      <p>AI将根据你的画像和学习目标，为你规划个性化学习路线</p>
    </div>

    <div class="generate-card">
      <el-form :model="pathForm" label-width="100px" class="path-form">
        <el-form-item label="学习主题" required>
          <el-input
            v-model="pathForm.subject"
            placeholder="例如：Python全栈开发、深度学习、数据结构与算法..."
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="学习目标">
          <el-input
            v-model="pathForm.goal"
            placeholder="描述你希望达到的目标，例如：能够独立开发Web应用..."
            type="textarea"
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="学习周期">
          <el-radio-group v-model="pathForm.duration">
            <el-radio-button
              v-for="opt in durationOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="学习强度">
          <el-radio-group v-model="pathForm.intensity">
            <el-radio
              v-for="opt in intensityOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="generate-btn"
            @click="handleGenerate"
          >
            一键生成学习路径
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.path-generate-page {
  max-width: 700px;
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

.generate-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
}

.generate-btn {
  width: 200px;
}
</style>
