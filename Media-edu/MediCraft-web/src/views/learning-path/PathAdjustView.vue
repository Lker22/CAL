<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { ElMessage } from 'element-plus'

const router = useRouter()
const learningPathStore = useLearningPathStore()

const loading = ref(false)
const stepsLoading = ref(false)
const selectedPath = ref('')
const adjustType = ref('extend')
const adjustReason = ref('')

// ========== 延长/压缩周期 ==========
const durationOptions = [
  { value: '1天', label: '1天' },
  { value: '5天', label: '5天' },
  { value: '7天', label: '7天' },
  { value: '14天', label: '2周' },
  { value: '1个月', label: '1个月' }
]
const selectedDuration = ref('7天')

// ========== 调整顺序 ==========
const reorderSteps = ref([])
const activeStepIndex = ref(null)

// ========== 新增内容 ==========
const showAddDialog = ref(false)
const newContentForm = ref({
  contentName: '',
  contentDetail: ''
})

// ========== 简化内容 ==========
const showRemoveDialog = ref(false)
const removeStepIds = ref([])

// ========== 通用 ==========
// 从store获取路径列表
const pathList = computed(() => {
  const raw = learningPathStore.paths
  if (Array.isArray(raw)) return raw
  if (raw && Array.isArray(raw.records)) return raw.records
  return []
})

// 当前选中路径的步骤
const currentSteps = computed(() => {
  if (adjustType.value === 'reorder' || adjustType.value === 'remove') {
    return reorderSteps.value
  }
  return []
})

// 调整原因占位符(根据类型动态变化)
const reasonPlaceholder = computed(() => {
  const map = {
    extend: '可选填，例如：最近工作忙，需要更多时间消化知识...',
    compress: '可选填，例如：考试临近，希望加快学习节奏...',
    reorder: '可选填，例如：想先学完数据库再学后端框架...',
    add: '可选填，例如：面试需要，补充这个知识点...',
    remove: '可选填，例如：这部分内容已掌握，可以跳过...'
  }
  return map[adjustType.value] || '请简要说明调整原因...'
})

// 调整类型
const adjustTypes = [
  { value: 'extend', label: '延长周期', desc: '学习节奏太快，需要更多时间' },
  { value: 'compress', label: '压缩周期', desc: '学习进度超前，希望加快节奏' },
  { value: 'reorder', label: '调整顺序', desc: '拖拽调整学习内容的先后顺序' },
  { value: 'add', label: '新增内容', desc: '补充额外的知识点到路径中' },
  { value: 'remove', label: '简化内容', desc: '移除不需要的学习步骤' }
]

// 切换路径时加载步骤
watch(selectedPath, async (newId) => {
  if (newId && (adjustType.value === 'reorder' || adjustType.value === 'remove')) {
    await loadPathSteps(newId)
  }
})

// 切换调整类型时，如果需要步骤就加载
watch(adjustType, async (newType) => {
  if (newType === 'reorder' || newType === 'remove') {
    if (selectedPath.value) {
      await loadPathSteps(selectedPath.value)
    }
  }
})

// 加载路径步骤
async function loadPathSteps(pathId) {
  stepsLoading.value = true
  try {
    const res = await learningPathStore.getPathDetail(pathId)
    const steps = learningPathStore.currentSteps || []
    reorderSteps.value = steps.map((s, i) => ({
      id: s.id,
      title: s.title || s.stepName,
      description: s.description || s.stepContent,
      status: s.status,
      sort: s.sort || (i + 1)
    }))
  } finally {
    stepsLoading.value = false
  }
}

// 步骤上移
function moveUp(index) {
  if (index <= 0) return
  const temp = reorderSteps.value[index]
  reorderSteps.value[index] = reorderSteps.value[index - 1]
  reorderSteps.value[index - 1] = temp
  // 更新sort
  reorderSteps.value.forEach((s, i) => { s.sort = i + 1 })
}

// 步骤下移
function moveDown(index) {
  if (index >= reorderSteps.value.length - 1) return
  const temp = reorderSteps.value[index]
  reorderSteps.value[index] = reorderSteps.value[index + 1]
  reorderSteps.value[index + 1] = temp
  reorderSteps.value.forEach((s, i) => { s.sort = i + 1 })
}

// 移到顶部
function moveTop(index) {
  if (index <= 0) return
  const item = reorderSteps.value.splice(index, 1)[0]
  reorderSteps.value.unshift(item)
  reorderSteps.value.forEach((s, i) => { s.sort = i + 1 })
}

// 移到底部
function moveBottom(index) {
  if (index >= reorderSteps.value.length - 1) return
  const item = reorderSteps.value.splice(index, 1)[0]
  reorderSteps.value.push(item)
  reorderSteps.value.forEach((s, i) => { s.sort = i + 1 })
}

// ========== 新增内容对话框 ==========
function openAddDialog() {
  newContentForm.value = { contentName: '', contentDetail: '' }
  showAddDialog.value = true
}

function confirmAddContent() {
  if (!newContentForm.value.contentName.trim()) {
    ElMessage.warning('请填写知识点名称')
    return
  }
  showAddDialog.value = false
  ElMessage.success('已添加新内容，请提交调整请求')
}

