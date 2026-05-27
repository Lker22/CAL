import request from './request'

/**
 * 学习效果评估相关API接口
 * 对接后端learning_evaluate表相关业务
 */
export const assessmentApi = {
  /**
   * 获取学习评估报告
   * @param {Object} params - { startDate, endDate }
   */
  getReport(params) {
    return request({
      url: '/assessment/report',
      method: 'get',
      params
    })
  },

  /**
   * 生成评估报告
   * @param {Object} data - { pathId, includeModules }
   */
  generateReport(data) {
    return request({
      url: '/assessment/report/generate',
      method: 'post',
      data
    })
  },

  /**
   * 获取评估结果
   * @param {Object} params - { reportId }
   */
  getResult(params) {
    return request({
      url: '/assessment/result',
      method: 'get',
      params
    })
  },

  /**
   * 获取学习数据统计
   * @param {Object} params - { startDate, endDate, type }
   */
  getStats(params) {
    return request({
      url: '/assessment/stats',
      method: 'get',
      params
    })
  },

  /**
   * 获取薄弱点分析
   */
  getWeakPoints() {
    return request({
      url: '/assessment/weak-points',
      method: 'get'
    })
  },

  /**
   * 获取学习趋势
   * @param {Object} params - { period }
   */
  getLearningTrend(params) {
    return request({
      url: '/assessment/trend',
      method: 'get',
      params
    })
  }
}
