import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { profileApi } from '@/api/profile'

/**
 * 学习画像状态管理
 * 管理学习画像的6大维度：知识基础、认知风格、学习目标、易错点、学习节奏、资源偏好
 */
export const useProfileStore = defineStore('profile', () => {
  // 学习画像数据
  const profile = ref(null)
  const profileLoading = ref(false)
  const profileDimensions = ref({
    knowledgeBase: null,      // 知识基础
    cognitiveStyle: null,     // 认知风格
    learningGoals: null,      // 学习目标
    errorPoints: null,        // 易错点
    learningRhythm: null,     // 学习节奏
    resourcePreference: null  // 资源偏好
  })

  // 对话上下文（画像构建对话）
  const chatMessages = ref([])
  const isBuilding = ref(false)

  // 画像完成度
  const profileCompletion = computed(() => {
    if (!profile.value) return 0
    const dimensions = Object.values(profileDimensions.value)
    const completed = dimensions.filter(d => d !== null).length
    return Math.round((completed / dimensions.length) * 100)
  })

  /**
   * 获取学习画像
   */
  async function getProfile() {
    profileLoading.value = true
    try {
      const response = await profileApi.getProfile()
      profile.value = response.data
      updateDimensions(response.data)
      return response
    } catch (error) {
      throw error
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * 更新画像维度数据
   */
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
        context: chatMessages.value
      })

      chatMessages.value.push({
        role: 'assistant',
        content: response.data.reply,
        timestamp: Date.now()
      })

      // 如果画像已更新，同步更新
      if (response.data.profileUpdated) {
        profile.value = response.data.profile
        updateDimensions(response.data.profile)
      }

      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 开始画像构建对话
   */
  async function startProfileBuild() {
    isBuilding.value = true
    chatMessages.value = []
    try {
      const response = await profileApi.startBuild()
      chatMessages.value.push({
        role: 'assistant',
        content: response.data.greeting,
        timestamp: Date.now()
      })
      return response
    } catch (error) {
      throw error
    } finally {
      isBuilding.value = false
    }
  }

  /**
   * 更新学习画像
   */
  async function updateProfile(profileData) {
    profileLoading.value = true
    try {
      const response = await profileApi.updateProfile(profileData)
      profile.value = response.data
      updateDimensions(response.data)
      return response
    } catch (error) {
      throw error
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * 清空对话上下文
   */
  function clearChatContext() {
    chatMessages.value = []
    isBuilding.value = false
  }

  return {
    profile,
    profileLoading,
    profileDimensions,
    chatMessages,
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
