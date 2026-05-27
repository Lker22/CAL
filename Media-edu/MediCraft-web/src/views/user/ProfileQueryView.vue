<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useProfileStore } from '@/stores/profile'
import { ElMessage } from 'element-plus'
import { PROFILE_DIMENSIONS } from '@/utils/constants'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'

const profileStore = useProfileStore()
const loading = ref(false)
const editMode = ref(false)

// 可编辑的画像数据
const editForm = reactive({
  knowledgeBase: '',
  cognitiveStyle: '',
  learningGoals: '',
  errorPoints: '',
  learningRhythm: '',
  resourcePreference: ''
})

// 加载画像
const loadProfile = async () => {
  loading.value = true
  try {
    await profileStore.getProfile()
  } catch {
    // 使用示例数据
    profileStore.profile = {
      knowledgeBase: '具备计算机科学基础知识，掌握Python和Java编程，了解数据结构与算法基础。',
      cognitiveStyle: '偏向视觉学习型，喜欢通过图表和视频来理解新概念。',
      learningGoals: '短期：掌握深度学习基础；中期：完成AI项目实践；长期：成为AI研发人才。',
      errorPoints: '数学推导能力较弱，概率统计和线性代数应用不够熟练。',
      learningRhythm: '每天2-3小时，周末4-5小时，上午学习效率最高。',
      resourcePreference: '偏好视频教程配合文档阅读，喜欢带有代码示例的实操型资源。'
    }
  } finally {
    loading.value = false
  }
}

// 进入编辑模式
const startEdit = () => {
  const profile = profileStore.profile
  Object.keys(editForm).forEach((key) => {
    editForm[key] = profile?.[key] || ''
  })
  editMode.value = true
}

// 取消编辑
const cancelEdit = () => {
  editMode.value = false
}

// 保存编辑
const saveEdit = async () => {
  loading.value = true
  try {
    await profileStore.updateProfile(editForm)
    ElMessage.success('画像更新成功')
    editMode.value = false
  } catch (error) {
    ElMessage.error('更新失败，请重试')
  } finally {
    loading.value = false
  }
}

// 维度配置列表
const dimensionList = Object.values(PROFILE_DIMENSIONS)

onMounted(() => {
  loadProfile()
})
</script>

<template>
  <div class="profile-query-page">
    <LoadingOverlay :visible="loading" text="处理中..." />

    <div class="page-header">
      <div class="header-left">
        <h2>画像查询与更新</h2>
        <p>查看和管理你的学习画像数据</p>
      </div>
      <div class="header-right">
        <el-button v-if="!editMode" type="primary" @click="startEdit">
          编辑画像
        </el-button>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveEdit">保存修改</el-button>
        </template>
      </div>
    </div>

    <!-- 查看模式 -->
    <div v-if="!editMode" class="profile-detail">
      <div
        v-for="dim in dimensionList"
        :key="dim.key"
        class="detail-card"
      >
        <div class="detail-header">
          <div class="detail-icon" :style="{ backgroundColor: dim.color + '15' }">
            <el-icon :size="20" :color="dim.color">
              <component :is="dim.icon" />
            </el-icon>
          </div>
          <h4>{{ dim.label }}</h4>
        </div>
        <div class="detail-body">
          <p class="detail-desc">{{ dim.description }}</p>
          <div class="detail-value">
            {{ profileStore.profile?.[dim.key] || '暂无数据' }}
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑模式 -->
    <div v-else class="profile-edit">
      <el-form label-position="top" class="edit-form">
        <el-form-item
          v-for="dim in dimensionList"
          :key="dim.key"
          :label="dim.label"
        >
          <el-input
            v-model="editForm[dim.key]"
            type="textarea"
            :rows="3"
            :placeholder="dim.description"
          />
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据说明 -->
    <div class="data-info">
      <h4>数据说明</h4>
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">固定信息</span>
          <span class="info-value">基本学习背景和学科信息</span>
        </div>
        <div class="info-item">
          <span class="info-label">动态行为</span>
          <span class="info-value">学习时长、频率、资源使用情况</span>
        </div>
        <div class="info-item">
          <span class="info-label">认知能力</span>
          <span class="info-value">认知风格、理解能力、记忆特点</span>
        </div>
        <div class="info-item">
          <span class="info-label">成长趋势</span>
          <span class="info-value">学习进步、能力变化、目标达成</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-query-page {
  position: relative;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.header-left p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.profile-detail {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;
}

.detail-header h4 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
}

.detail-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-body {
  padding: 16px 20px;
}

.detail-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.profile-edit {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #1d1e2c;
}

.data-info {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.data-info h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0 0 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.info-label {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.info-value {
  font-size: 13px;
  color: #606266;
}
</style>
