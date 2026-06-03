import request from './request'

/**
 * 学习路径相关API接口
 * 对接后端learning_path、learning_path_step、learning_behavior表相关业务
 */
export const learningPathApi = {
  /**
   * 一键生成个性化学习路径
   * @param {Object} data - { subject, goal, duration }
   */
  generatePath(data) {
    return request({
      url: '/learning-path/generate',
      method: 'post',
      data
    })
  },

  /**
   * 获取学习路径列表
   * @param {Object} params - { page, pageSize, status }
   */
  getPaths(params) {
    return request({
      url: '/learning-path/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取学习路径详情（包含步骤）
   * @param {Number} pathId - 路径ID
   */
  getPathDetail(pathId) {
    return request({
      url: `/learning-path/detail/${pathId}`,
      method: 'get'
    })
  },

  /**
   * 完成学习步骤（打卡）
   * @param {Number} stepId - 步骤ID
   * @param {Object} data - { duration, notes, score }
   */
  completeStep(stepId, data) {
    return request({
      url: `/learning-path/step/${stepId}/complete`,
      method: 'post',
      data
    })
  },

  /**
   * 获取智能资源推荐
   * @param {Number} pathId - 路径ID
   */
  getRecommendedResources(pathId) {
    return request({
      url: `/learning-path/${pathId}/recommend`,
      method: 'get'
    })
  },

  /**
   * 动态调整学习路径（AI调用较慢，延长超时到120秒）
   * @param {Number} pathId - 路径ID
   * @param {Object} data - { adjustmentType, params }
   */
  adjustPath(pathId, data) {
    return request({
      url: `/learning-path/${pathId}/adjust`,
      method: 'put',
      data,
      timeout: 120000
    })
  },

  /**
   * 删除学习路径
   * @param {Number} pathId - 路径ID
   */
  deletePath(pathId) {
    return request({
      url: `/learning-path/${pathId}`,
      method: 'delete'
    })
  },

  /**
   * 记录学习行为
   * @param {Object} data - { pathId, stepId, behaviorType, duration }
   */
  recordBehavior(data) {
    return request({
      url: '/learning-path/behavior',
      method: 'post',
      data
    })
  },

  /**
   * 生成步骤学习资源(检查缓存/调用智能体)
   * @param {Number} stepId
   */
  generateStepResource(stepId) {
    return request({
      url: `/learning-path/step/${stepId}/generate-resource`,
      method: 'post',
      timeout: 120000
    })
  },

  /**
   * 关联生成的资源到步骤
   * @param {Number} stepId
   * @param {Number} resourceId
   */
  linkResourceToStep(stepId, resourceId) {
    return request({
      url: `/learning-path/step/${stepId}/link-resource/${resourceId}`,
      method: 'post'
    })
  },

  /**
   * 提交测验答案
   * @param {Number} stepId
   * @param {Array} answers - [{ questionId, userAnswer, spendTime }]
   */
  submitQuiz(stepId, answers) {
    return request({
      url: `/learning-path/step/${stepId}/submit-quiz`,
      method: 'post',
      data: answers
    })
  }
}
