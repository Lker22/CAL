/**
 * 流式输出处理工具函数
 * 支持SSE（Server-Sent Events）和Fetch API流式读取
 */

/**
 * 创建SSE流式请求
 * @param {String} url - 请求URL
 * @param {Object} options - 配置选项
 * @param {Function} onMessage - 消息回调
 * @param {Function} onError - 错误回调
 * @param {Function} onComplete - 完成回调
 */
export function createSSEStream(url, options = {}, onMessage, onError, onComplete) {
  const token = localStorage.getItem('token')
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const fullUrl = `${baseUrl}${url}`

  const controller = new AbortController()

  fetch(fullUrl, {
    method: options.method || 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          if (onComplete) onComplete()
          break
        }

        buffer += decoder.decode(value, { stream: true })

        // 解析SSE格式：data: xxx\n\n
        const lines = buffer.split('\n\n')
        buffer = lines.pop() || '' // 保留不完整的部分

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6).trim()

            if (data === '[DONE]') {
              if (onComplete) onComplete()
              return
            }

            try {
              const parsed = JSON.parse(data)
              if (onMessage) onMessage(parsed)
            } catch {
              // 非JSON格式，直接传递文本
              if (onMessage) onMessage({ content: data })
            }
          }
        }
      }
    })
    .catch((error) => {
      if (error.name !== 'AbortError') {
        if (onError) onError(error)
      }
    })

  // 返回控制器，用于取消请求
  return controller
}

/**
 * 解析流式文本块，支持Markdown增量渲染
 * @param {String} fullText - 完整文本
 * @param {String} newChunk - 新增文本块
 */
export function appendStreamText(fullText, newChunk) {
  return fullText + newChunk
}

/**
 * 将流式数据转为可消费的迭代器
 * @param {ReadableStream} stream - 可读流
 */
export async function* streamToAsyncIterator(stream) {
  const reader = stream.getReader()
  const decoder = new TextDecoder()

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      yield decoder.decode(value, { stream: true })
    }
  } finally {
    reader.releaseLock()
  }
}
