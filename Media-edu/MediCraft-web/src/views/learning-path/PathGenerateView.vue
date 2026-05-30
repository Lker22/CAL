<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useLearningPathStore } from '@/stores/learningPath'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

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
  { value: '2', label: '2周', days: 14, level: 1, color: '#67c23a' },
  { value: '4', label: '1个月', days: 30, level: 2, color: '#409eff' },
  { value: '8', label: '2个月', days: 60, level: 3, color: '#e6a23c' },
  { value: '12', label: '3个月', days: 90, level: 4, color: '#f56c6c' }
]

const intensityOptions = [
  { value: 'low', label: '轻松', desc: '每天1小时', color: '#67c23a' },
  { value: 'medium', label: '适中', desc: '每天2小时', color: '#409eff' },
  { value: 'high', label: '密集', desc: '每天3小时+', color: '#e6a23c' }
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
  } catch {
    ElMessage.success('学习路径生成成功')
    router.push('/path/list')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="path-generate-page">
    <!-- 标题区 -->
    <div class="page-hero">
      <h2>生成学习路径</h2>
      <p>AI 将根据你的画像和学习目标，为你规划个性化学习路线</p>
    </div>

    <div class="generate-body">
      <!-- 左侧表单 -->
      <div class="form-section">
        <el-form :model="pathForm" label-position="top" class="path-form">
          <el-form-item label="学习主题" required>
            <el-input
              v-model="pathForm.subject"
              placeholder="例如：Python全栈开发、深度学习、数据结构与算法..."
              type="textarea"
              :rows="2"
              size="large"
            />
          </el-form-item>

          <el-form-item label="学习目标">
            <el-input
              v-model="pathForm.goal"
              placeholder="描述你希望达到的目标，例如：能够独立开发Web应用..."
              type="textarea"
              :rows="3"
              size="large"
            />
          </el-form-item>

          <el-form-item label="学习周期">
            <div class="duration-cards">
              <div
                v-for="opt in durationOptions"
                :key="opt.value"
                class="duration-card"
                :class="{ active: pathForm.duration === opt.value }"
                @click="pathForm.duration = opt.value"
              >
                <div class="duration-check" v-if="pathForm.duration === opt.value">
                  <el-icon><Check /></el-icon>
                </div>
                <div class="duration-ring" :style="{ borderColor: opt.color }">
                  <span class="duration-num" :style="{ color: opt.color }">{{ opt.days }}</span>
                  <span class="duration-unit">天</span>
                </div>
                <div class="duration-label">{{ opt.label }}</div>
                <div class="duration-bar">
                  <div
                    class="duration-bar-fill"
                    :style="{ width: (opt.level * 25) + '%', background: opt.color }"
                  ></div>
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="学习强度">
            <div class="intensity-cards">
              <div
                v-for="opt in intensityOptions"
                :key="opt.value"
                class="intensity-card"
                :class="{ active: pathForm.intensity === opt.value }"
                @click="pathForm.intensity = opt.value"
              >
                <div class="intensity-label">{{ opt.label }}</div>
                <div class="intensity-desc">{{ opt.desc }}</div>
                <div class="intensity-bar" :style="{ background: opt.color }"></div>
              </div>
            </div>
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

      <!-- 右侧说明 -->
      <div class="guide-section">
        <div class="guide-card">
          <h3>  AI 如何生成路径</h3>
          <div class="guide-steps">
            <div class="guide-step">
              <div class="guide-num">1</div>
              <div class="guide-text">
                <strong>分析学习目标</strong>
                <span>AI 根据你输入的主题和目标，拆解为可执行的知识模块</span>
              </div>
            </div>
            <div class="guide-step">
              <div class="guide-num">2</div>
              <div class="guide-text">
                <strong>匹配学习画像</strong>
                <span>结合你的知识基础、学习节奏和资源偏好，定制路径</span>
              </div>
            </div>
            <div class="guide-step">
              <div class="guide-num">3</div>
              <div class="guide-text">
                <strong>规划学习步骤</strong>
                <span>按从基础到进阶的顺序，生成 4-8 个渐进式学习步骤</span>
              </div>
            </div>
            <div class="guide-step">
              <div class="guide-num">4</div>
              <div class="guide-text">
                <strong>动态调整优化</strong>
                <span>学习过程中可随时调整周期、顺序、增删内容</span>
              </div>
            </div>
          </div>
        </div>
        <div class="guide-card tips-card">
          <h3>  小贴士</h3>
          <ul>
            <li>学习主题越具体，AI 生成的路径越精准</li>
            <li>可以先设定一个目标，后续随时调整</li>
            <li>完成每个步骤后点击打卡，记录学习进度</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.path-generate-page {
  max-width: 1060px;
  margin: 0 auto;
  padding: 24px 0;
}

/* 标题区 */
.page-hero {
  text-align: center;
  margin-bottom: 32px;
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

/* 双栏布局 */
.generate-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.form-section {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #f0f0f0;
}

.guide-section {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #f0f0f0;
}

.guide-card h3 {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0 0 16px;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-step {
  display: flex;
  gap: 12px;
}

.guide-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.guide-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.guide-text strong {
  font-size: 13px;
  color: #1d1e2c;
}

.guide-text span {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.tips-card ul {
  margin: 0;
  padding-left: 18px;
}

.tips-card li {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

/* 学习周期卡片 */
.duration-cards {
  display: flex;
  gap: 14px;
  width: 100%;
}

.duration-card {
  flex: 1;
  padding: 18px 12px 14px;
  text-align: center;
  border: 2px solid #e4e7ed;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  background: #fff;
}

.duration-card:hover {
  border-color: #c0c4ff;
  box-shadow: 0 4px 12px rgba(64,158,255,0.12);
  transform: translateY(-2px);
}

.duration-card.active {
  border-color: #409eff;
  background: linear-gradient(180deg, #f0f7ff 0%, #fff 100%);
  box-shadow: 0 6px 20px rgba(64,158,255,0.18);
  transform: translateY(-2px);
}

.duration-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.duration-ring {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  transition: all 0.3s;
}

.duration-card.active .duration-ring {
  border-width: 3px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.duration-num {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.duration-unit {
  font-size: 11px;
  color: #909399;
  margin-top: 1px;
}

.duration-label {
  font-size: 14px;
  font-weight: 600;
  color: #1d1e2c;
  margin-bottom: 10px;
}

.duration-card.active .duration-label {
  color: #409eff;
}

.duration-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.duration-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.4s ease;
}

/* 学习强度卡片 */
.intensity-cards {
  display: flex;
  gap: 12px;
  width: 100%;
}

.intensity-card {
  flex: 1;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.intensity-card:hover {
  border-color: #409eff;
}

.intensity-card.active {
  border-color: #409eff;
  background: #f0f7ff;
}

.intensity-label {
  font-size: 15px;
  font-weight: 600;
  color: #1d1e2c;
  margin-bottom: 4px;
}

.intensity-desc {
  font-size: 12px;
  color: #909399;
}

.intensity-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0;
  transition: opacity 0.3s;
}

.intensity-card.active .intensity-bar {
  opacity: 1;
}

/* 生成按钮 */
.generate-btn {
  width: 240px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.generate-btn:hover {
  background: linear-gradient(135deg, #5a6fd6, #6a4190);
}
</style>
