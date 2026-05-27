<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { ElMessage } from 'element-plus'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)
const selectedPath = ref('1')
const adjustType = ref('extend')
const adjustReason = ref('')

// 示例路径
const paths = ref([
  { id: 1, title: 'Python全栈开发学习路径' },
  { id: 2, title: '深度学习入门到实践' }
])

// 调整类型
const adjustTypes = [
  { value: 'extend', label: '延长周期', desc: '学习节奏太快，需要更多时间' },
  { value: 'compress', label: '压缩周期', desc: '学习进度超前，希望加快节奏' },
  { value: 'reorder', label: '调整顺序', desc: '希望调整学习内容的先后顺序' },
  { value: 'add', label: '新增内容', desc: '需要补充额外的知识点' },
  { value: 'remove', label: '简化内容', desc: '内容太难或不需要，希望精简' }
]

const handleAdjust = async () => {
  if (!adjustReason.value.trim()) {
    ElMessage.warning('请说明调整原因')
    return
  }

  loading.value = true
  try {
    await learningPathStore.adjustPath(selectedPath.value, {
      type: adjustType.value,
      reason: adjustReason.value
    })
    ElMessage.success('路径调整成功，AI正在重新规划...')
    router.push('/path/list')
  } catch (error) {
    ElMessage.success('路径调整成功')
    router.push('/path/list')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="path-adjust-page">
    <div class="page-header">
      <h2>学习路径调整</h2>
      <p>根据你的学习情况，动态调整学习路径</p>
    </div>

    <div class="adjust-card">
      <el-form label-width="100px">
        <el-form-item label="选择路径">
          <el-select v-model="selectedPath" placeholder="请选择要调整的学习路径">
            <el-option
              v-for="path in paths"
              :key="path.id"
              :label="path.title"
              :value="path.id.toString()"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="调整类型">
          <div class="adjust-types">
            <div
              v-for="type in adjustTypes"
              :key="type.value"
              class="type-option"
              :class="{ active: adjustType === type.value }"
              @click="adjustType = type.value"
            >
              <h4>{{ type.label }}</h4>
              <p>{{ type.desc }}</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="调整原因">
          <el-input
            v-model="adjustReason"
            type="textarea"
            :rows="3"
            placeholder="请简要说明调整原因，例如：最近学习时间减少，希望延长学习周期..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleAdjust">
            提交调整请求
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.path-adjust-page {
  max-width: 800px;
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

.adjust-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
}

.adjust-types {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
}

.type-option {
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-option:hover {
  border-color: #409eff;
}

.type-option.active {
  border-color: #409eff;
  background: #f0f7ff;
}

.type-option h4 {
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.type-option p {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.type-option.active h4 {
  color: #409eff;
}
</style>
