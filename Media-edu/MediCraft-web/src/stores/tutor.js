import { ref } from 'vue'
import { defineStore } from 'pinia'
import { tutorApi } from '@/api/tutor'

/**
 * 智能辅导状态管理
 */
export const useTutorStore = defineStore('tutor', () => {
  const conversationMessages = ref([])
  const currentAnswer = ref(null)
  const isAnswering = ref(false)

  const historyRecords = ref([])
  const historyTotal = ref(0)
  const historyLoading = ref(false)

  /**
   * 发送提问（同步获取回答，前端逐字显示）
   */
  async function askQuestion(questionData, onChunk) {
    conversationMessages.value.push({
      role: 'user',
      content: questionData.text,
      images: questionData.images || [],
      timestamp: Date.now()
    })

    isAnswering.value = true
    // 先显示"思考中"提示
    currentAnswer.value = { textAnswer: 'AI正在思考中，请稍候...', imageUrl: null }

    try {
      const response = await tutorApi.askQuestion(questionData)
      const data = response.data

      // 清空"思考中"，开始逐字显示真实回答
      currentAnswer.value.textAnswer = ''
      const fullText = data.textAnswer || ''
      let charIndex = 0

      await new Promise((resolve) => {
        const timer = setInterval(() => {
          if (charIndex < fullText.length) {
            // 每次追加若干字符
            const chunk = fullText.slice(charIndex, charIndex + 5)
            currentAnswer.value.textAnswer += chunk
            charIndex += 5
            if (onChunk) onChunk(chunk)
          } else {
            clearInterval(timer)
            resolve()
          }
        }, 16)
      })

      // 打字动画完成后，将完整消息加入对话列表
      conversationMessages.value.push({
        role: 'assistant',
        content: data.textAnswer,
        imageUrl: data.imageUrl,
        timestamp: Date.now()
      })

      return data
    } catch (error) {
      console.warn('[Tutor] 提问失败:', error.message)
      throw error
    } finally {
      isAnswering.value = false
      currentAnswer.value = null
    }
  }

  /**
   * 获取答疑历史记录
   */
  async function getHistory(params = {}) {
    historyLoading.value = true
    try {
      const response = await tutorApi.getHistory(params)
      const data = response.data
      historyRecords.value = data.records || []
      historyTotal.value = data.total || 0
      return data
    } catch (error) {
      console.warn('[Tutor] 获取历史记录失败:', error.message)
      historyRecords.value = []
      historyTotal.value = 0
    } finally {
      historyLoading.value = false
    }
  }

  /**
   * 删除答疑记录
   */
  async function deleteRecord(recordId) {
    try {
      await tutorApi.deleteTutorRecord(recordId)
      historyRecords.value = historyRecords.value.filter(r => r.recordId !== recordId)
      historyTotal.value = Math.max(0, historyTotal.value - 1)
      return true
    } catch (error) {
      console.warn('[Tutor] 删除记录失败:', error.message)
      throw error
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
    historyTotal,
    historyLoading,
    askQuestion,
    getHistory,
    deleteRecord,
    clearConversation
  }
})