// ========== 简化内容对话框 ==========
function openRemoveDialog() {
  removeStepIds.value = []
  showRemoveDialog.value = true
}

function confirmRemoveContent() {
  if (removeStepIds.value.length === 0) {
    ElMessage.warning('请至少选择一个要移除的步骤')
    return
  }
  showRemoveDialog.value = false
  ElMessage.success(`已选择 ${removeStepIds.value.length} 个步骤待移除，请提交调整请求`)
}

// ========== 提交调整 ==========
function buildParams() {
  switch (adjustType.value) {
    case 'extend':
      return JSON.stringify({ duration: selectedDuration.value })
    case 'compress':
      return JSON.stringify({ duration: selectedDuration.value })
    case 'reorder':
      return JSON.stringify({
        steps: reorderSteps.value.map(s => ({ id: s.id, sort: s.sort }))
      })
    case 'add':
      return JSON.stringify({
        contentName: newContentForm.value.contentName,
        contentDetail: newContentForm.value.contentDetail
      })
    case 'remove':
      return JSON.stringify({
        removeStepIds: removeStepIds.value
      })
    default:
      return '{}'
  }
}

function buildAutoReason() {
  switch (adjustType.value) {
    case 'extend':
      return `延长学习周期 ${selectedDuration.value}`
    case 'compress':
      return `压缩学习周期 ${selectedDuration.value}`
    case 'reorder':
      return '调整学习步骤顺序'
    case 'add':
      return `新增知识点: ${newContentForm.value.contentName}`
    case 'remove':
      return `移除 ${removeStepIds.value.length} 个步骤`
    default:
      return adjustReason.value
  }
}

const handleAdjust = async () => {
  if (!selectedPath.value) {
    ElMessage.warning('请选择要调整的学习路径')
    return
  }

  // 新增内容校验
  if (adjustType.value === 'add' && !newContentForm.value.contentName.trim()) {
    ElMessage.warning('请先点击"新增内容"填写要添加的知识点')
    return
  }

  // 简化内容校验
  if (adjustType.value === 'remove' && removeStepIds.value.length === 0) {
    ElMessage.warning('请先点击"选择要移除的步骤"选择内容')
    return
  }

  loading.value = true
  try {
    const autoReason = buildAutoReason()
    const finalReason = adjustReason.value.trim() || autoReason

    const response = await learningPathStore.adjustPath(selectedPath.value, {
      adjustmentType: adjustType.value,
      reason: finalReason,
      params: buildParams()
    })
    ElMessage.success('路径调整成功')
    router.push('/path/list')
  } catch (error) {
    // 错误提示已在 Axios 拦截器中显示，此处只做日志记录
    console.error('[PathAdjust] 调整失败详情:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (pathList.value.length === 0) {
    await learningPathStore.getPaths()
  }
  if (pathList.value.length > 0 && !selectedPath.value) {
    selectedPath.value = String(pathList.value[0].id)
  }
})
</script>

