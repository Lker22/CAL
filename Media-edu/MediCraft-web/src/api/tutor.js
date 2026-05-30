import request from './request'

/**
 * 智能辅导相关API接口
 */
export const tutorApi = {
  /**
   * 发送提问
   * @param {Object} data - { text, images: [] }
   */
  askQuestion(data) {
    return request({
      url: '/tutor/ask',
      method: 'post',
      data,
      timeout: 120000  // AI回答可能需要较长时间，延长到120秒
    })
  },

  /**
   * 获取答疑历史记录
   * @param {Object} params - { page, pageSize }
   */
  getHistory(params) {
    return request({
      url: '/tutor/history',
      method: 'get',
      params
    })
  },

  /**
   * 获取答疑详情
   * @param {Number} recordId - 答疑记录ID
   */
  getTutorDetail(recordId) {
    return request({
      url: `/tutor/detail/${recordId}`,
      method: 'get'
    })
  },

  /**
   * 删除答疑记录
   * @param {Number} recordId - 答疑记录ID
   */
  deleteTutorRecord(recordId) {
    return request({
      url: `/tutor/${recordId}`,
      method: 'delete'
    })
  }
}
