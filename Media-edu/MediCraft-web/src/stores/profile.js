import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { profileApi } from '@/api/profile'

export const useProfileStore = defineStore('profile', () => {
  const profile = ref(null)
  const profileLoading = ref(false)
  const profileDimensions = ref({
    knowledgeBase: null,
    cognitiveStyle: null,
    learningGoals: null,
    errorPoints: null,
    learningRhythm: null,
    resourcePreference: null
  })

  const chatMessages = ref([])
  const sessionId = ref(null)
  const isBuilding = ref(false)

  const profileCompletion = computed(() => {
    if (!profile.value) return 0
    const dimensions = Object.values(profileDimensions.value)
    const completed = dimensions.filter(d => d !== null).length
    return Math.round((completed / dimensions.length) * 100)
  })

  async function getProfile() {
    profileLoading.value = true
    try {
      const response = await profileApi.getProfile()
      profile.value = response.data
      updateDimensions(response.data)
      return response
    } catch (error) {
      console.warn('[Profile] 获取画像失败:', error.message)
    } finally {
      profileLoading.value = false
    }
  }

  function updateDimensions(profileData) {
    if (!profileData) return
    profileDimensions.value = {
      knowledgeBase: profileData.knowledgeBase || null,
      cognitiveStyle: profileData.cognitiveStyle || null,
      learningGoals: profileData.learningGoals || null,
      errorPoints: profileData.errorPoints || null,
      learningRhythm: profileData.learningRhythm || null,
      resourcePreference: profileData.resourcePreference || null
    }
  }

  /**
   * 发送画像构建对话消息
   * 后端期望: { context: sessionId(String), message: 用户输入(String) }
   * 后端返回: { aiReply: "...", profile?: {...} }
   */
  async function sendChatMessage(message) {
    chatMessages.value.push({
      role: 'user',
      content: message,
      timestamp: Date.now()
    })

    try {
      const response = await profileApi.buildProfile({
        message,
        context: sessionId.value  // 发送 sessionId 字符串，不是消息数组
      })

      chatMessages.value.push({
        role: 'assistant',
        content: response.data.aiReply,
        timestamp: Date.now()
      })

      // 如果画像已抽取
      if (response.data.profile) {
        profile.value = response.data.profile
        updateDimensions(response.data.profile)
      }

      return response
    } catch (error) {
      console.warn('[Profile] 发送消息失败:', error.message)
      throw error
    }
  }

  /**
   * 开始画像构建对话
   * 后端返回: Result { data: "sessionId字符串" }
   */
  async function startProfileBuild() {
    isBuilding.value = true
    chatMessages.value = []
    sessionId.value = null
    try {
      const response = await profileApi.startBuild()
      sessionId.value = response.data  // 后端直接返回 sessionId 字符串
      chatMessages.value.push({
        role: 'assistant',
        content: '你好！我是你的智能学习助手，接下来我会通过对话来了解你的学习情况。请告诉我你的专业和年级？',
        timestamp: Date.now()
      })
      return response
    } catch (error) {
      console.warn('[Profile] 开始画像构建失败:', error.message)
      throw error
    } finally {
      isBuilding.value = false
    }
  }

  async function updateProfile(profileData) {
    profileLoading.value = true
    try {
      const response = await profileApi.updateProfile(profileData)
      profile.value = response.data
      updateDimensions(response.data)
      return response
    } catch (error) {
      console.warn('[Profile] 更新画像失败:', error.message)
    } finally {
      profileLoading.value = false
    }
  }

  function clearChatContext() {
    chatMessages.value = []
    sessionId.value = null
    isBuilding.value = false
  }

  return {
    profile,
    profileLoading,
    profileDimensions,
    chatMessages,
    sessionId,
    isBuilding,
    profileCompletion,
    getProfile,
    updateDimensions,
    sendChatMessage,
    startProfileBuild,
    updateProfile,
    clearChatContext
  }
})
