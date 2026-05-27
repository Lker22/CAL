import request from './request'

/**
 * 智能辅导相关API接口
 * 对接后端smart_tutor表相关业务
 * 支持多模态提问（文字+图片）、AI解答（文字+图解+视频讲解）
 */
export const tutorApi = {
  /**
   * 发送提问（普通请求）
   * @param {Object} data - { text, images: [] }
   */
  askQuestion(data) {
    return request({
      url: '/tutor/ask',
      method: 'post',
      data
    })
  },

  /**
   * 发送提问（流式输出SSE）
   * @param {Object} data - { text, images: [] }
   * @param {Function} onChunk - 接收流式数据块的回调函数
   */
  askQuestionStream(data, onChunk) {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('token')
      const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

      fetch(`${baseUrl}/tutor/ask/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error('请求失败')
          }

          const reader = response.body.getReader()
          const decoder = new TextDecoder()

          function read() {
            reader.read().then(({ done, value }) => {
              if (done) {
                resolve()
                return
              }

              const chunk = decoder.decode(value, { stream: true })
              // 解析SSE格式数据
              const lines = chunk.split('\n')
              lines.forEach((line) => {
                if (line.startsWith('data: ')) {
                  const data = line.slice(6)
                  if (data === '[DONE]') {
                    resolve()
                    return
                  }
                  try {
                    const parsed = JSON.parse(data)
                    if (onChunk) onChunk(parsed.content || parsed)
                  } catch (e) {
                    if (onChunk) onChunk(data)
                  }
                }
              })

              read()
            })
          }

          read()
        })
        .catch((error) => {
          reject(error)
        })
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
