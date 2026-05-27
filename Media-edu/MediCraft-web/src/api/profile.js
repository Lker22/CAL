import request from './request'

/**
 * 学习画像相关API接口
 * 对接后端student_profile、chat_context表相关业务
 * 画像6大维度：知识基础、认知风格、学习目标、易错点、学习节奏、资源偏好
 */
export const profileApi = {
  /**
   * 获取学习画像
   */
  getProfile() {
    return request({
      url: '/profile',
      method: 'get'
    })
  },

  /**
   * 更新学习画像
   * @param {Object} data - 画像维度数据
   */
  updateProfile(data) {
    return request({
      url: '/profile',
      method: 'put',
      data
    })
  },

  /**
   * 开始画像构建对话
   */
  startBuild() {
    return request({
      url: '/profile/build/start',
      method: 'post'
    })
  },

  /**
   * 发送画像构建对话消息
   * @param {Object} data - { message, context }
   */
  buildProfile(data) {
    return request({
      url: '/profile/build/chat',
      method: 'post',
      data
    })
  },

  /**
   * 流式对话构建画像（SSE）
   * @param {Object} data - { message, context }
   */
  buildProfileStream(data) {
    return request({
      url: '/profile/build/stream',
      method: 'post',
      data,
      responseType: 'stream'
    })
  },

  /**
   * 获取画像构建历史对话
   */
  getChatHistory() {
    return request({
      url: '/profile/chat/history',
      method: 'get'
    })
  },

  /**
   * 重置画像
   */
  resetProfile() {
    return request({
      url: '/profile/reset',
      method: 'post'
    })
  }
}
