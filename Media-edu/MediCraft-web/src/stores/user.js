import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import router from '@/router'
import {
  mockLogin,
  mockRegister,
  mockGetUserInfo,
  mockUpdateUserInfo,
  printDefaultCredentials
} from '@/utils/mockAuth'

/**
 * 用户状态管理
 * 管理用户登录状态、用户信息、认证token
 * 无后端时使用模拟认证（localStorage存储）
 */
export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 用户登录（模拟认证）
   */
  async function login(loginData) {
    const result = mockLogin(loginData.username, loginData.password)

    if (!result.success) {
      throw new Error(result.message)
    }

    token.value = result.data.token
    userInfo.value = result.data.userInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

    return { data: result.data }
  }

  /**
   * 用户注册（模拟认证）
   */
  async function register(registerData) {
    const result = mockRegister(
      registerData.username,
      registerData.password,
      registerData.email,
      registerData.nickname
    )

    if (!result.success) {
      throw new Error(result.message)
    }

    return { message: result.message }
  }

  /**
   * 获取用户信息（模拟）
   */
  async function getUserInfo() {
    const result = mockGetUserInfo(token.value)
    if (result.success) {
      userInfo.value = result.data
      localStorage.setItem('userInfo', JSON.stringify(result.data))
    }
    return { data: result.data }
  }

  /**
   * 更新用户信息（模拟）
   */
  async function updateUserInfo(updateData) {
    const result = mockUpdateUserInfo(updateData)
    if (result.success) {
      userInfo.value = result.data
      localStorage.setItem('userInfo', JSON.stringify(result.data))
    }
    return { data: result.data }
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  /**
   * 初始化用户信息（从localStorage恢复）
   */
  function initUserInfo() {
    const storedUserInfo = localStorage.getItem('userInfo')
    const storedToken = localStorage.getItem('token')
    if (storedUserInfo && storedToken) {
      userInfo.value = JSON.parse(storedUserInfo)
      token.value = storedToken
    }
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    login,
    register,
    getUserInfo,
    updateUserInfo,
    logout,
    initUserInfo
  }
})
