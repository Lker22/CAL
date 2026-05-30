<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { tutorApi } from '@/api/tutor'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import MarkdownRenderer from '@/components/markdown/MarkdownRenderer.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const answer = ref(null)

const goBack = () => {
  router.push('/tutor/history')
}

onMounted(async () => {
  const recordId = route.params.recordId
  if (!recordId) {
    ElMessage.warning('缺少记录ID')
    router.push('/tutor/history')
    return
  }

  try {
    const response = await tutorApi.getTutorDetail(recordId)
    answer.value = response.data
  } catch (error) {
    ElMessage.error('获取答疑详情失败')
    router.push('/tutor/history')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="tutor-answer-page" v-loading="loading">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回历史
      </el-button>
    </div>

    <div v-if="answer" class="answer-container">
      <!-- 问题展示 -->
      <div class="question-card">
        <h4>问题</h4>
        <p>{{ answer.question }}</p>
      </div>

      <!-- AI解答 -->
      <div class="answer-card">
        <div class="answer-header">
          <div class="ai-avatar">AI</div>
          <h4>AI解答</h4>
        </div>

        <div class="answer-content">
          <MarkdownRenderer :content="answer.textAnswer" />
        </div>

        <!-- 图片讲解 -->
        <div v-if="answer.imageUrl" class="image-explanation">
          <h5>图解说明</h5>
          <el-image
            :src="answer.imageUrl"
            fit="contain"
            :preview-src-list="[answer.imageUrl]"
            class="explanation-image"
          />
        </div>

        <!-- 视频讲解 -->
        <div v-if="answer.videoUrl" class="video-explanation">
          <h5>视频讲解</h5>
          <video :src="answer.videoUrl" controls class="explanation-video" />
        </div>
      </div>
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
.tutor-answer-page {
  padding: 0;
}

.page-header {
  margin-bottom: 16px;
}

.answer-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: #f0f7ff;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #409eff;
}

.question-card h4 {
  font-size: 14px;
  color: #409eff;
  margin: 0 0 8px;
}

.question-card p {
  font-size: 15px;
  color: #1d1e2c;
  margin: 0;
  font-weight: 500;
}

.answer-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.answer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d1e2c, #2d2e42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.answer-header h4 {
  font-size: 16px;
  color: #1d1e2c;
  margin: 0;
}

.answer-content {
  margin-bottom: 24px;
}

.image-explanation {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 10px;
}

.image-explanation h5 {
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.explanation-image {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
}

.video-explanation {
  margin-top: 24px;
}

.video-explanation h5 {
  font-size: 14px;
  color: #1d1e2c;
  margin: 0 0 12px;
}

.explanation-video {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
}
</style>
