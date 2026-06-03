<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { resourceApi } from '@/api/resource'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const learningPathStore = useLearningPathStore()

const stepId = route.params.stepId
const pathId = route.query.pathId
const stepType = route.query.stepType || 'document'
const stepTitle = route.query.title || '学习资源'

const loading = ref(true)
const resourceStatus = ref('idle') // idle | generating | cached | error
const resource = ref(null)
const taskId = ref(null)
const progress = ref(0)
const statusText = ref('')
let pollTimer = null

// ========== 测验状态 ==========
const quizPhase = ref('answering') // answering | results
const quizList = ref([])
const userAnswers = ref({})
const questionTimers = ref({})
const quizResult = ref(null)

// 解析题库内容为题目数组
function parseQuizContent(content) {
  if (!content) return []
  try {
    let arr = JSON.parse(content)
    if (Array.isArray(arr)) return arr
    return [arr]
  } catch {
    const match = content.match(/```(?:json)?\s*([\s\S]*?)```/)
    if (match) {
      try {
        let arr = JSON.parse(match[1].trim())
        if (Array.isArray(arr)) return arr
        return [arr]
      } catch { /* fall through */ }
    }
    return [{ id: 1, title: content, options: [], answer: '', analysis: '' }]
  }
}

// 加载/生成资源
async function initResource() {
  loading.value = true
  try {
    const result = await learningPathStore.generateStepResource(stepId)
    if (result.status === 'cached') {
      resource.value = result.resource
      resourceStatus.value = 'cached'
      if (stepType === 'quiz') {
        quizList.value = parseQuizContent(result.resource.resourceContent)
      }
    } else if (result.status === 'generating') {
      taskId.value = result.taskId
      resourceStatus.value = 'generating'
      statusText.value = '正在生成学习资源...'
      startPolling()
    }
  } catch (error) {
    resourceStatus.value = 'error'
    ElMessage.error('生成资源失败，请重试')
  } finally {
    loading.value = false
  }
}

// 轮询生成进度
function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!taskId.value) return
    try {
      const response = await resourceApi.getGenerationProgress(taskId.value)
      const task = response.data || response
      progress.value = task.progress || 0

      if (task.status === 'success') {
        stopPolling()
        statusText.value = '生成完成！'
        // 获取资源详情
        if (task.resourceId) {
          const resDetail = await resourceApi.getResourceDetail(task.resourceId)
          resource.value = resDetail.data || resDetail
          resourceStatus.value = 'cached'
          // 关联资源到步骤
          await learningPathStore.linkResourceToStep(stepId, task.resourceId)
          if (stepType === 'quiz') {
            quizList.value = parseQuizContent(resource.value.resourceContent)
          }
        }
      } else if (task.status === 'failed') {
        stopPolling()
        resourceStatus.value = 'error'
        statusText.value = task.errorMsg || '生成失败'
        ElMessage.error('资源生成失败: ' + (task.errorMsg || '未知错误'))
      } else {
        statusText.value = `生成中... ${progress.value}%`
      }
    } catch (e) {
      // 轮询出错不停止，等下次重试
    }
  }, 2000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// ========== 答题交互 ==========
function selectOption(questionIndex, option) {
  if (quizPhase.value === 'results') return
  userAnswers.value[questionIndex] = option
}

async function submitQuiz() {
  // 检查是否所有题目都已作答
  const unanswered = quizList.value.filter((_, i) => !userAnswers.value[i])
  if (unanswered.length > 0) {
    ElMessage.warning(`还有 ${unanswered.length} 道题未作答`)
    return
  }

  const answers = quizList.value.map((q, index) => ({
    questionId: index,
    userAnswer: userAnswers.value[index],
    spendTime: questionTimers.value[index] || 0
  }))

  try {
    const result = await learningPathStore.submitQuiz(stepId, answers)
    quizResult.value = result
    quizPhase.value = 'results'
    ElMessage.success(`测验完成！得分: ${result.score}分`)
  } catch (error) {
    ElMessage.error('提交测验失败，请重试')
  }
}

function retryQuiz() {
  quizPhase.value = 'answering'
  userAnswers.value = {}
  questionTimers.value = {}
  quizResult.value = null
}

