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
      console.warn('[LearningPath] 生成路径失败:', error.message)
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
      // response = {code:200, msg:"操作成功", data:{records:[...], total:N, ...}}
      // axios拦截器已返回response.data, 所以response就是Result对象
      // response.data 就是分页对象, response.data.records 才是数组
      const pageData = response.data
      if (pageData && Array.isArray(pageData.records)) {
        paths.value = pageData.records
      } else if (Array.isArray(pageData)) {
        paths.value = pageData
      } else {
        paths.value = []
      }
      return response
    } catch (error) {
      console.warn('[LearningPath] 获取路径列表失败:', error.message)
      paths.value = []
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
      console.warn('[LearningPath] 获取路径详情失败:', error.message)
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
      // 刷新当前路径详情以获取最新进度
      if (currentPath.value && currentPath.value.id) {
        await getPathDetail(currentPath.value.id)
      }
      return response
    } catch (error) {
      console.warn('[LearningPath] 完成步骤失败:', error.message)
      throw error
    }
  }

  /**
   * 获取推荐资源
   */
  async function getRecommendedResources(pathId) {
    try {
      const response = await learningPathApi.getRecommendedResources(pathId)
      // response = {code:200, data: [...资源列表]}
      recommendedResources.value = Array.isArray(response.data) ? response.data : []
      return response
    } catch (error) {
      console.warn('[LearningPath] 获取推荐资源失败:', error.message)
      recommendedResources.value = []
    }
  }

  /**
   * 调整学习路径
   */
  async function adjustPath(pathId, adjustData) {
    pathLoading.value = true
    try {
      const response = await learningPathApi.adjustPath(pathId, adjustData)
      // response = {code:200, data: LearningPathDetailVO}
      // response.data 就是 LearningPathDetailVO (含 title, steps 等字段)
      currentPath.value = response.data
      currentSteps.value = response.data?.steps || []
      return response
    } catch (error) {
      console.error('[LearningPath] 调整路径失败:', error)
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
      console.warn('[LearningPath] 删除路径失败:', error.message)
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