<template>
  <div class="path-adjust-page">
    <div class="page-hero">
      <h2>学习路径调整</h2>
      <p>根据你的学习情况，动态调整学习路径 · AI 智能重规划</p>
    </div>

    <div class="adjust-card">
      <el-form label-width="100px">
        <!-- 选择路径 -->
        <el-form-item label="选择路径">
          <el-select v-model="selectedPath" placeholder="请选择要调整的学习路径" style="width: 400px">
            <el-option
              v-for="path in pathList"
              :key="path.id"
              :label="path.title || path.pathName"
              :value="String(path.id)"
            />
          </el-select>
        </el-form-item>

        <!-- 调整类型 -->
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

        <!-- ====== 延长/压缩周期：选择具体时长 ====== -->
        <el-form-item v-if="adjustType === 'extend' || adjustType === 'compress'" label="调整时长">
          <div class="duration-options">
            <div
              v-for="opt in durationOptions"
              :key="opt.value"
              class="duration-chip"
              :class="{ active: selectedDuration === opt.value }"
              @click="selectedDuration = opt.value"
            >
              {{ adjustType === 'extend' ? '+' : '-' }}{{ opt.label }}
            </div>
          </div>
          <div class="duration-hint">
            当前选择：{{ adjustType === 'extend' ? '延长' : '压缩' }} <strong>{{ selectedDuration }}</strong>
          </div>
        </el-form-item>

        <!-- ====== 调整顺序：显示步骤列表，可上下移动 ====== -->
        <el-form-item v-if="adjustType === 'reorder'" label="步骤排序">
          <div v-loading="stepsLoading" class="reorder-list">
            <div v-if="reorderSteps.length === 0" class="empty-steps">
              暂无步骤数据
            </div>
            <div
              v-for="(step, index) in reorderSteps"
              :key="step.id"
              class="reorder-item"
              :class="{ dragging: activeStepIndex === index }"
            >
              <div class="step-index">{{ index + 1 }}</div>
              <div class="step-info">
                <div class="step-name">{{ step.title }}</div>
                <div class="step-desc">{{ step.description }}</div>
              </div>
              <div class="step-actions">
                <el-button
                  size="small"
                  text
                  type="primary"
                  :disabled="index === 0"
                  @click="moveTop(index)"
                >
                  置顶
                </el-button>
                <el-button
                  size="small"
                  text
                  :disabled="index === 0"
                  @click="moveUp(index)"
                >
                  <el-icon><Top /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  text
                  :disabled="index === reorderSteps.length - 1"
                  @click="moveDown(index)"
                >
                  <el-icon><Bottom /></el-icon>
                </el-button>
                <el-button
                  size="small"
                  text
                  type="primary"
                  :disabled="index === reorderSteps.length - 1"
                  @click="moveBottom(index)"
                >
                  置底
                </el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- ====== 新增内容：点击按钮弹出对话框 ====== -->
        <el-form-item v-if="adjustType === 'add'" label="新增内容">
          <div class="add-section">
            <el-button type="primary" plain @click="openAddDialog">
              <el-icon><Plus /></el-icon>
              新增内容
            </el-button>
            <div v-if="newContentForm.contentName" class="added-preview">
              <el-tag type="success" closable @close="newContentForm.contentName = ''">
                {{ newContentForm.contentName }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- ====== 简化内容：点击按钮弹出对话框 ====== -->
        <el-form-item v-if="adjustType === 'remove'" label="选择步骤">
          <div class="remove-section">
            <el-button type="danger" plain @click="openRemoveDialog">
              <el-icon><Delete /></el-icon>
              选择要移除的步骤
            </el-button>
            <div v-if="removeStepIds.length > 0" class="removed-preview">
              <el-tag type="danger">已选择 {{ removeStepIds.length }} 个步骤待移除</el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 调整原因 -->
        <el-form-item label="调整原因">
          <el-input
            v-model="adjustReason"
            type="textarea"
            :rows="3"
            :placeholder="reasonPlaceholder"
          />
        </el-form-item>

        <!-- 提交 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleAdjust" size="large">
            提交调整请求
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ====== 新增内容对话框 ====== -->
    <el-dialog v-model="showAddDialog" title="新增学习内容" width="500px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="知识点名称" required>
          <el-input
            v-model="newContentForm.contentName"
            placeholder="例如：Python装饰器、MySQL索引优化..."
          />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input
            v-model="newContentForm.contentDetail"
            type="textarea"
            :rows="4"
            placeholder="描述你想学习的具体内容和要求..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddContent">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- ====== 简化内容对话框 ====== -->
    <el-dialog v-model="showRemoveDialog" title="选择要移除的步骤" width="550px" :close-on-click-modal="false">
      <div v-loading="stepsLoading" class="remove-step-list">
        <el-checkbox-group v-model="removeStepIds">
          <div
            v-for="step in reorderSteps"
            :key="step.id"
            class="remove-step-item"
          >
            <el-checkbox :value="step.id" :label="step.id">
              <div class="remove-step-info">
                <span class="remove-step-name">{{ step.title }}</span>
                <span class="remove-step-desc">{{ step.description }}</span>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="showRemoveDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmRemoveContent">
          确认移除 ({{ removeStepIds.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Top, Bottom, Plus, Delete } from '@element-plus/icons-vue'
export default {
  components: { Top, Bottom, Plus, Delete }
}
</script>

<style scoped>
.path-adjust-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 0;
}

/* 标题区 */
.page-hero {
  text-align: center;
  margin-bottom: 28px;
  padding: 28px 0 20px;
}

.page-hero h2 {
  font-size: 26px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.page-hero p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.adjust-card {
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  border: 1px solid #f0f0f0;
}

/* 调整类型卡片 */
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

/* 延长/压缩时长选项 */
.duration-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.duration-chip {
  padding: 8px 20px;
  border: 2px solid #e4e7ed;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
  user-select: none;
}

.duration-chip:hover {
  border-color: #409eff;
  color: #409eff;
}

.duration-chip.active {
  border-color: #409eff;
  background: #409eff;
  color: #fff;
}

.duration-hint {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}

.duration-hint strong {
  color: #409eff;
  font-size: 15px;
}

/* 调整顺序列表 */
.reorder-list {
  width: 100%;
  min-height: 100px;
}

.empty-steps {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 0;
}

.reorder-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.3s;
}

.reorder-item:hover {
  background: #f0f7ff;
  border-color: #c6e2ff;
}

.step-index {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-info {
  flex: 1;
  min-width: 0;
}

.step-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d1e2c;
  margin-bottom: 2px;
}

.step-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.step-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

/* 新增内容 */
.add-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.added-preview {
  display: flex;
  gap: 8px;
}

/* 简化内容 */
.remove-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.removed-preview {
  display: flex;
  gap: 8px;
}

/* 简化内容对话框 */
.remove-step-list {
  max-height: 400px;
  overflow-y: auto;
}

.remove-step-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.remove-step-item:hover {
  background: #fafafa;
}

.remove-step-item:last-child {
  border-bottom: none;
}

.remove-step-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.remove-step-name {
  font-size: 14px;
  color: #1d1e2c;
}

.remove-step-desc {
  font-size: 12px;
  color: #909399;
}
</style>