// 提取选项字母 (如 "A.xxx" → "A")
function getOptionLetter(option) {
  const match = option.match(/^([A-Za-z])[.、．:：\s]/)
  return match ? match[1].toUpperCase() : option.charAt(0).toUpperCase()
}

// 提取选项内容 (如 "A.xxx" → "xxx")
function getOptionContent(option) {
  return option.replace(/^[A-Za-z][.、．:：\s]+/, '')
}

const goBack = () => {
  router.push(`/path/step/${pathId}`)
}

onMounted(() => {
  initResource()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div class="step-resource-page">
    <!-- 顶部导航 -->
    <div class="page-nav">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回学习步骤
      </el-button>
      <div class="nav-title">
        <h3>{{ stepTitle }}</h3>
        <el-tag v-if="stepType === 'quiz'" type="danger" size="small" effect="dark">知识测验</el-tag>
        <el-tag v-else type="primary" size="small" effect="dark">学习文档</el-tag>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="32" color="#409eff"><Loading /></el-icon>
      <p>正在准备学习资源...</p>
    </div>

    <!-- 生成中 - 进度条 -->
    <div v-else-if="resourceStatus === 'generating'" class="generating-state">
      <div class="gen-card">
        <el-icon class="is-loading" :size="48" color="#409eff"><Loading /></el-icon>
        <h3>{{ statusText }}</h3>
        <el-progress :percentage="progress" :stroke-width="10" style="width: 80%; margin-top: 16px;" />
        <p class="gen-hint">AI 正在根据知识点生成个性化学习资料，请耐心等待...</p>
      </div>
    </div>

    <!-- 生成失败 -->
    <div v-else-if="resourceStatus === 'error'" class="error-state">
      <el-result icon="error" title="资源生成失败" :sub-title="statusText">
        <template #extra>
          <el-button type="primary" @click="initResource">重新生成</el-button>
        </template>
      </el-result>
    </div>

    <!-- 文档类型 - 展示资源 -->
    <div v-else-if="resourceStatus === 'cached' && stepType === 'document'" class="resource-content">
      <div class="resource-header-card">
        <h2>{{ resource.resourceTitle }}</h2>
        <div class="resource-meta">
          <el-tag size="small" type="primary" effect="plain">文档</el-tag>
          <span>{{ resource.difficulty || '标准' }}</span>
        </div>
      </div>
      <div class="markdown-body">
        <MarkdownRenderer :content="resource.resourceContent" />
      </div>
    </div>

    <!-- 测验类型 - 答题界面 -->
    <div v-else-if="resourceStatus === 'cached' && stepType === 'quiz'" class="quiz-section">
      <!-- 答题阶段 -->
      <div v-if="quizPhase === 'answering'">
        <div class="quiz-header">
          <h2>{{ resource.resourceTitle || '知识测验' }}</h2>
          <p>共 {{ quizList.length }} 道题，点击选项选择答案</p>
        </div>

        <div v-for="(quiz, qi) in quizList" :key="qi" class="quiz-card">
          <h4 class="quiz-title">{{ qi + 1 }}. {{ quiz.title || quiz.question }}</h4>
          <div class="quiz-options">
            <div
              v-for="(opt, oi) in quiz.options"
              :key="oi"
              class="quiz-option"
              :class="{ selected: userAnswers[qi] === getOptionLetter(opt) }"
              @click="selectOption(qi, getOptionLetter(opt))"
            >
              <span class="option-letter">{{ getOptionLetter(opt) }}</span>
              <span class="option-text">{{ getOptionContent(opt) }}</span>
            </div>
          </div>
        </div>

        <div class="quiz-actions">
          <el-button
            type="primary"
            size="large"
            :disabled="Object.keys(userAnswers).length < quizList.length"
            @click="submitQuiz"
          >
            提交答卷
          </el-button>
        </div>
      </div>

      <!-- 结果阶段 -->
      <div v-if="quizPhase === 'results' && quizResult">
        <div class="result-header">
          <div class="score-circle">
            <span class="score-num">{{ quizResult.score }}</span>
            <span class="score-label">分</span>
          </div>
          <div class="result-stats">
            <div class="stat-item">
              <span class="stat-num green">{{ quizResult.correct }}</span>
              <span class="stat-label">答对</span>
            </div>
            <div class="stat-item">
              <span class="stat-num red">{{ quizResult.total - quizResult.correct }}</span>
              <span class="stat-label">答错</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ quizResult.total }}</span>
              <span class="stat-label">总题数</span>
            </div>
          </div>
        </div>

        <div v-for="(quiz, qi) in quizList" :key="qi" class="quiz-card result-card">
          <h4 class="quiz-title">{{ qi + 1 }}. {{ quiz.title || quiz.question }}</h4>
          <div class="quiz-options">
            <div
              v-for="(opt, oi) in quiz.options"
              :key="oi"
              class="quiz-option"
              :class="{
                correct: getOptionLetter(opt) === quiz.answer,
                wrong: userAnswers[qi] === getOptionLetter(opt) && getOptionLetter(opt) !== quiz.answer,
                selected: userAnswers[qi] === getOptionLetter(opt)
              }"
            >
              <span class="option-letter">{{ getOptionLetter(opt) }}</span>
              <span class="option-text">{{ getOptionContent(opt) }}</span>
            </div>
          </div>
          <div v-if="quiz.analysis" class="quiz-analysis">
            <p><strong>解析：</strong>{{ quiz.analysis }}</p>
          </div>
          <div class="quiz-answer-tag">
            <el-tag v-if="userAnswers[qi] === quiz.answer" type="success" size="small">回答正确</el-tag>
            <el-tag v-else type="danger" size="small">
              回答错误，正确答案: {{ quiz.answer }}
            </el-tag>
          </div>
        </div>

        <div class="quiz-actions">
          <el-button type="primary" size="large" @click="retryQuiz">重新答题</el-button>
          <el-button size="large" @click="goBack">返回步骤</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
