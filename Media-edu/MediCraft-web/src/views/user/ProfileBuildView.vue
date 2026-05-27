<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useProfileStore } from '@/stores/profile'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import StreamText from '@/components/markdown/StreamText.vue'

const profileStore = useProfileStore()

const inputText = ref('')
const chatContainer = ref(null)
const isLoading = ref(false)
const hasStarted = ref(false)

// 开始画像构建对话
const startConversation = async () => {
  hasStarted.value = true
  isLoading.value = true
  try {
    await profileStore.startProfileBuild()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('启动对话失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  const message = inputText.value.trim()
  if (!message || isLoading.value) return

  inputText.value = ''
  isLoading.value = true

  try {
    await profileStore.sendChatMessage(message)
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 处理回车发送
const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 快捷问题
const quickQuestions = [
  '我是一名计算机科学专业的学生',
  '我希望学习人工智能相关知识',
  '我更喜欢通过视频和图表来学习',
  '我每天大约能学习2小时'
]

const handleQuickQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

onMounted(() => {
  if (profileStore.chatMessages.length === 0) {
    // 尚未开始对话
  } else {
    hasStarted.value = true
  }
})
</script>

<template>
  <div class="profile-build-page">
    <div class="page-header">
      <h2>学习画像构建</h2>
      <p>通过自然语言对话，AI将为你建立个性化学习画像</p>
    </div>

    <div class="chat-container" ref="chatContainer">
      <!-- 未开始状态 -->
      <div v-if="!hasStarted" class="welcome-section">
        <div class="welcome-content">
          <div class="welcome-icon">🧠</div>
          <h3>开始构建你的学习画像</h3>
          <p>AI将通过多轮对话了解你的学习特点和需求</p>
          <el-button type="primary" size="large" @click="startConversation" :loading="isLoading">
            开始对话
          </el-button>
        </div>
      </div>

      <!-- 对话消息列表 -->
      <div v-else class="messages-list">
        <div
          v-for="(msg, index) in profileStore.chatMessages"
          :key="index"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar">
            <span v-if="msg.role === 'user'">我</span>
            <span v-else>AI</span>
          </div>
          <div class="message-bubble">
            <StreamText
              v-if="msg.role === 'assistant'"
              :text="msg.content"
              :is-streaming="isLoading && index === profileStore.chatMessages.length - 1"
            />
            <div v-else class="user-text">{{ msg.content }}</div>
          </div>
        </div>

        <!-- AI正在输入提示 -->
        <div v-if="isLoading && profileStore.chatMessages[profileStore.chatMessages.length - 1]?.role === 'user'" class="message-item assistant">
          <div class="message-avatar">
            <span>AI</span>
          </div>
          <div class="message-bubble typing">
            <span class="dot" />
            <span class="dot" />
            <span class="dot" />
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷问题 -->
    <div v-if="hasStarted && profileStore.chatMessages.length <= 2" class="quick-questions">
      <span class="quick-label">快速回复：</span>
      <el-tag
        v-for="(q, i) in quickQuestions"
        :key="i"
        class="quick-tag"
        effect="plain"
        round
        @click="handleQuickQuestion(q)"
      >
        {{ q }}
      </el-tag>
    </div>

    <!-- 输入框 -->
    <div v-if="hasStarted" class="input-section">
      <div class="input-wrapper">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="描述你的学习情况、目标或偏好..."
          resize="none"
          :disabled="isLoading"
          @keydown="handleKeydown"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="isLoading"
          :disabled="!inputText.trim()"
          circle
          class="send-btn"
          @click="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-build-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 112px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.page-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  font-size: 20px;
  color: #1d1e2c;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.welcome-content {
  text-align: center;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome-content h3 {
  font-size: 22px;
  color: #1d1e2c;
  margin: 0 0 8px;
}

.welcome-content p {
  font-size: 14px;
  color: #909399;
  margin: 0 0 24px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.assistant {
  align-self: flex-start;
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

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-bubble {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.user-text {
  white-space: pre-wrap;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.quick-questions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 24px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 12px;
  color: #909399;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  color: #409eff;
  border-color: #409eff;
}

.input-section {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
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
