import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import router from '@/router'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value)

  // 登录 
  async function login(loginData) {
    const res = await userApi.login(loginData)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return { data: res.data }
  }

  // 注册 
  async function register(registerData) {
    const res = await userApi.register(registerData)
    return { message: res.message }
  }

  // 获取用户信息
  async function getUserInfo() {
    const res = await userApi.getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return { data: res.data }
  }

  // 更新用户信息
  async function updateUserInfo(updateData) {
    const res = await userApi.updateUserInfo(updateData)
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return { data: res.data }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  // 初始化用户信息
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