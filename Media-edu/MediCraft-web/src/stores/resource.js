import { ref } from 'vue'
import { defineStore } from 'pinia'
import { resourceApi } from '@/api/resource'

/**
 * 学习资源状态管理
 * 管理AI智能体生成的学习资源，包括文档、思维导图、题库、视频脚本、实操案例
 */
export const useResourceStore = defineStore('resource', () => {
  // 资源列表
  const resources = ref([])
  const currentResource = ref(null)
  const resourceLoading = ref(false)

  // 智能体列表
  const agents = ref([])
  const selectedAgent = ref(null)

  // 资源生成状态
  const generationTasks = ref([])
  const currentTask = ref(null)

  /**
   * 获取智能体列表
   */
  async function getAgents() {
    try {
      const response = await resourceApi.getAgents()
      agents.value = response.data
      return response
    } catch (error) {
      console.warn('[Resource] 获取智能体列表失败:', error.message)
      agents.value = []
    }
  }

  /**
   * 选择智能体
   */
  function selectAgent(agent) {
    selectedAgent.value = agent
  }

  /**
   * 生成学习资源
   */
  async function generateResource(params) {
    try {
      const response = await resourceApi.generateResource(params)
      currentTask.value = response.data
      generationTasks.value.push(response.data)
      return response
    } catch (error) {
      console.warn('[Resource] 生成资源失败:', error.message)
      throw error
    }
  }

  /**
   * 获取资源生成进度
   */
  async function getGenerationProgress(taskId) {
    try {
      const response = await resourceApi.getGenerationProgress(taskId)
      // 更新任务状态
      const taskIndex = generationTasks.value.findIndex(t => t.id === taskId)
      if (taskIndex !== -1) {
        generationTasks.value[taskIndex] = response.data
      }
      if (currentTask.value && currentTask.value.id === taskId) {
        currentTask.value = response.data
      }
      return response
    } catch (error) {
      console.warn('[Resource] 获取生成进度失败:', error.message)
    }
  }

  /**
   * 获取资源列表
   */
  async function getResources(params = {}) {
    resourceLoading.value = true
    try {
      const response = await resourceApi.getResources(params)
      resources.value = response.data
      return response
    } catch (error) {
      console.warn('[Resource] 获取资源列表失败:', error.message)
      resources.value = []
    } finally {
      resourceLoading.value = false
    }
  }

  /**
   * 获取资源详情
   */
  async function getResourceDetail(resourceId) {
    resourceLoading.value = true
    try {
      const response = await resourceApi.getResourceDetail(resourceId)
      currentResource.value = response.data
      return response
    } catch (error) {
      console.warn('[Resource] 获取资源详情失败:', error.message)
    } finally {
      resourceLoading.value = false
    }
  }

  /**
   * 删除资源
   */
  async function deleteResource(resourceId) {
    try {
      await resourceApi.deleteResource(resourceId)
      resources.value = resources.value.filter(r => r.id !== resourceId)
      if (currentResource.value && currentResource.value.id === resourceId) {
        currentResource.value = null
      }
    } catch (error) {
      console.warn('[Resource] 删除资源失败:', error.message)
    }
  }

  /**
   * 清空当前资源
   */
  function clearCurrentResource() {
    currentResource.value = null
  }

  /**
   * 清空当前任务
   */
  function clearCurrentTask() {
    currentTask.value = null
  }

  return {
    resources,
    currentResource,
    resourceLoading,
    agents,
    selectedAgent,
    generationTasks,
    currentTask,
    getAgents,
    selectAgent,
    generateResource,
    getGenerationProgress,
    getResources,
    getResourceDetail,
    deleteResource,
    clearCurrentResource,
    clearCurrentTask
  }
})
