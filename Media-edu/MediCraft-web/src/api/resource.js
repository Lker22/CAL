import request from './request'

/**
 * 学习资源相关API接口
 * 对接后端ai_agent、learning_resource表相关业务
 * 资源类型：文档、思维导图、题库、视频脚本、实操案例
 */
export const resourceApi = {
  /**
   * 获取智能体列表
   * 六大角色：需求解析、文档生成、思维导图、题库、实操案例、多模态
   */
  getAgents() {
    return request({
      url: '/resource/agents',
      method: 'get'
    })
  },

  /**
   * 获取智能体详情
   * @param {Number} agentId - 智能体ID
   */
  getAgentDetail(agentId) {
    return request({
      url: `/resource/agents/${agentId}`,
      method: 'get'
    })
  },

  /**
   * 生成学习资源（异步任务）
   * @param {Object} data - { agentId, resourceType, topic, params }
   */
  generateResource(data) {
    return request({
      url: '/resource/generate',
      method: 'post',
      data
    })
  },

  /**
   * 获取资源生成进度
   * @param {Number} taskId - 任务ID
   */
  getGenerationProgress(taskId) {
    return request({
      url: `/resource/generate/progress/${taskId}`,
      method: 'get'
    })
  },

  /**
   * 获取资源列表
   * @param {Object} params - { type, keyword, page, pageSize }
   */
  getResources(params) {
    return request({
      url: '/resource/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取资源详情
   * @param {Number} resourceId - 资源ID
   */
  getResourceDetail(resourceId) {
    return request({
      url: `/resource/detail/${resourceId}`,
      method: 'get'
    })
  },

  /**
   * 删除资源
   * @param {Number} resourceId - 资源ID
   */
  deleteResource(resourceId) {
    return request({
      url: `/resource/${resourceId}`,
      method: 'delete'
    })
  },

  /**
   * 下载资源
   * @param {Number} resourceId - 资源ID
   */
  downloadResource(resourceId) {
    return request({
      url: `/resource/download/${resourceId}`,
      method: 'get',
      responseType: 'blob'
    })
  }
}
