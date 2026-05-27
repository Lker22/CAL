import { ref } from 'vue'
import { defineStore } from 'pinia'
import { learningPathApi } from '@/api/learningPath'

/**
 * 学习路径状态管理
 * 管理学习路径的生成、步骤、资源推荐和动态调整
 */
export const useLearningPathStore = defineStore('learningPath', () => {
  // 学习路径列表
  const paths = ref([])
  const currentPath = ref(null)
  const pathLoading = ref(false)

  // 学习步骤
  const currentSteps = ref([])
  const currentStep = ref(null)

  // 推荐资源
  const recommendedResources = ref([])

  /**
   * 生成学习路径
   */
  async function generatePath(params) {
    pathLoading.value = true
    try {
      const response = await learningPathApi.generatePath(params)
      paths.value.push(response.data)
      return response
    } catch (error) {
      throw error
    } finally {
      pathLoading.value = false
    }
  }

  /**
   * 获取学习路径列表
   */
  async function getPaths(params = {}) {
    pathLoading.value = true
    try {
      const response = await learningPathApi.getPaths(params)
      paths.value = response.data
      return response
    } catch (error) {
      throw error
    } finally {
      pathLoading.value = false
    }
  }

  /**
   * 获取学习路径详情（包含步骤）
   */
  async function getPathDetail(pathId) {
    pathLoading.value = true
    try {
      const response = await learningPathApi.getPathDetail(pathId)
      currentPath.value = response.data
      currentSteps.value = response.data.steps || []
      return response
    } catch (error) {
      throw error
    } finally {
      pathLoading.value = false
    }
  }

  /**
   * 完成学习步骤（打卡）
   */
  async function completeStep(stepId, data = {}) {
    try {
      const response = await learningPathApi.completeStep(stepId, data)
      // 更新步骤状态
      const stepIndex = currentSteps.value.findIndex(s => s.id === stepId)
      if (stepIndex !== -1) {
        currentSteps.value[stepIndex].status = 'completed'
        currentSteps.value[stepIndex].completedAt = Date.now()
      }
      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 获取推荐资源
   */
  async function getRecommendedResources(pathId) {
    try {
      const response = await learningPathApi.getRecommendedResources(pathId)
      recommendedResources.value = response.data
      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * 调整学习路径
   */
  async function adjustPath(pathId, adjustData) {
    pathLoading.value = true
    try {
      const response = await learningPathApi.adjustPath(pathId, adjustData)
      currentPath.value = response.data
      currentSteps.value = response.data.steps || []
      return response
    } catch (error) {
      throw error
    } finally {
      pathLoading.value = false
    }
  }

  /**
   * 删除学习路径
   */
  async function deletePath(pathId) {
    try {
      await learningPathApi.deletePath(pathId)
      paths.value = paths.value.filter(p => p.id !== pathId)
      if (currentPath.value && currentPath.value.id === pathId) {
        currentPath.value = null
        currentSteps.value = []
      }
    } catch (error) {
      throw error
    }
  }

  /**
   * 清空当前路径
   */
  function clearCurrentPath() {
    currentPath.value = null
    currentSteps.value = []
    currentStep.value = null
  }

  return {
    paths,
    currentPath,
    pathLoading,
    currentSteps,
    currentStep,
    recommendedResources,
    generatePath,
    getPaths,
    getPathDetail,
    completeStep,
    getRecommendedResources,
    adjustPath,
    deletePath,
    clearCurrentPath
  }
})