export default {
  components: { ArrowLeft, Loading }
}
</script>

<style scoped>
.step-resource-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 0;
}

.page-nav {
  margin-bottom: 16px;
}

.page-nav .nav-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.page-nav .nav-title h3 {
  font-size: 20px;
  color: #1d1e2c;
  margin: 0;
}

/* 加载状态 */
.loading-state,
.generating-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.gen-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  border: 1px solid #f0f0f0;
  width: 100%;
}

.gen-card h3 {
  font-size: 18px;
  color: #1d1e2c;
  margin: 16px 0 8px;
}

.gen-hint {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}

/* 资源内容 */
.resource-content {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.resource-header-card {
  padding: 24px 28px;
  border-bottom: 1px solid #f0f0f0;
}

.resource-header-card h2 {
  font-size: 20px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}

.markdown-body {
  padding: 28px;
}

/* 测验界面 */
.quiz-section {
  max-width: 760px;
  margin: 0 auto;
}

.quiz-header {
  text-align: center;
  margin-bottom: 24px;
}

.quiz-header h2 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.quiz-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.quiz-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid #f0f0f0;
}

.quiz-title {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0 0 16px;
  line-height: 1.6;
}

.quiz-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quiz-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.quiz-option:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.quiz-option.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.quiz-option.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.quiz-option.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.option-letter {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: #f0f2f5;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
}

.quiz-option.selected .option-letter {
  background: #409eff;
  color: #fff;
}

.quiz-option.correct .option-letter {
  background: #67c23a;
  color: #fff;
}

.quiz-option.wrong .option-letter {
  background: #f56c6c;
  color: #fff;
}

.option-text {
  font-size: 14px;
  color: #303133;
}

.quiz-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 0;
}

/* 结果展示 */
.result-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-num {
  font-size: 56px;
  font-weight: 800;
  line-height: 1;
}

.score-label {
  font-size: 16px;
  opacity: 0.8;
  margin-top: 4px;
}

.result-stats {
  display: flex;
  gap: 28px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.stat-num.green { color: #a5f5a5; }
.stat-num.red { color: #ffb3b3; }

.stat-label {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}

.quiz-analysis {
  margin-top: 12px;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.quiz-analysis p {
  font-size: 13px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.quiz-answer-tag {
  margin-top: 10px;
}
</style>
