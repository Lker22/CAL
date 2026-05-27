/**
 * 模拟认证系统（无后端时使用）
 * 用户数据存储在 localStorage 中
 */

const STORAGE_KEY = 'mock_users'

/**
 * 默认管理员账号
 */
const DEFAULT_ADMIN = {
  id: 1,
  username: 'admin',
  password: 'admin123',
  email: 'admin@medicraft.com',
  nickname: '管理员',
  role: 'admin',
  createdAt: '2024-01-01 00:00:00'
}

/**
 * 获取所有用户（从localStorage）
 */
export function getAllUsers() {
  const users = localStorage.getItem(STORAGE_KEY)
  if (users) {
    return JSON.parse(users)
  }
  // 首次初始化，创建默认管理员
  saveUsers([DEFAULT_ADMIN])
  return [DEFAULT_ADMIN]
}

/**
 * 保存用户列表
 */
function saveUsers(users) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(users))
}

/**
 * 模拟登录
 */
export function mockLogin(username, password) {
  const users = getAllUsers()
  const user = users.find(
    (u) => u.username === username && u.password === password
  )

  if (!user) {
    return {
      success: false,
      message: '用户名或密码错误'
    }
  }

  // 生成token
  const token = `mock_${Date.now()}_${Math.random().toString(36).slice(2)}`

  return {
    success: true,
    data: {
      token,
      userInfo: {
        id: user.id,
        username: user.username,
        email: user.email,
        nickname: user.nickname,
        role: user.role,
        createdAt: user.createdAt
      }
    }
  }
}

/**
 * 模拟注册
 */
export function mockRegister(username, password, email, nickname) {
  const users = getAllUsers()

  // 检查用户名是否存在
  if (users.some((u) => u.username === username)) {
    return {
      success: false,
      message: '用户名已存在'
    }
  }

  const newUser = {
    id: Date.now(),
    username,
    password,
    email,
    nickname,
    role: 'user',
    createdAt: new Date().toLocaleString('zh-CN')
  }

  users.push(newUser)
  saveUsers(users)

  return {
    success: true,
    message: '注册成功'
  }
}

/**
 * 模拟获取用户信息
 */
export function mockGetUserInfo(token) {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    return {
      success: true,
      data: JSON.parse(userInfo)
    }
  }
  return {
    success: false,
    message: '未登录'
  }
}

/**
 * 模拟更新用户信息
 */
export function mockUpdateUserInfo(updateData) {
  const users = getAllUsers()
  const currentUsername = JSON.parse(localStorage.getItem('userInfo') || '{}').username

  const userIndex = users.findIndex((u) => u.username === currentUsername)
  if (userIndex === -1) {
    return {
      success: false,
      message: '用户不存在'
    }
  }

  users[userIndex] = {
    ...users[userIndex],
    ...updateData
  }

  saveUsers(users)

  return {
    success: true,
    data: users[userIndex]
  }
}

/**
 * 打印默认账号信息
 */
export function printDefaultCredentials() {
  console.log('%c=== AI学习辅助系统 - 默认登录账号 ===', 'color: #409eff; font-size: 16px; font-weight: bold;')
  console.log('%c用户名: admin', 'color: #67c23a; font-size: 14px;')
  console.log('%c密码: admin123', 'color: #67c23a; font-size: 14px;')
  console.log('%c角色: 管理员', 'color: #e6a23c; font-size: 14px;')
  console.log('%c==============================', 'color: #409eff; font-size: 16px;')
}
