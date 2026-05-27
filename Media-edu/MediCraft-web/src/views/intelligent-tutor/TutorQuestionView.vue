<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useTutorStore } from '@/stores/tutor'
import { ElMessage } from 'element-plus'
import { Promotion, Picture } from '@element-plus/icons-vue'
import StreamText from '@/components/markdown/StreamText.vue'
import FileUpload from '@/components/upload/FileUpload.vue'

const tutorStore = useTutorStore()

const inputText = ref('')
const uploadedImages = ref([])
const chatContainer = ref(null)
const isLoading = ref(false)

// 示例问题
const sampleQuestions = [
  '什么是机器学习？',
  'Python中列表和元组的区别？',
  '如何理解神经网络的反向传播？',
  '请解释一下HTTP协议的工作原理'
]

// 发送提问
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text && uploadedImages.value.length === 0) return

  const questionData = {
    text: text || '请分析这张图片',
    images: uploadedImages.value.map(img => img.url)
  }

  inputText.value = ''
  uploadedImages.value = []
  isLoading.value = true

  try {
    // 使用流式输出
    await tutorStore.askQuestionStream(questionData, (chunk) => {
      scrollToBottom()
    })
  } catch (error) {
    // 模拟流式回复
    const mockReply = `这是一个很好的问题！让我为你详细解答。

### 关于"${questionData.text}"

**核心概念：**
这是计算机科学中的重要基础概念。理解它需要从以下几个方面入手：

1. **基础定义**：首先需要明确基本定义和范畴
2. **工作原理**：了解其内部运行机制
3. **应用场景**：掌握实际使用场景

\`\`\`python
# 示例代码
def example():
    print("Hello, AI Learning!")
\`\`\`

> 💡 提示：建议结合实践项目来加深理解。

如果你还有其他问题，随时可以问我！`

    let currentIndex = 0
    const interval = setInterval(() => {
      if (currentIndex < mockReply.length) {
        const chunk = mockReply.slice(currentIndex, currentIndex + 5)
        if (tutorStore.currentAnswer) {
          tutorStore.currentAnswer.answer += chunk
        } else {
          tutorStore.currentAnswer = { answer: chunk }
        }
        currentIndex += 5
        scrollToBottom()
      } else {
        clearInterval(interval)
        isLoading.value = false
        tutorStore.conversationMessages.push({
          role: 'assistant',
          content: mockReply,
          timestamp: Date.now()
        })
      }
    }, 30)

    tutorStore.conversationMessages.push({
      role: 'user',
      content: questionData.text,
      images: questionData.images,
      timestamp: Date.now()
    })
    return
  } finally {
    isLoading.value = false
  }
}

// 点击示例问题
const handleSampleQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

// 处理图片上传
const handleImageUpload = (result) => {
  uploadedImages.value.push(result)
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
            <!-- 文本内容 -->
            <StreamText
              v-if="msg.role === 'assistant'"
              :text="msg.content"
              :is-streaming="isLoading && index === tutorStore.conversationMessages.length - 1"
            />
            <div v-else class="user-text">{{ msg.content }}</div>
          </div>
        </div>

        <!-- 当前流式回复 -->
        <div v-if="tutorStore.currentAnswer && isLoading" class="message-item assistant">
          <div class="message-avatar">
            <span>AI</span>
          </div>
          <div class="message-content">
            <StreamText
              :text="tutorStore.currentAnswer.answer"
              :is-streaming="true"
            />
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
        <FileUpload
          type="image"
          :max-count="3"
          :max-size="5"
          :model-value="uploadedImages"
          use-base64
          @success="handleImageUpload"
          class="upload-btn"
        />
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

<script>
import { Close } from '@element-plus/icons-vue'
export default {
  components: { Close }
}
</script>

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

.image-preview-area {
  display: flex;
  gap: 8px;
  padding: 8px 24px;
  border-top: 1px solid #f0f0f0;
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

.upload-btn {
  flex-shrink: 0;
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
