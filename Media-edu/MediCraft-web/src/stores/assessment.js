import { ref } from 'vue'
import { defineStore } from 'pinia'
import { assessmentApi } from '@/api/assessment'

/**
 * 学习效果评估状态管理
 * 管理学习评估报告、评估结果、学习数据统计
 */
export const useAssessmentStore = defineStore('assessment', () => {
  // 评估报告
  const assessmentReport = ref(null)
  const reportLoading = ref(false)

  // 评估结果
  const assessmentResult = ref(null)
  const resultLoading = ref(false)

  // 学习数据统计
  const learningStats = ref(null)
  const statsLoading = ref(false)

  /**
   * 获取学习评估报告
   */
  async function getAssessmentReport(params = {}) {
    reportLoading.value = true
    try {
      const response = await assessmentApi.getReport(params)
      assessmentReport.value = response.data
      return response
    } catch (error) {
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 获取评估结果
   */
  async function getAssessmentResult(params = {}) {
    resultLoading.value = true
    try {
      const response = await assessmentApi.getResult(params)
      assessmentResult.value = response.data
      return response
    } catch (error) {
      throw error
    } finally {
      resultLoading.value = false
    }
  }

  /**
   * 获取学习数据统计
   */
  async function getLearningStats(params = {}) {
    statsLoading.value = true
    try {
      const response = await assessmentApi.getStats(params)
      learningStats.value = response.data
      return response
    } catch (error) {
      throw error
    } finally {
      statsLoading.value = false
    }
  }

  /**
   * 生成评估报告
   */
  async function generateReport(params = {}) {
    reportLoading.value = true
    try {
      const response = await assessmentApi.generateReport(params)
      assessmentReport.value = response.data
      return response
    } catch (error) {
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  return {
    assessmentReport,
    reportLoading,
    assessmentResult,
    resultLoading,
    learningStats,
    statsLoading,
    getAssessmentReport,
    getAssessmentResult,
    getLearningStats,
    generateReport
  }
})
