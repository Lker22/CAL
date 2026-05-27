import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 创建axios实例
 * 统一配置请求和响应拦截器
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 添加token认证、请求头等
 */
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理响应数据、错误提示
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果响应状态码不是成功状态，进行错误处理
    if (res.code && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')

      // 401: 未授权，跳转登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    // 开发阶段无后端时，静默处理网络错误
    if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED' || error.message === 'Network Error') {
      console.warn('[API] 后端服务未启动，静默处理')
      return Promise.reject(new Error('服务暂不可用'))
    }

    // 处理HTTP错误
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.message || '请求失败')
      }
    } else {
      // 非HTTP错误（如取消请求、超时等），不显示全局错误提示
      if (error.code !== 'ERR_CANCELED') {
        console.warn('[API] 请求失败:', error.message)
      }
    }

    return Promise.reject(error)
  }
)

export default request
