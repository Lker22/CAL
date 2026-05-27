import { ref } from 'vue'
import { defineStore } from 'pinia'
import { tutorApi } from '@/api/tutor'

/**
 * 智能辅导状态管理
 * 管理答疑对话、多模态提问、AI解答、答疑历史
 */
export const useTutorStore = defineStore('tutor', () => {
  // 当前对话
  const conversationMessages = ref([])
  const currentAnswer = ref(null)
  const isAnswering = ref(false)

  // 答疑历史
  const historyRecords = ref([])
  const historyLoading = ref(false)

  /**
   * 发送提问（支持文字+图片多模态）
   */
  async function askQuestion(questionData) {
    // 添加用户消息
    conversationMessages.value.push({
      role: 'user',
      content: questionData.text,
      images: questionData.images || [],
      timestamp: Date.now()
    })

    isAnswering.value = true
    try {
      const response = await tutorApi.askQuestion(questionData)
      currentAnswer.value = response.data

      // 添加AI回复
      conversationMessages.value.push({
        role: 'assistant',
        content: response.data.answer,
        format: response.data.format, // text, image, video
        timestamp: Date.now()
      })

      return response
    } catch (error) {
      console.warn('[Tutor] 提问失败:', error.message)
      throw error
    } finally {
      isAnswering.value = false
    }
  }

  /**
   * 流式提问（支持流式输出）
   */
  async function askQuestionStream(questionData, onChunk) {
    conversationMessages.value.push({
      role: 'user',
      content: questionData.text,
      images: questionData.images || [],
      timestamp: Date.now()
    })

    isAnswering.value = true
    currentAnswer.value = { answer: '', format: 'streaming' }

    try {
      await tutorApi.askQuestionStream(questionData, (chunk) => {
        currentAnswer.value.answer += chunk
        if (onChunk) onChunk(chunk)
      })

      conversationMessages.value.push({
        role: 'assistant',
        content: currentAnswer.value.answer,
        format: 'text',
        timestamp: Date.now()
      })

      return currentAnswer.value
    } catch (error) {
      console.warn('[Tutor] 流式提问失败，使用模拟回复:', error.message)
      throw error
    } finally {
      isAnswering.value = false
    }
  }

  /**
   * 获取答疑历史记录
   */
  async function getHistory(params = {}) {
    historyLoading.value = true
    try {
      const response = await tutorApi.getHistory(params)
      historyRecords.value = response.data
      return response
    } catch (error) {
      console.warn('[Tutor] 获取历史记录失败:', error.message)
      historyRecords.value = []
    } finally {
      historyLoading.value = false
    }
  }

  /**
   * 清空当前对话
   */
  function clearConversation() {
    conversationMessages.value = []
    currentAnswer.value = null
  }

  return {
    conversationMessages,
    currentAnswer,
    isAnswering,
    historyRecords,
    historyLoading,
    askQuestion,
    askQuestionStream,
    getHistory,
    clearConversation
  }
})
