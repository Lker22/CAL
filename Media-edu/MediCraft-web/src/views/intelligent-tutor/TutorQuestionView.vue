<script setup>
import { ref, nextTick } from 'vue'
import { useTutorStore } from '@/stores/tutor'
import { ElMessage } from 'element-plus'
import { Promotion, Picture, Close } from '@element-plus/icons-vue'
import StreamText from '@/components/markdown/StreamText.vue'
import { fileToBase64 } from '@/utils/upload'

const tutorStore = useTutorStore()

const inputText = ref('')
const uploadedImages = ref([])
const chatContainer = ref(null)
const isLoading = ref(false)

// 示例问题
const sampleQuestions = [
  '如何高效地做课堂笔记？',
  '什么是思维导图？怎么用它来复习？',
  '怎样提高学习时的专注力？',
  '请解释费曼学习法的核心步骤'
]

// 发送提问
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text && uploadedImages.value.length === 0) return
  if (isLoading.value) return

  const questionData = {
    text: text || '请分析这张图片',
    images: uploadedImages.value.map(img => img.url)
  }

  inputText.value = ''
  uploadedImages.value = []
  isLoading.value = true

  try {
    await tutorStore.askQuestion(questionData, () => {
      scrollToBottom()
    })
  } catch (error) {
    ElMessage.error('提问失败，请重试')
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

// 点击示例问题
const handleSampleQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.warning('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.warning('图片大小不能超过5MB')
    return false
  }
  if (uploadedImages.value.length >= 3) {
    ElMessage.warning('最多上传3张图片')
    return false
  }
  return true
}

// 自定义图片上传(转base64)
const handleCustomUpload = async (options) => {
  const { file } = options
  try {
    const base64 = await fileToBase64(file)
    uploadedImages.value.push({
      name: file.name,
      url: base64,
      type: file.type,
      size: file.size
    })
  } catch {
    ElMessage.error('图片上传失败')
  }
}

// 移除图片
const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 清空对话
const clearChat = () => {
  tutorStore.clearConversation()
}

// 处理键盘事件
const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="tutor-question-page">
    <div class="page-header">
      <div class="header-left">
        <h2>AI智能答疑</h2>
        <p>支持文字和图片多模态提问</p>
      </div>
      <el-button text type="danger" @click="clearChat" :disabled="tutorStore.conversationMessages.length === 0">
        清空对话
      </el-button>
    </div>

    <div class="chat-container" ref="chatContainer">
      <!-- 空状态 -->
      <div v-if="tutorStore.conversationMessages.length === 0 && !tutorStore.currentAnswer" class="welcome-area">
        <div class="welcome-icon">💡</div>
        <h3>有什么学习问题？</h3>
        <p>文字描述 + 图片上传，AI为你详细解答</p>

        <div class="sample-questions">
          <div
            v-for="(q, i) in sampleQuestions"
            :key="i"
            class="sample-card"
            @click="handleSampleQuestion(q)"
          >
            {{ q }}
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="messages-list">
        <div
          v-for="(msg, index) in tutorStore.conversationMessages"
          :key="index"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar">
            <span v-if="msg.role === 'user'">我</span>
            <span v-else>AI</span>
          </div>
          <div class="message-content">
            <!-- 用户图片 -->
            <div v-if="msg.images && msg.images.length" class="message-images">
              <el-image
                v-for="(img, i) in msg.images"
                :key="i"
                :src="img"
                fit="cover"
                class="preview-image"
                :preview-src-list="msg.images"
              />
            </div>
            <!-- AI回答 -->
            <StreamText
              v-if="msg.role === 'assistant'"
              :text="msg.content"
              :is-streaming="false"
            />
            <div v-else class="user-text">{{ msg.content }}</div>
            <!-- AI图解 -->
            <div v-if="msg.imageUrl" class="ai-image">
              <el-image
                :src="msg.imageUrl"
                fit="contain"
                :preview-src-list="[msg.imageUrl]"
                class="ai-explanation-image"
              />
            </div>
          </div>
        </div>

        <!-- 当前流式回复 -->
        <div v-if="tutorStore.currentAnswer && isLoading" class="message-item assistant">
          <div class="message-avatar">
            <span>AI</span>
          </div>
          <div class="message-content">
            <StreamText
              :text="tutorStore.currentAnswer.textAnswer"
              :is-streaming="true"
            />
            <div v-if="tutorStore.currentAnswer.imageUrl" class="ai-image">
              <el-image
                :src="tutorStore.currentAnswer.imageUrl"
                fit="contain"
                :preview-src-list="[tutorStore.currentAnswer.imageUrl]"
                class="ai-explanation-image"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览区 -->
    <div v-if="uploadedImages.length" class="image-preview-area">
      <div v-for="(img, index) in uploadedImages" :key="index" class="preview-item">
        <el-image :src="img.url" fit="cover" class="preview-thumb" />
        <el-icon class="remove-btn" @click="removeImage(index)"><Close /></el-icon>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-wrapper">
        <el-upload
          class="compact-upload"
          :show-file-list="false"
          :before-upload="beforeImageUpload"
          :http-request="handleCustomUpload"
          accept="image/*"
          :disabled="isLoading || uploadedImages.length >= 3"
        >
          <el-tooltip content="上传图片(最多3张)" placement="top">
            <el-icon class="upload-icon-btn" :size="20"><Picture /></el-icon>
          </el-tooltip>
        </el-upload>
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入你的学习问题... (Enter发送，Shift+Enter换行)"
          resize="none"
          :disabled="isLoading"
          @keydown="handleKeydown"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="isLoading"
          :disabled="!inputText.trim() && uploadedImages.length === 0"
          circle
          class="send-btn"
          @click="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.tutor-question-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 112px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left h2 {
  font-size: 18px;
  color: #1d1e2c;
  margin: 0 0 2px;
}

.header-left p {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.welcome-area {
  text-align: center;
  padding: 40px 0;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.welcome-area h3 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.welcome-area p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 24px;
}

.sample-questions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

.sample-card {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
}

.sample-card:hover {
  background: #f0f7ff;
  color: #409eff;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #1d1e2c, #2d2e42);
  color: #fff;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message-item.user .message-content {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-content {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-images {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.preview-image {
  width: 100px;
  height: 100px;
  border-radius: 6px;
}

.user-text {
  white-space: pre-wrap;
}

.ai-image {
  margin-top: 12px;
}

.ai-explanation-image {
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
}

.image-preview-area {
  display: flex;
  gap: 8px;
  padding: 8px 24px 0;
}

.preview-item {
  position: relative;
  width: 60px;
  height: 60px;
}

.preview-thumb {
  width: 100%;
  height: 100%;
  border-radius: 6px;
}

.remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-area {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.compact-upload {
  flex-shrink: 0;
}

.upload-icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  color: #909399;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.upload-icon-btn:hover {
  background: #ecf5ff;
  color: #409eff;
}

.input-wrapper :deep(.el-textarea) {
  flex: 1;
  min-width: 0;
}

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 10px 16px;
}

.send-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}
</style>
